package org.molgenis.data.vcf.utils;

import autovalue.shaded.com.google.common.common.collect.Lists;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.MolgenisFieldTypes;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.datastructures.Sample;
import org.molgenis.data.vcf.datastructures.Trio;
import org.molgenis.vcf.meta.VcfMetaInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static org.molgenis.MolgenisFieldTypes.MREF;
import static org.molgenis.data.vcf.VcfRepository.ALT;
import static org.molgenis.data.vcf.VcfRepository.CHROM;
import static org.molgenis.data.vcf.VcfRepository.POS;
import static org.molgenis.data.vcf.VcfRepository.REF;

public class VcfUtils
{
	/**
	 * Creates a internal molgenis id from a vcf entity
	 *
	 * @param vcfEntity
	 * @return the id
	 */
	public static String createId(Entity vcfEntity)
	{
		StringBuilder strBuilder = new StringBuilder();
		strBuilder.append(StringUtils.strip(vcfEntity.get(CHROM).toString()));
		strBuilder.append("_");
		strBuilder.append(StringUtils.strip(vcfEntity.get(POS).toString()));
		strBuilder.append("_");
		strBuilder.append(StringUtils.strip(vcfEntity.get(REF).toString()));
		strBuilder.append("_");
		strBuilder.append(StringUtils.strip(vcfEntity.get(ALT).toString()));
		String idStr = strBuilder.toString();

		// use MD5 hash to prevent ids that are too long
		MessageDigest messageDigest;
		try
		{
			messageDigest = MessageDigest.getInstance("MD5");
		}
		catch (NoSuchAlgorithmException e)
		{
			throw new RuntimeException(e);
		}
		byte[] md5Hash = messageDigest.digest(idStr.getBytes(Charset.forName("UTF-8")));

		// convert MD5 hash to string ids that can be safely used in URLs
		String id = BaseEncoding.base64Url().omitPadding().encode(md5Hash);

		return id;
	}

	public static String getIdFromInfoField(String line)
	{
		int idStartIndex = line.indexOf("ID=") + 3;
		int idEndIndex = line.indexOf(',');
		return line.substring(idStartIndex, idEndIndex);
	}

	public static List<AttributeMetaData> getAtomicAttributesFromList(Iterable<AttributeMetaData> outputAttrs)
	{
		List<AttributeMetaData> result = new ArrayList<>();
		for (AttributeMetaData attributeMetaData : outputAttrs)
		{
			if (attributeMetaData.getDataType().getEnumType().equals(MolgenisFieldTypes.FieldTypeEnum.COMPOUND))
			{
				result.addAll(getAtomicAttributesFromList(attributeMetaData.getAttributeParts()));
			}
			else
			{
				result.add(attributeMetaData);
			}
		}
		return result;
	}

	public static Map<String, AttributeMetaData> getAttributesMapFromList(Iterable<AttributeMetaData> outputAttrs)
	{
		Map<String, AttributeMetaData> attributeMap = new LinkedHashMap<>();
		List<AttributeMetaData> attributes = getAtomicAttributesFromList(outputAttrs);
		for (AttributeMetaData attributeMetaData : attributes)
		{
			attributeMap.put(attributeMetaData.getName(), attributeMetaData);
		}
		return attributeMap;
	}

	public static String toVcfDataType(MolgenisFieldTypes.FieldTypeEnum dataType)
	{
		switch (dataType)
		{
			case BOOL:
				return VcfMetaInfo.Type.FLAG.toString();
			case LONG:
			case DECIMAL:
				return VcfMetaInfo.Type.FLOAT.toString();
			case INT:
				return VcfMetaInfo.Type.INTEGER.toString();
			case EMAIL:
			case ENUM:
			case HTML:
			case HYPERLINK:
			case STRING:
			case TEXT:
			case DATE:
			case DATE_TIME:
			case CATEGORICAL:
			case XREF:
			case CATEGORICAL_MREF:
			case MREF:
				return VcfMetaInfo.Type.STRING.toString();
			case COMPOUND:
			case FILE:
			default:
				throw new RuntimeException("unsupported vcf data type " + dataType);
		}
	}

