package org.molgenis.variantbrowser;

import java.util.Iterator;

import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TopDocs;
import org.molgenis.data.DataService;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;

public class TopDocsEntityIterable implements Iterable<Entity>
{
	private final TopDocs topDocs;
	private final IndexSearcher indexSearcher;
	private final EntityMetaData entityMetaData;
	private final DataService dataService;
	private final int offset;

	public TopDocsEntityIterable(TopDocs topDocs, IndexSearcher indexSearcher, EntityMetaData entityMetaData,
			DataService dataService)
	{
		this(topDocs, indexSearcher, entityMetaData, dataService, 0);
	}

	public TopDocsEntityIterable(TopDocs topDocs, IndexSearcher indexSearcher, EntityMetaData entityMetaData,
			DataService dataService, int offset)
	{
		this.topDocs = topDocs;
		this.indexSearcher = indexSearcher;
		this.entityMetaData = entityMetaData;
		this.dataService = dataService;
		this.offset = offset;
	}

	@Override
	public Iterator<Entity> iterator()
	{
		return new Iterator<Entity>()
		{
			private int idx = offset;

			@Override
			public boolean hasNext()
			{
				return idx < topDocs.scoreDocs.length;
			}

			@Override
			public Entity next()
			{
				return new ScoreDocEntity(topDocs.scoreDocs[idx++], indexSearcher, entityMetaData, dataService);
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}
}
