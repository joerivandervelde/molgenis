package org.molgenis.omx.decorators;

import java.util.List;

import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.Mapper;
import org.molgenis.framework.db.MapperDecorator;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.omx.values.PermittedValue;
import org.molgenis.omx.values.XrefValue;

public class XrefValueDecorator<E extends XrefValue> extends MapperDecorator<E>
{
	public XrefValueDecorator(Mapper<E> generatedMapper)
	{
		super(generatedMapper);
	}

	@Override
	public int add(List<E> entities) throws DatabaseException
	{
		for(XrefValue o : entities)
		{
			//PermittedValue p = this.find(PermittedValue.class, new QueryRule(PermittedValue.FEATURE, Operator.EQUALS, o.getf));
			// ???
		}
		return super.add(entities);
	}

}