	public static Iterator<Entity> reverseXrefMrefRelation(Iterator<Entity> annotatedRecords)
	{
		return new Iterator<Entity>()
		{
			PeekingIterator<Entity> effects = Iterators.peekingIterator(annotatedRecords);

			DefaultEntityMetaData resultEMD;
			EntityMetaData effectsEMD;

			private void createResultEntityMetaData(Entity effect, EntityMetaData variantEMD)
			{
				if (resultEMD == null || effectsEMD == null)
				{
					effectsEMD = effect.getEntityMetaData();
					resultEMD = new DefaultEntityMetaData(variantEMD);
					resultEMD.addAttribute(VcfWriterUtils.EFFECT).setDataType(MREF).setRefEntity(effectsEMD);
				}
			}

			@Override
			public boolean hasNext()
			{
				return effects.hasNext();
			}

			private Entity createEntityStructure(Entity variant, List<Entity> effectsForVariant)
			{
				createResultEntityMetaData(effectsForVariant.get(0), variant.getEntityMetaData());
				Entity newVariant = new MapEntity(variant, resultEMD);

				if (effectsForVariant.size() > 1)
				{
					newVariant.set(VcfWriterUtils.EFFECT, effectsForVariant);
				}
				else
				{
					// is this an empty effect entity?
					Entity entity = effectsForVariant.get(0);
					boolean isEmpty = true;
					for (AttributeMetaData attr : effectsEMD.getAtomicAttributes())
					{
						if (attr.getName().equals(effectsEMD.getIdAttribute().getName())
								|| attr.getName().equals(VcfWriterUtils.VARIANT))
						{
							continue;
						}
						else if (entity.get(attr.getName()) != null)
						{
							isEmpty = false;
							break;
						}
					}

					if (!isEmpty) newVariant.set(VcfWriterUtils.EFFECT, effectsForVariant);
				}
				return newVariant;
			}

			@Override
			public Entity next()
			{
				Entity variant = null;
				String peekedId;
				List<Entity> effectsForVariant = Lists.newArrayList();
				while (effects.hasNext())
				{
					peekedId = effects.peek().getEntity(VcfWriterUtils.VARIANT).getIdValue().toString();
					if (variant == null || variant.getIdValue().toString().equals(peekedId))
					{
						Entity effect = effects.next();
						variant = effect.getEntity(VcfWriterUtils.VARIANT);
						effectsForVariant.add(effect);
					}
					else
					{
						return createEntityStructure(variant, effectsForVariant);
					}
				}
				return createEntityStructure(variant, effectsForVariant);
			}
		};
	}

	public static List<Entity> createEntityStructureForVcf(EntityMetaData entityMetaData, String attributeName,
			Stream<Entity> inputStream)
	{
		return createEntityStructureForVcf(entityMetaData, attributeName, inputStream, Collections.emptyList());
	}

	public static List<Entity> createEntityStructureForVcf(EntityMetaData entityMetaData, String attributeName,
			Stream<Entity> inputStream, List<AttributeMetaData> annotatorAttributes)
	{
		AttributeMetaData attributeToParse = entityMetaData.getAttribute(attributeName);
		String description = attributeToParse.getDescription();
		if (description.indexOf(':') == -1)
		{
			throw new RuntimeException(
					"Unable to create entitystructure, missing semicolon in description of [" + attributeName + "]");
		}

		String[] step1 = description.split(":");
		String entityName = org.apache.commons.lang.StringUtils.deleteWhitespace(step1[0]);
		String value = step1[1].replaceAll("^\\s'|'$", "");

		Map<Integer, AttributeMetaData> metadataMap = parseDescription(value, annotatorAttributes);
		DefaultEntityMetaData xrefMetaData = getXrefEntityMetaData(metadataMap, entityName);

		List<Entity> results = new ArrayList<>();
		for (Entity inputEntity : inputStream.collect(Collectors.toList()))
		{
			DefaultEntityMetaData newEntityMetadata = removeRefFieldFromInfoMetadata(attributeToParse, inputEntity);
			Entity originalEntity = new MapEntity(inputEntity, newEntityMetadata);

			results.addAll(parseValue(xrefMetaData, metadataMap, inputEntity.getString(attributeToParse.getName()),
					originalEntity));
		}
		return results;
	}

