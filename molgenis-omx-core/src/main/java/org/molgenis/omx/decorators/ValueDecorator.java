package org.molgenis.omx.decorators;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.omx.core.Value;

public class ValueDecorator<E extends Value> extends MapperDecorator<E>
{
	public ValueDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		for(Value o : entities)
		{
			if(o.getClass().equals(Value.class))
			{
				throw new DatabaseException("You cannot create an instance of Value, use a subclass instead");
			}
			
			//validation rules?
			
			
		}
		return super.add(entities);
	}

}
