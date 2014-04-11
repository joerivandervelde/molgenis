package org.molgenis.data.annotation.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.annotation.AnnotationService;
import org.molgenis.data.annotation.TabixReader;
import org.molgenis.data.annotation.VariantAnnotator;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.data.support.MapEntity;
import org.molgenis.framework.server.MolgenisSettings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

/**
 * <p>
 * This class performs a system call to cross reference a chromosome and genomic location with a tabix indexed file. A
 * match can result in 1, 2 or 3 hits. These matches are reduced to one based on a reference and alternative nucleotide
 * base. The remaining hit will be used to parse two CADD scores.
 * </p>
 * 
 * <p>
 * <b>CADD returns:</b> CADD score Absolute, CADD score Scaled
 * </p>
 * 
 * @author mdehaan
 * 
 * */
@Component("caddService")
public class CaddServiceAnnotator extends VariantAnnotator
{
	private final MolgenisSettings molgenisSettings;
	private final AnnotationService annotatorService;

	// the cadd service returns these two values
	static final String CADD_SCALED = "CADD_SCALED";
	static final String CADD_ABS = "CADD_ABS";

	private static final String NAME = "CADD";

	public static final String CADD_FILE_LOCATION_PROPERTY = "cadd_location";

	@Autowired
	public CaddServiceAnnotator(MolgenisSettings molgenisSettings, AnnotationService annotatorService)
	{
		this.molgenisSettings = molgenisSettings;
		this.annotatorService = annotatorService;
	}

	@Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
		annotatorService.addAnnotator(this);
	}

	@Override
	public boolean annotationDataExists()
	{
		return new File(molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY)).exists();
	}

	@Override
	public String getName()
	{
		return NAME;
	}

	@Override
	public List<Entity> annotateEntity(Entity entity) throws IOException, InterruptedException
	{
		List<Entity> results = new ArrayList<Entity>();
		String caddFile = molgenisSettings.getProperty(CADD_FILE_LOCATION_PROPERTY);

		String chromosome = entity.getString(CHROMOSOME);
		Long position = entity.getLong(POSITION);
		String reference = entity.getString(REFERENCE);
		String alternative = entity.getString(ALTERNATIVE);

		String caddAbs = "";
		String caddScaled = "";

		TabixReader txr = new TabixReader(caddFile);
		String line = txr.query(chromosome + ":" + position).next();

		String[] split = null;

		if (line != null)
		{
			split = line.split("\t");

			if (split[2].equals(reference) && split[3].equals(alternative))
			{
				caddAbs = split[4];
				caddScaled = split[5];
			}
		}

		HashMap<String, Object> resultMap = new HashMap<String, Object>();

		resultMap.put(CADD_ABS, caddAbs);
		resultMap.put(CADD_SCALED, caddScaled);
		resultMap.put(CHROMOSOME, chromosome);
		resultMap.put(POSITION, position);
		resultMap.put(ALTERNATIVE, alternative);
		resultMap.put(REFERENCE, reference);

		results.add(new MapEntity(resultMap));

		return results;
	}

	@Override
	public EntityMetaData getOutputMetaData()
	{
		DefaultEntityMetaData metadata = new DefaultEntityMetaData(this.getClass().getName(), MapEntity.class);

		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CADD_ABS, FieldTypeEnum.DECIMAL));
		metadata.addAttributeMetaData(new DefaultAttributeMetaData(CADD_SCALED, FieldTypeEnum.DECIMAL));

		return metadata;
	}

}
