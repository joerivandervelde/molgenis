package org.molgenis.variantbrowser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

public class VariantReferencer
{
	public static void main(String[] args) throws IOException
	{
		String indexDir = "/Users/charbonb/data/variants/index";

		String[] entityNames =
		{ "1000G_Cardio_summary", "1000G_Fakepatient_0", "1000G_Fakepatient_1", "1000G_Fakepatient_2",
				"1000G_Fakepatient_3", "1000G_Fakepatient_4", "CADD_DSP_allsnps", "CADD_PKP2_allsnps",
				"CADD_TTN_allsnps", "GoNL_CardioGenes_populationvariants", "LOVD_DMD_TTN", "NCBI_ClinVar",
				"NHGRI_GWAS_Catalog", "UMCG_5GPM_TTN_pat", "UMCG_ARVC_DSP", "UMCG_ARVC_PKP2", "UMCG_ARVC_TTN",
				"UMCG_Diagnostics_CardioManagedVariants_Artefact", "UMCG_Diagnostics_CardioManagedVariants_Benign",
				"UMCG_Diagnostics_CardioManagedVariants_LikelyBenign",
				"UMCG_Diagnostics_CardioManagedVariants_LikelyPathogenic",
				"UMCG_Diagnostics_CardioManagedVariants_Pathogenic", "UMCG_Diagnostics_CardioManagedVariants_VOUS",
				"UMCG_Diagnostics_Cardio_Batch1_106Samples", "UMCG_Diagnostics_Cardio_Batch2_107Samples",
				"UMCG_Diagnostics_Cardio_Batch3_108Samples" };

		System.out.println("creating variant map");
		Map<String, List<String>> variantMap = new HashMap<String, List<String>>();
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
		try
		{
			for (int i = 0; i < indexReader.maxDoc(); i++)
			{
				Document doc = indexReader.document(i);

				String chrom = doc.get("CHROM");
				if (chrom == null || chrom.isEmpty()) chrom = doc.get("chr");

				String pos = doc.get("POS");
				if (pos == null || pos.isEmpty()) pos = doc.get("pos");

				if (chrom != null && pos != null)
				{
					String entityName = doc.get("__entity");
					String key = chrom + pos;
					List<String> dataSets = variantMap.get(key);
					if (dataSets == null)
					{
						dataSets = new ArrayList<String>(1);
						variantMap.put(key, dataSets);
					}
					dataSets.add(entityName);
				}

				if (i % 10000 == 0) System.out.println(i);
			}

			System.out.println("writing variant map");
			Directory dir = FSDirectory.open(new File(indexDir + "-out"));
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_45, analyzer);
			iwc.setOpenMode(OpenMode.CREATE);
			iwc.setRAMBufferSizeMB(1024.0);

			StringField identifierField = new StringField("__identifier", "", Field.Store.YES);
			StringField refField = new StringField("refs", "", Field.Store.YES);
			IndexWriter indexWriter = new IndexWriter(dir, iwc);
			try
			{
				for (int i = 0; i < indexReader.maxDoc(); i++)
				{
					Document doc = indexReader.document(i);

					String chrom = doc.get("CHROM");
					if (chrom == null || chrom.isEmpty()) chrom = doc.get("chr");

					String pos = doc.get("POS");
					if (pos == null || pos.isEmpty()) pos = doc.get("pos");

					doc.removeField("__identifier");

					if (chrom != null && pos != null)
					{
						String entityName = doc.get("__entity");
						String key = chrom + pos;

						List<String> dataSets = variantMap.get(key);
						if (dataSets != null)
						{
							for (String dataSet : dataSets)
							{
								if (!dataSet.equals(entityName))
								{
									refField.setStringValue(dataSet + ':' + chrom + '-' + pos);
									doc.add(refField);
								}
							}
						}

						identifierField.setStringValue(entityName + ':' + chrom + '-' + pos);
						doc.add(identifierField);
					}
					indexWriter.addDocument(doc);
					if (i % 10000 == 0) System.out.println(i);
				}
				// indexWriter.updateDocument(term, doc);
			}
			finally
			{
				indexWriter.close();
			}
		}
		finally
		{
			indexReader.close();
		}
	}
}
