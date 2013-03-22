package org.molgenis.omx.decorators;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.omx.core.DataItem;

public class DataItemDecorator<E extends DataItem> extends MapperDecorator<E>
{
	public DataItemDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		for (DataItem d : entities)
		{
			if (!d.getDataSet().getProtocol().getFeatures().contains(d.getFeature()))
			{
				throw new DatabaseException("This dataitem refers to a feature that is not part of the dataset protocol!");
			}
		}
		return super.add(entities);
	}

}
