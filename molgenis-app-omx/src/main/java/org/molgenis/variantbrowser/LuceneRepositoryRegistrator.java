package org.molgenis.variantbrowser;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.fieldtypes.FieldType;
import org.molgenis.fieldtypes.LongField;
import org.molgenis.fieldtypes.MrefField;
import org.molgenis.fieldtypes.StringField;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

@Component
public class LuceneRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;

    @Value("${index.directory:@null}")
    private String indexDirectory;
    @Value("${data.directory:@null}")
    private String dataDirectory;

    @Autowired
	public LuceneRepositoryRegistrator(DataService dataService)
	{
		this.dataService = dataService;
	}

	@Override
	public int getOrder()
	{
		return Ordered.HIGHEST_PRECEDENCE + 2;
	}

    @Override
	public void onApplicationEvent(ContextRefreshedEvent event)
	{
        StringBuilder indexMeta = new StringBuilder();
        final File folder = new File(dataDirectory);
        File[] files = folder.listFiles();
        for (File file : files){
            try {
                if(!file.isHidden()) {
                    indexMeta.append(MetadataReader.getMetadataString(file));
                }
            } catch (IOException e) {
                System.err.println("ERROR getting headers for file: "+file.getName());
            }
        }
        //indexMeta.append("UMCG_Diagnostics_Cardio_Batch1_106Samples\r\n" + "CHROM,POS,ID,REF,ALT,QUAL,referenceCount,hetrozygoteAltCount,homezygoteAltCount,FILTER,INFO\r\n"
		//		+ "UMCG_Diagnostics_Cardio_Batch2_107Samples\r\n" + "CHROM,POS,ID,REF,ALT,referenceCount,hetrozygoteAltCount,homezygoteAltCount,QUAL,FILTER,INFO\r\n"
		//		+ "UMCG_Diagnostics_Cardio_Batch3_108Samples\r\n" + "CHROM,POS,ID,REF,ALT,referenceCount,hetrozygoteAltCount,homezygoteAltCount,QUAL,FILTER,INFO\r\n"
        //        + "CADD_DSP_allsnps\r\n" + "chr,pos,ref,alt,raw,phred");
        System.out.println(indexMeta.toString());
        CSVReader csvReader = new CSVReader(new StringReader(indexMeta.toString()));
		try
		{
			String[] tokens;
			while ((tokens = csvReader.readNext()) != null)
			{
				EntityMetaData entityMetaData = createEntityMetaData(tokens[0], csvReader.readNext());
				dataService
						.addRepository(new LuceneRepository(indexDirectory, entityMetaData, dataService));
			}
			dataService.addRepository(new LuceneRepository(indexDirectory, createEntityMetaData(
					"variantdata", new String[]
					{ "CHROM", "POS", "INFO" }), dataService));
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
		finally
		{
			try
			{
				csvReader.close();
			}
			catch (IOException e)
			{
			}
		}
	}

	private EntityMetaData createEntityMetaData(final String entityName, final String[] attributeNames)
	{
		final List<AttributeMetaData> attributes = new ArrayList<AttributeMetaData>();
		for (String attributeName : attributeNames)
		{
			AttributeMetaData attribute = createAttributeMetaData(attributeName);
			attributes.add(attribute);
		}
		attributes.add(createAttributeMetaData("refs"));
		attributes.add(createAttributeMetaData("__identifier"));

		return new EntityMetaData()
		{
			@Override
			public String getName()
			{
				return entityName;
			}

			@Override
			public String getLabel()
			{
				return getName();
			}

			@Override
			public String getDescription()
			{
				return null;
			}

			@Override
			public Iterable<AttributeMetaData> getAttributes()
			{
				return attributes;
			}

			@Override
			public Iterable<AttributeMetaData> getAtomicAttributes()
			{
				return getAttributes();
			}

			@Override
			public AttributeMetaData getIdAttribute()
			{
				for (AttributeMetaData attribute : attributes)
					if (attribute.isIdAtrribute()) return attribute;
				return null;
			}

			@Override
			public AttributeMetaData getLabelAttribute()
			{
				for (AttributeMetaData attribute : attributes)
					if (attribute.isLabelAttribute()) return attribute;
				return null;
			}

			@Override
			public AttributeMetaData getAttribute(String attributeName)
			{
				for (AttributeMetaData attribute : attributes)
					if (attribute.getName().equalsIgnoreCase(attributeName)) return attribute;
				return null;
			}

			@Override
			public Class<? extends Entity> getEntityClass()
			{
				// TODO Auto-generated method stub
				return null;
			}
		};

	}

	private AttributeMetaData createAttributeMetaData(final String attributeName)
	{
		return new AttributeMetaData()
		{

			@Override
			public String getName()
			{
				return attributeName;
			}

			@Override
			public String getLabel()
			{
				return getName();
			}

			@Override
			public String getDescription()
			{
				return null;
			}

			@Override
			public FieldType getDataType()
			{
				if (attributeName.equalsIgnoreCase("POS")) return new LongField();
				else if (attributeName.equalsIgnoreCase("refs")) return new MrefField();
				else return new StringField();
			}

			@Override
			public boolean isNillable()
			{
				return true;
			}

			@Override
			public boolean isReadonly()
			{
				return false;
			}

			@Override
			public boolean isUnique()
			{
				return false;
			}

			@Override
			public boolean isVisible()
			{
				return true;// attributeName.startsWith("__") ? false : true;
			}

			@Override
			public Object getDefaultValue()
			{
				return null;
			}

			@Override
			public boolean isIdAtrribute()
			{
				return attributeName.equals("__id");
			}

			@Override
			public boolean isLabelAttribute()
			{
				return attributeName.equals("__identifier");
			}

			@Override
			public boolean isLookupAttribute()
			{
				return false;
			}

			@Override
			public boolean isAuto()
			{
				return false;
			}

			@Override
			public EntityMetaData getRefEntity()
			{
				return attributeName.equalsIgnoreCase("refs") ? createEntityMetaData("variantdata", new String[]
				{ "CHROM, POS" }) : null;
			}

			@Override
			public Iterable<AttributeMetaData> getAttributeParts()
			{
				return null;
			}
		};
	}
}
