package org.molgenis.variantbrowser;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.*;
import java.util.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import au.com.bytecode.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Value;

/**
 * Index all text files under a directory.
 * <p>
 * This is a command-line application demonstrating simple Lucene indexing. Run it with no command-line arguments for
 * usage information.
 */
public class IndexFiles
{

    public static final String REFERENCE = "referenceCount";
    public static final String HETROZYGOTE_ALT = "hetrozygoteAltCount";
    public static final String HOMEZYGOTE_ALT = "homezygoteAltCount";
    public static final String INFOFIELD = "INFO";
    public static final String CHROMOSOME = "CHROM";
    public static final String POSITION = "POS";
    public static final String GTC_PREFIX = "gtc=";

    private IndexFiles()
	{
	}

	/** Index all text files under a directory. */
	public static void main(String[] args)
	{   Properties props = new Properties();
        try {
            InputStream stream = new FileInputStream(System.getProperty("user.home")+"/.molgenis/omx/molgenis-server.properties"); // open the file
            props.load(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // process properties content
        String docsPath = props.getProperty("data.directory");
        String usage = "java org.molgenis.variantbrowser.IndexFiles"
				+ "This indexes the documents in path specified in molgenis-server.properties, creating a Lucene index"
				+ "in INDEX_PATH that can be searched with SearchFiles";
		String indexPath = "index";
		boolean create = true;
		for (int i = 0; i < args.length; i++)
		{
			if ("-index".equals(args[i]))
			{
				indexPath = args[i + 1];
				i++;
			}
			else if ("-update".equals(args[i]))
			{
				create = false;
			}
		}

		if (docsPath == null)
		{
			System.err.println("Usage: " + usage);
			System.exit(1);
		}

		final File docDir = new File(docsPath);
		if (!docDir.exists() || !docDir.canRead())
		{
			System.out.println("Document directory '" + docDir.getAbsolutePath()
					+ "' does not exist or is not readable, please check the path");
			System.exit(1);
		}

		Date start = new Date();
		try
		{
			System.out.println("Indexing to directory '" + indexPath + "'...");

			Directory dir = FSDirectory.open(new File(indexPath));
			// :Post-Release-Update-Version.LUCENE_XY:
			Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_45);
			IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_45, analyzer);

			if (create)
			{
				// Create a new index in the directory, removing any
				// previously indexed documents:
				iwc.setOpenMode(OpenMode.CREATE);
			}
			else
			{
				// Add new documents to an existing index:
				iwc.setOpenMode(OpenMode.CREATE_OR_APPEND);
			}

			// Optional: for better indexing performance, if you
			// are indexing many documents, increase the RAM
			// buffer. But if you do this, increase the max heap
			// size to the JVM (eg add -Xmx512m or -Xmx1g):
			//
			iwc.setRAMBufferSizeMB(1024.0);

			IndexWriter writer = new IndexWriter(dir, iwc);
			indexDocs(writer, docDir);

			// NOTE: if you want to maximize search performance,
			// you can optionally call forceMerge here. This can be
			// a terribly costly operation, so generally it's only
			// worth it when your index is relatively static (ie
			// you're done adding documents to it):
			//
			// writer.forceMerge(1);

			writer.close();

			Date end = new Date();
			System.out.println(end.getTime() - start.getTime() + " total milliseconds");

		}
		catch (IOException e)
		{
			System.out.println(" caught a " + e.getClass() + "\n with message: " + e.getMessage());
		}
	}

	/**
	 * Indexes the given file using the given writer, or if a directory is given, recurses over files and directories
	 * found under the given directory.
	 * 
	 * NOTE: This method indexes one document per input file. This is slow. For good throughput, put multiple documents
	 * into your input file(s). An example of this is in the benchmark module, which can create "line doc" files, one
	 * document per line, using the <a
	 * href="../../../../../contrib-benchmark/org/apache/lucene/benchmark/byTask/tasks/WriteLineDocTask.html"
	 * >WriteLineDocTask</a>.
	 * 
	 * @param writer
	 *            Writer to the index where the given file/dir info will be stored
	 * @param file
	 *            The file to index, or the directory to recurse into to find files to index
	 * @throws IOException
	 *             If there is a low-level I/O error
	 */
	static void indexDocs(IndexWriter writer, File file) throws IOException
	{
		// do not try to index files that cannot be read
		if (file.canRead())
		{
			if (file.isDirectory())
			{
				String[] files = file.list();
				// an IO error could occur
				if (files != null)
				{
					for (int i = 0; i < files.length; i++)
					{
						indexDocs(writer, new File(file, files[i]));
					}
				}
			}
			else
			{
				indexFile(writer, file);
			}
		}
	}

