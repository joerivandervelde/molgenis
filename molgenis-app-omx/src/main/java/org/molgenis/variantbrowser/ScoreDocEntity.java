package org.molgenis.variantbrowser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.molgenis.data.AttributeMetaData;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.MolgenisDataException;
import org.molgenis.data.support.AbstractEntity;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

public class ScoreDocEntity extends AbstractEntity
{
	private static final long serialVersionUID = 1L;

	private final ScoreDoc scoreDoc;
	private final IndexSearcher indexSearcher;
	private final EntityMetaData entityMetaData;
	private final DataService dataService;
	private transient Document doc;

	public ScoreDocEntity(ScoreDoc scoreDoc, IndexSearcher indexSearcher, EntityMetaData entityMetaData,
			DataService dataService)
	{
		this.scoreDoc = scoreDoc;
		this.indexSearcher = indexSearcher;
		this.entityMetaData = entityMetaData;
		this.dataService = dataService;
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		return entityMetaData;
	}

	@Override
	public Iterable<String> getAttributeNames()
	{
		Document doc = getDocument();
		return Lists.transform(doc.getFields(), new Function<IndexableField, String>()
		{
			@Override
			public String apply(IndexableField field)
			{
				return field.name();
			}
		});
	}

	@Override
	public Integer getIdValue()
	{
		return scoreDoc.doc;
	}

	@Override
	public List<String> getLabelAttributeNames()
	{
		return Arrays.asList(getEntityMetaData().getLabelAttribute().getName());
	}

	@Override
	public Object get(String attributeName)
	{
		Document doc = getDocument();

		AttributeMetaData attribute = entityMetaData.getAttribute(attributeName);
		switch (attribute.getDataType().getEnumType())
		{
			case XREF:
			{
				String refVal = doc.get(attributeName);
				return toRefEntity(refVal);
			}
			case MREF:
			{
				String[] refVals = doc.getValues(attributeName);
				List<Entity> refEntities = new ArrayList<Entity>();
				for (String refVal : refVals)
					refEntities.add(toRefEntity(refVal));
				return refEntities;
			}
			default:
				return doc.get(attributeName);
		}
	}

	@Override
	public void set(String attributeName, Object value)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void set(Entity entity, boolean strict)
	{
		throw new UnsupportedOperationException();
	}

	private Document getDocument()
	{
		if (doc == null)
		{
			try
			{
				doc = indexSearcher.doc(scoreDoc.doc);
			}
			catch (IOException e)
			{
				throw new MolgenisDataException(e);
			}
		}
		return doc;
	}

	private Entity toRefEntity(String val)
	{
		try
		{
			int idx = val.indexOf(":");
			String refEntityName = val.substring(0, idx);
			EntityMetaData refEntityMetaData = dataService.getEntityMetaData(refEntityName);
			Query q = new TermQuery(new Term("__identifier", val));
			TopDocs refTopDocs = indexSearcher.search(q, Integer.MAX_VALUE);
			return new ScoreDocEntity(refTopDocs.scoreDocs[0], indexSearcher, refEntityMetaData, dataService);
		}
		catch (NumberFormatException e)
		{
			throw new MolgenisDataException(e);
		}
		catch (IOException e)
		{
			throw new MolgenisDataException(e);
		}
	}
}