	private static DefaultEntityMetaData getXrefEntityMetaData(Map<Integer, AttributeMetaData> metadataMap,
			String entityName)
	{
		DefaultEntityMetaData xrefMetaData = new DefaultEntityMetaData(entityName);
		xrefMetaData.addAttributeMetaData(new DefaultAttributeMetaData("identifier").setAuto(true).setVisible(false),
				EntityMetaData.AttributeRole.ROLE_ID);
		xrefMetaData.addAllAttributeMetaData(com.google.common.collect.Lists.newArrayList(metadataMap.values()));
		xrefMetaData
				.addAttributeMetaData(new DefaultAttributeMetaData("Variant", MolgenisFieldTypes.FieldTypeEnum.MREF));
		return xrefMetaData;
	}

	private static DefaultEntityMetaData removeRefFieldFromInfoMetadata(AttributeMetaData attributeToParse,
			Entity inputEntity)
	{
		DefaultEntityMetaData newMeta = (DefaultEntityMetaData) inputEntity.getEntityMetaData();
		DefaultAttributeMetaData newInfoMetadata = (DefaultAttributeMetaData) newMeta.getAttribute(VcfRepository.INFO);
		newInfoMetadata.setAttributesMetaData(StreamSupport
				.stream(newMeta.getAttribute(VcfRepository.INFO).getAttributeParts().spliterator(), false)
				.filter(attr -> !attr.getName().equals(attributeToParse.getName())).collect(Collectors.toList()));
		newMeta.removeAttributeMetaData(VcfRepository.INFO_META);
		newMeta.addAttributeMetaData(newInfoMetadata);
		return newMeta;
	}

	private static Map<Integer, AttributeMetaData> parseDescription(String description,
			List<AttributeMetaData> annotatorAttributes)
	{
		String value = description.replaceAll("^\\s'|'$", "");

		String[] attributeStrings = value.split("\\|");
		Map<Integer, AttributeMetaData> attributeMap = new HashMap<>();
		Map<String, AttributeMetaData> annotatorAttributeMap = getAttributesMapFromList(annotatorAttributes);
		for (int i = 0; i < attributeStrings.length; i++)
		{
			String attribute = attributeStrings[i];
			MolgenisFieldTypes.FieldTypeEnum type = annotatorAttributeMap.containsKey(attribute)
					? annotatorAttributeMap.get(attribute).getDataType().getEnumType()
					: MolgenisFieldTypes.FieldTypeEnum.STRING;
			AttributeMetaData attr = new DefaultAttributeMetaData(
					org.apache.commons.lang.StringUtils.deleteWhitespace(attribute), type).setLabel(attribute);
			attributeMap.put(i, attr);
		}
		return attributeMap;
	}

	private static List<Entity> parseValue(EntityMetaData metadata, Map<Integer, AttributeMetaData> attributesMap,
			String value, Entity originalEntity)
	{
		List<Entity> result = new ArrayList<>();
		String[] valuesPerEntity = value.split(",");

		for (Integer i = 0; i < valuesPerEntity.length; i++)
		{
			String[] values = valuesPerEntity[i].split("\\|");

			MapEntity singleResult = new MapEntity(metadata);
			for (Integer j = 0; j < values.length; j++)
			{
				String attributeName = attributesMap.get(j).getName().replaceAll("^\'|\'$", "");
				String attributeValue = values[j];
				singleResult.set(attributeName, attributeValue);
				singleResult.set("Variant", originalEntity);

			}
			result.add(singleResult);
		}
		return result;
	}