	static void indexFile(IndexWriter writer, File file) throws IOException
	{
		// System.out.println(file.getName());
		// System.out.println("indexing " + file.getPath());
		FileInputStream fis;
        try
		{
			fis = new FileInputStream(file);
		}
		catch (FileNotFoundException fnfe)
		{
			// at least on windows, some temporary files raise this exception with an "access denied" message
			// checking if the file can be read doesn't help
			return;
		}

		try
		{

			// make a new, empty document
			Document doc = new Document();
			Map<String, Field> fields = null;
			CSVReader csvReader = new CSVReader(new BufferedReader(new InputStreamReader(fis, "UTF-8")), '\t');
			try
			{
				String[] tokens, headers = null;
				String entityName = null;
				while ((tokens = csvReader.readNext()) != null)
				{
					if (tokens[0].startsWith("##")) continue;
					else if (tokens[0].startsWith("#"))
					{
						headers = new String[tokens.length];
						System.arraycopy(tokens, 0, headers, 0, tokens.length);
						headers[0] = headers[0].substring(1);

						fields = new HashMap<String, Field>();
						for (String header : headers)
						{
                            if("chr".equals(header)) header = CHROMOSOME;
                            if("pos".equals(header)) header = POSITION;
                            Field field;
							// if (!header.equalsIgnoreCase("POS"))
							// {
                            if(INFOFIELD.equals(header)) {
                                addField(doc, fields, new IntField(REFERENCE, -1, Field.Store.YES));
                                addField(doc, fields, new IntField(HETROZYGOTE_ALT, -1, Field.Store.YES));
                                addField(doc, fields, new IntField(HOMEZYGOTE_ALT, -1, Field.Store.YES));
                            }

							field = new TextField(header, "", Field.Store.YES);
							// }
							// else
							// {
							// field = new LongField(header, -1l, Field.Store.YES);
							// }
                            addField(doc, fields, field);
						}
						Field identifierField = new StringField("__identifier", "", Field.Store.YES);
                        addField(doc,fields,identifierField);

						entityName = FilenameUtils.removeExtension(file.getName());
						doc.add(new TextField("__entity", entityName, Field.Store.YES));
						doc.add(new TextField("__entity", "variantdata", Field.Store.YES));

                        System.out.println("!!!!:" + entityName);
						System.out.println("!!!!:" + StringUtils.join(headers, ','));
					}
					else if (headers == null)
					{
						headers = new String[tokens.length];
						System.arraycopy(tokens, 0, headers, 0, tokens.length);

						fields = new HashMap<String, Field>();
						for (String header : headers)
						{
							Field field;
							// if (!header.equalsIgnoreCase("POS"))
							// {
                            if("chr".equals(header)) header = CHROMOSOME;
                            if("pos".equals(header)) header = POSITION;
                            field = new TextField(header, "", Field.Store.YES);
							// }
							// else
							// {
							// field = new LongField(header, -1l, Field.Store.YES);
							// }
							fields.put(header, field);
							doc.add(field);
						}
                        if(!Arrays.asList(headers).contains("ID")){
                            addField(doc, fields, new TextField("ID", "", Field.Store.YES));
                        }
						Field identifierField = new StringField("__identifier", "", Field.Store.YES);
						fields.put("__identifier", identifierField);
						doc.add(identifierField);

						entityName = FilenameUtils.removeExtension(file.getName());
						doc.add(new TextField("__entity", entityName, Field.Store.YES));
						doc.add(new TextField("__entity", "variantdata", Field.Store.YES));

                        System.out.println("????:" + entityName);
						System.out.println("????:" + StringUtils.join(headers, ','));
					}
					else
					{
						if (tokens.length != headers.length)
						{
							// System.out.println(line + " - number of tokens [" + tokens.length
							// + "] does not match number of columns [" + headers.length + "], skipping line");
							continue;
						}

						String chr = null, pos = null;
                        fields.get("ID").setStringValue("");
                        for (int i = 0; i < tokens.length; ++i)
						{
							String token = tokens[i];
							String header = headers[i];
							if (header.equalsIgnoreCase(CHROMOSOME) || header.equalsIgnoreCase("chr")){
                                chr = token;
                                header = CHROMOSOME;
                            }
							if (header.equalsIgnoreCase(POSITION)) {
                                pos = token;
                                header = POSITION;
                            };
							Field field = fields.get(header);
							// FIXME see http://stackoverflow.com/a/7078087
							// if (!header.equalsIgnoreCase("POS"))
							// {
							if(header.equals(INFOFIELD)){
                                String[] gtc;
                                int index = token.toLowerCase().indexOf(GTC_PREFIX);
                                if(index != -1) {
                                    String temp = token.substring(index+4);
                                    int endIndex = temp.indexOf(";");
                                    if (endIndex == -1) {
                                        gtc = temp.split(",");
                                    } else {
                                        gtc = temp.substring(0, endIndex).split(",");
                                    }
                                    if (gtc.length == 3) {
                                        fields.get(REFERENCE).setIntValue(new Integer(gtc[0]));
                                        fields.get(HETROZYGOTE_ALT).setIntValue(new Integer(gtc[1]));
                                        fields.get(HOMEZYGOTE_ALT).setIntValue(new Integer(gtc[2]));
                                    } else {
                                        System.err.println("ERROR - invalid number of gtc values; " + token);
                                    }
                                }
                            }
                            field.setStringValue(token);

							// }
							// else
							// {
							// try
							// {
							// field.setLongValue(Long.valueOf(token));
							// }
							// catch (NumberFormatException e)
							// {
							// System.out.println(line + " - pos is not a number [" + token
							// + "], skipping line");
							// continue;
							// }
							// }
						}
                        if(fields.get("ID").stringValue().equals("")){
                            fields.get("ID").setStringValue("ID_" + pos + "_" + chr);
                        }
						if (chr != null && pos != null) fields.get("__identifier").setStringValue(
								entityName + ':' + chr + '-' + pos);

						if (writer.getConfig().getOpenMode() == OpenMode.CREATE)
						{
							// New index, so we just add the document (no old document can be there):
							writer.addDocument(doc);
						}
						else
						{
							// Existing index (an old copy of this document may have been indexed) so
							// we use updateDocument instead to replace the old one matching the exact
							// path, if present:
							writer.updateDocument(new Term("path", file.getPath()), doc);
						}
					}
					// if (++line % 10000 == 0) System.out.println("parsed " + line + " lines");
				}
			}
			finally
			{
				csvReader.close();
			}
		}
		finally
		{
			fis.close();
		}
	}

    private static void addField(Document doc, Map<String, Field> fields, Field field) {
        fields.put(field.name(), field);
        doc.add(field);
    }

}
