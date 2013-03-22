package org.molgenis.omx.decorators;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.omx.core.Feature;
import org.molgenis.omx.core.Protocol;

public class ProtocolDecorator<E extends Protocol> extends MapperDecorator<E>
{
	public ProtocolDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		for(Protocol p : entities)
		{
			//check only 'root' protocols ?
			if(p.getClass().equals(Protocol.class)){
				
			}
			
		}
		return super.add(entities);
	}

}