	/**
	 *
	 * Get pedigree data from VCF Now only support child, father, mother No fancy data structure either Output:
	 * result.put(childID, Arrays.asList(new String[]{motherID, fatherID}));
	 *
	 * TODO: check that a MotherID cannot also be a FatherID ?
	 *
	 * @param inputVcfFileReader
	 * @return
	 * @throws FileNotFoundException
	 */
	public static HashMap<String, Trio> getPedigree(BufferedReader inputVcfFileReader) throws Exception
	{
		HashMap<String, Trio> result = new HashMap<>();

		String line = null;
		while ((line = inputVcfFileReader.readLine()) != null)
		{

			// quit when we don't see header lines anymore
			if (!line.startsWith(VcfRepository.PREFIX))
			{
				break;
			}

			// detect pedigree line
			// expecting e.g. ##PEDIGREE=<Child=100400,Mother=100402,Father=100401>
			if (line.startsWith("##PEDIGREE"))
			{
				//System.out.println("Pedigree data line: " + line);
				String childID = null;
				String motherID = null;
				String fatherID = null;

				String lineStripped = line.replace("##PEDIGREE=<", "").replace(">", "");
				String[] lineSplit = lineStripped.split(",", -1);
				for (String element : lineSplit)
				{
					if (element.startsWith("Child"))
					{
						childID = element.replace("Child=", "");
						if(result.containsKey(childID))
						{
							throw new Exception("Child has multiple occurences in pedigree data: " + childID);
						}
					}
					else if (element.startsWith("Mother"))
					{
						motherID = element.replace("Mother=", "");
					}
					else if (element.startsWith("Father"))
					{
						fatherID = element.replace("Father=", "");
					}
					else
					{
						throw new MolgenisDataException(
								"Expected Child, Mother or Father, but found: " + element + " in line " + line);
					}
				}

				// only child ID would be silly, but 1 missing parent is okay, so we want child + mother or child + father
				if ((childID != null && motherID != null) || (childID != null && fatherID != null))
				{
					// good
					result.put(childID, new Trio(new Sample(childID), (motherID != null ? new Sample(motherID) : null), (fatherID != null ? new Sample(fatherID) : null)));
				}
				else
				{
					throw new MolgenisDataException("Missing Child, Mother or Father ID in line " + line);
				}
			}
		}
		return result;
	}

	/**
	 * Group samples for the sample phenotype together
	 * @param sampleToPhenotype
	 * @return
     */
	public static HashMap<String, List<String>> getPhenotypeToSampleIDs(HashMap<String, String> sampleToPhenotype)
	{
		HashMap<String, List<String>> res = new HashMap<>();

		for(String sample : sampleToPhenotype.keySet())
		{
			String phenotype = sampleToPhenotype.get(sample);
			if(res.containsKey(phenotype))
			{
				res.get(phenotype).add(sample);
			}
			else
			{
				ArrayList<String> sampleIDs = new ArrayList<>();
				sampleIDs.add(sample);
				res.put(phenotype, sampleIDs);
			}
		}

		return res;
	}

	/**
	 * Get sample-phenotype relations out of VCF header
	 * @param vcfFile
	 * @return
	 * @throws Exception
     */
	public static HashMap<String, String> getSampleToPhenotype(File vcfFile) throws Exception {

		String line;
		HashMap<String, String> res = new HashMap<>();
		Scanner s = new Scanner(vcfFile);

		while ((line = s.nextLine()) != null)
		{
			// quit when we don't see header lines anymore
			if (!line.startsWith(VcfRepository.PREFIX)) {
				break;
			}

			if (line.startsWith("##SAMPLE")) {
				String phenotype = null;
				String id = null;
				String lineStripped = line.replace("##SAMPLE=<", "").replace(">", "");
				String[] lineSplit = lineStripped.split(",", -1);
				for (String element : lineSplit) {
					if (element.startsWith("PHENOTYPE")) {
						phenotype = element.replace("PHENOTYPE=", "");
					} else if (element.startsWith("ID")) {
						id = element.replace("ID=", "");
					}
				}
				if (id == null) {
					throw new Exception("Sample line does not contain 'ID': " + line);
				}
				if (phenotype == null) {
					throw new Exception("Sample line does not contain 'PHENOTYPE': " + line);
				}
				res.put(id, phenotype);
			}
		}
		return res;
	}

}
