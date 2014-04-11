package org.molgenis.variantbrowser;

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
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import au.com.bytecode.opencsv.CSVReader;

@Component
public class LuceneRepositoryRegistrator implements ApplicationListener<ContextRefreshedEvent>, Ordered
{
	private final DataService dataService;

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
		String indexMeta = "1000G_Cardio_summary\r\n"
				+ "CHROM,POS,ID,REF,ALT,QUAL,FILTER,INFO\r\n"
				+ "1000G_Fakepatient_0\r\n"
				+ "CHROM,POS,ID,REF,ALT,QUAL,FILTER,INFO,FORMAT,SAMPLEINDEX_0\r\n"
				+ "1000G_Fakepatient_1\r\n"
				+ "CHROM,POS,ID,REF,ALT,QUAL,FILTER,INFO,FORMAT,SAMPLEINDEX_1\r\n"
				+ "1000G_Fakepatient_2\r\n"
				+ "CHROM,POS,ID,REF,ALT,QUAL,FILTER,INFO,FORMAT,SAMPLEINDEX_2\r\n"
				+ "1000G_Fakepatient_3\r\n"
				+ "CHROM,POS,ID,REF,ALT,QUAL,FILTER,INFO,FORMAT,SAMPLEINDEX_3\r\n"
				+ "1000G_Fakepatient_4\r\n"
				+ "CHROM,POS,ID,REF,ALT,QUAL,FILTER,INFO,FORMAT,SAMPLEINDEX_4\r\n"
				+ "CADD_DSP_allsnps\r\n"
				+ "chr,pos,ref,alt,raw,phred\r\n"
				+ "CADD_PKP2_allsnps\r\n"
				+ "chr,pos,ref,alt,raw,phred\r\n"
				+ "CADD_TTN_allsnps\r\n"
				+ "chr,pos,ref,alt,raw,phred\r\n"
				+ "GoNL_CardioGenes_populationvariants\r\n"
				+ "CHROM,POS,ID,REF,ALT,QUAL,FILTER,INFO\r\n"
				+ "LOVD_DMD_TTN\r\n"
				+ "Exon,DNA change,TimesReported,Var_pub_as,RNA change,Protein change,DB-ID,Variant remarks,Genet_ori,Segregation,Reference,Template,Technique,Frequency,RE-site,chr,pos,cdna,ref,alt\r\n"
				+ "NCBI_ClinVar\r\n"
				+ "CHROM,POS,ID,REF,ALT,QUAL,FILTER,INFO\r\n"
				+ "NHGRI_GWAS_Catalog\r\n"
				+ "Index,Date.Added.to.Catalog,PUBMEDID,First.Author,Date,Journal,Link,Study,Disease.Trait,Initial.Sample.Size,Replication.Sample.Size,Region,Chr_id,chr,pos,Mapped_gene,Upstream_gene_id,Downstream_gene_id,Snp_gene_ids,Upstream_gene_distance,Downstream_gene_distance,Strongest.SNP.Risk.Allele,SNPs,Merged,Snp_id_current,Context,Intergenic,Risk.Allele.Frequency,p.Value,Pvalue_mlog,p.Value..text.,OR.or.beta,X95..CI..text.,Platform..SNPs.passing.QC.,CNV\r\n"
				+ "UMCG_5GPM_TTN_pat\r\n"
				+ "chr,pos,ref,alt,raw,phred\r\n"
				+ "UMCG_ARVC_DSP\r\n"
				+ "Gene,Locus,Exon,Mutation,DNA_Change,Protein_Change,Type,Reported_Classification,No_of_clinical_reports,Details,chr,pos,id,ref,alt,raw,phred\r\n"
				+ "UMCG_ARVC_PKP2\r\n"
				+ "Gene,Locus,Exon,Mutation,DNA_Change,Protein_Change,Type,Reported_Classification,No_of_clinical_reports,Details,chr,pos,id,ref,alt,raw,phred\r\n"
				+ "UMCG_ARVC_TTN\r\n"
				+ "chr,pos,ID,REF,ALT,Gene,Locus,Exon,DNA Change,Protein Change,Type,Reported Classification,No of clinical reports\r\n"
				+ "UMCG_Diagnostics_CardioManagedVariants_Artefact\r\n"
				+ "chr,pos,stop,ref,alt,variant_type,location,effect,gene,transcript,exon,c_nomen,p_nomen,dbsnp\r\n"
				+ "UMCG_Diagnostics_CardioManagedVariants_Benign\r\n"
				+ "chr,pos,stop,ref,alt,variant_type,location,effect,gene,transcript,exon,c_nomen,p_nomen,dbsnp\r\n"
				+ "UMCG_Diagnostics_CardioManagedVariants_LikelyBenign\r\n"
				+ "chr,pos,stop,ref,alt,variant_type,location,effect,gene,transcript,exon,c_nomen,p_nomen,dbsnp\r\n"
				+ "UMCG_Diagnostics_CardioManagedVariants_LikelyPathogenic\r\n"
				+ "chr,pos,stop,ref,alt,variant_type,location,effect,gene,transcript,exon,c_nomen,p_nomen,dbsnp\r\n"
				+ "UMCG_Diagnostics_CardioManagedVariants_Pathogenic\r\n"
				+ "chr,pos,stop,ref,alt,variant_type,location,effect,gene,transcript,exon,c_nomen,p_nomen,dbsnp\r\n"
				+ "UMCG_Diagnostics_CardioManagedVariants_VOUS\r\n"
				+ "chr,pos,stop,ref,alt,variant_type,location,effect,gene,transcript,exon,c_nomen,p_nomen,dbsnp\r\n"
				+ "UMCG_Diagnostics_Cardio_Batch1_106Samples\r\n" + "CHROM,POS,ID,REF,ALT,QUAL,FILTER,INFO\r\n"
				+ "UMCG_Diagnostics_Cardio_Batch2_107Samples\r\n" + "CHROM,POS,ID,REF,ALT,QUAL,FILTER,INFO\r\n"
				+ "UMCG_Diagnostics_Cardio_Batch3_108Samples\r\n" + "CHROM,POS,ID,REF,ALT,QUAL,FILTER,INFO";

		CSVReader csvReader = new CSVReader(new StringReader(indexMeta));
		try
		{
			String[] tokens;
			while ((tokens = csvReader.readNext()) != null)
			{
				EntityMetaData entityMetaData = createEntityMetaData(tokens[0], csvReader.readNext());
				dataService
						.addRepository(new LuceneRepository("D:\\tmp\\variantindex-out", entityMetaData, dataService));
			}
			dataService.addRepository(new LuceneRepository("D:\\tmp\\variantindex-out", createEntityMetaData(
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
