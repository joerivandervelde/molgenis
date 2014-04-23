package org.molgenis.variantbrowser;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public class SearchFiles
{
	public static void main(String[] args) throws IOException, ParseException
	{
		IndexReader indexReader = DirectoryReader.open(FSDirectory.open(new File("D:\\tmp\\variantindex-out")));
		try
		{
			IndexSearcher indexSearcher = new IndexSearcher(indexReader);
			Query q = new TermQuery(new Term("__identifier", "1000G_Cardio_summary:15-63335326"));
			TopDocs topDocs = indexSearcher.search(q, Integer.MAX_VALUE);
			System.out.println(topDocs.totalHits);

			// TopDocs search = indexSearcher.search(
			// NumericRangeQuery.newLongRange("POS", 63335473l, 63335475l, true, true), Integer.MAX_VALUE);
			// System.out.println(search.scoreDocs.length);
			// Query parse = new QueryParser(Version.LUCENE_45, "fieldname", new StandardAnalyzer(Version.LUCENE_45))
			// .parse("POS:[63335473 TO 63335475]");
			// TopDocs search2 = indexSearcher.search(parse, Integer.MAX_VALUE);
			// System.out.println(search2.scoreDocs.length);

		}
		finally
		{
			indexReader.close();
		}
	}
}
