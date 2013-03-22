package org.molgenis.omx.decorators;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.omx.core.ObservedValue;

public class ObservedValueDecorator<E extends ObservedValue> extends MapperDecorator<E>
{
	public ObservedValueDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		for(ObservedValue o : entities)
		{
			if(o.getDataItem().getDataSet_Id().equals(o.getObservation().getPartOfDataSet_Id()))
			{
				throw new DatabaseException("DataItem refers to a different DataSet than Observation!");
			}
		}
		return super.add(entities);
	}

}
