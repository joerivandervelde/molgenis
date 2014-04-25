package org.molgenis.variantbrowser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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
import org.springframework.beans.factory.annotation.Value;

public class VariantReferencer
{
	public static void main(String[] args) throws IOException
	{
        String usage = "java org.molgenis.variantbrowser.VariantReferencer"
                + "-index [INPUT_INDEX_PATH]";
        Properties props = new Properties();
        InputStream stream = new FileInputStream(System.getProperty("user.home")+"/.molgenis/omx/molgenis-server.properties"); // open the file
        props.load(stream);

        // process properties content
        String indexOutputDir = props.getProperty("index.directory");

        String indexPath = null;
        for (int i = 0; i < args.length; i++)
        {
            if ("-index".equals(args[i]))
            {
                indexPath = args[i + 1];
                i++;
            }
        }
        if (indexPath == null)
        {
            System.err.println("Usage: " + usage);
            System.exit(1);
        }

        System.out.println("creating variant map");
		Map<String, List<String>> variantMap = new HashMap<String, List<String>>();
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
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
			Directory dir = FSDirectory.open(new File(indexOutputDir));
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
