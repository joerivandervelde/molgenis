package org.molgenis.variantbrowser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TotalHitCountCollector;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataAccessException;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.Query;
import org.molgenis.data.QueryRule;
import org.molgenis.data.QueryRule.Operator;
import org.molgenis.data.Queryable;
import org.molgenis.data.Repository;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.elasticsearch.request.LuceneQueryStringBuilder;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.springframework.beans.factory.annotation.Value;

public class LuceneRepository implements Queryable, Repository
{
    public static final String BASE_URL = "lucene://";

	private final IndexReader indexReader;
	private final IndexSearcher indexSearcher;
	private final QueryParser queryParser;
	private final EntityMetaData entityMetaData;
	private final DataService dataService;

	public LuceneRepository(String indexDir, EntityMetaData entityMetaData, DataService dataService) throws IOException
	{
        this.indexReader = DirectoryReader.open(FSDirectory.open(new File(indexDir)));
		this.indexSearcher = new IndexSearcher(indexReader);
		this.entityMetaData = entityMetaData;
		this.queryParser = new QueryParser(Version.LUCENE_45, "fieldname", new StandardAnalyzer(Version.LUCENE_45));
		this.dataService = dataService;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public <E extends Entity> Iterable<E> iterator(Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String getUrl()
	{
		return BASE_URL + entityMetaData.getName() + '/';
	}

	@Override
	public long count()
	{
		return count(new QueryImpl());
	}

	@Override
	public long count(Query q)
	{
		org.apache.lucene.search.Query luceneQ = createQuery(q);
		try
		{
			TotalHitCountCollector countCollector = new TotalHitCountCollector();
			indexSearcher.search(luceneQ, countCollector);
			return countCollector.getTotalHits();
		}
		catch (IOException e)
		{
			throw new MolgenisDataAccessException(e);
		}

	}

	@Override
	public Iterable<Entity> findAll(Query q)
	{
		org.apache.lucene.search.Query luceneQ = createQuery(q);
		org.apache.lucene.search.Sort luceneSort = createSort(q);
		int n = q.getOffset() + q.getPageSize();

		TopDocs topDocs;
		try
		{
			if (luceneSort == null) topDocs = indexSearcher.search(luceneQ, n);
			else topDocs = indexSearcher.search(luceneQ, n, luceneSort);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
		return new TopDocsEntityIterable(topDocs, indexSearcher, entityMetaData, dataService, q.getOffset());
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Query q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Entity findOne(Query q)
	{
		org.apache.lucene.search.Query luceneQ = createQuery(q);

		TopDocs topDocs;
		try
		{
			topDocs = indexSearcher.search(luceneQ, 1);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
		return new TopDocsEntityIterable(topDocs, indexSearcher, entityMetaData, dataService).iterator().next();
	}

	@Override
	public Entity findOne(Integer id)
	{
		return new ScoreDocEntity(new ScoreDoc(id, 0f), indexSearcher, entityMetaData, dataService);
	}

	@Override
	public Iterable<Entity> findAll(Iterable<Integer> ids)
	{
		return Iterables.transform(ids, new Function<Integer, Entity>()
		{
			@Override
			public Entity apply(Integer id)
			{
				return findOne(id);
			}
		});
	}

	@Override
	public <E extends Entity> Iterable<E> findAll(Iterable<Integer> ids, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOne(Integer id, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public <E extends Entity> E findOne(Query q, Class<E> clazz)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return findAll(new QueryImpl()).iterator();
	}

	@Override
	public void close() throws IOException
	{
		indexReader.close();
	}

	@Override
	public String getName()
	{
		return entityMetaData.getName();
	}

	private org.apache.lucene.search.Sort createSort(Query q)
	{
		// TODO implement
		return null;
	}

	private org.apache.lucene.search.Query createQuery(Query q)
	{
		// hack
		if (q.getRules() != null && q.getRules().size() == 1 && q.getRules().get(0).getField().equals("__identifier"))
		{
			return new TermQuery(new Term("__identifier", q.getRules().get(0).getValue().toString()));
		}

		QueryImpl qExpanded = new QueryImpl(q);
		if (!qExpanded.getRules().isEmpty()) qExpanded.addRule(new QueryRule(Operator.AND));
		qExpanded.addRule(new QueryRule("__entity", Operator.EQUALS, getName()));

		String luceneQueryStr = LuceneQueryStringBuilder.buildQueryString(qExpanded.getRules());
		System.out.println(luceneQueryStr);
		try
		{
			return queryParser.parse(luceneQueryStr);
		}
		catch (ParseException e)
		{
			throw new MolgenisDataException(e);
		}
	}
}
