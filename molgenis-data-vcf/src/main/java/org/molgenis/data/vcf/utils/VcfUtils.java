package org.molgenis.data.vcf.utils;

import com.google.common.io.BaseEncoding;
import org.apache.commons.lang3.StringUtils;
import org.molgenis.data.Entity;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.meta.AttributeType;
import org.molgenis.data.meta.model.Attribute;
import org.molgenis.data.meta.model.AttributeFactory;
import org.molgenis.data.meta.model.EntityTypeFactory;
import org.molgenis.data.vcf.VcfRepository;
import org.molgenis.data.vcf.datastructures.Sample;
import org.molgenis.data.vcf.datastructures.Trio;
import org.molgenis.vcf.meta.VcfMetaInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.molgenis.data.meta.AttributeType.COMPOUND;
import static org.molgenis.data.vcf.model.VcfAttributes.*;

@Component
public class VcfUtils
{
	@Autowired
	private EntityTypeFactory entityTypeFactory;

	@Autowired
	private AttributeFactory attributeFactory;

	/**
	 * Creates a internal molgenis id from a vcf entity
	 *
	 * @return the id
	 */
	public static String createId(Entity vcfEntity)
	{
		String idStr = StringUtils.strip(vcfEntity.get(CHROM).toString()) + "_" + StringUtils.strip(
				vcfEntity.get(POS).toString()) + "_" + StringUtils.strip(vcfEntity.get(REF).toString()) + "_"
				+ StringUtils.strip(vcfEntity.get(ALT).toString()) + "_" + StringUtils.strip(
				vcfEntity.get(ID).toString()) + "_" + StringUtils.strip(
				vcfEntity.get(QUAL) != null ? vcfEntity.get(QUAL).toString() : "") + "_" + StringUtils.strip(
				vcfEntity.get(FILTER) != null ? vcfEntity.get(FILTER).toString() : "");

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
		byte[] md5Hash = messageDigest.digest(idStr.getBytes(UTF_8));

		// convert MD5 hash to string ids that can be safely used in URLs

		return BaseEncoding.base64Url().omitPadding().encode(md5Hash);
	}

	public static String getIdFromInfoField(String line)
	{
		int idStartIndex = line.indexOf("ID=") + 3;
		int idEndIndex = line.indexOf(',');
		return line.substring(idStartIndex, idEndIndex);
	}

	public static List<Attribute> getAtomicAttributesFromList(Iterable<Attribute> outputAttrs)
	{
		List<Attribute> result = new ArrayList<>();
		for (Attribute attribute : outputAttrs)
		{
			if (attribute.getDataType() == COMPOUND)
			{
				result.addAll(getAtomicAttributesFromList(attribute.getChildren()));
			}
			else
			{
				result.add(attribute);
			}
		}
		return result;
	}

	public static Map<String, Attribute> getAttributesMapFromList(Iterable<Attribute> outputAttrs)
	{
		Map<String, Attribute> attributeMap = new LinkedHashMap<>();
		List<Attribute> attributes = getAtomicAttributesFromList(outputAttrs);
		for (Attribute attribute : attributes)
		{
			attributeMap.put(attribute.getName(), attribute);
		}
		return attributeMap;
	}

	protected static String toVcfDataType(AttributeType dataType)
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
			case ONE_TO_MANY:
				return VcfMetaInfo.Type.STRING.toString();
			case COMPOUND:
			case FILE:
				throw new RuntimeException("invalid vcf data type " + dataType);
			default:
				throw new RuntimeException("unsupported vcf data type " + dataType);
		}
	}

	/**
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
