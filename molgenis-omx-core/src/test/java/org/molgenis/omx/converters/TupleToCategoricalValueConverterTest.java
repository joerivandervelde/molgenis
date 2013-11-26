package org.molgenis.omx.converters;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;

import org.molgenis.data.DataService;
import org.molgenis.data.Query;
import org.molgenis.data.support.QueryImpl;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.omx.observ.Category;
import org.molgenis.omx.observ.ObservableFeature;
import org.molgenis.omx.observ.value.CategoricalValue;
import org.molgenis.util.tuple.Cell;
import org.molgenis.util.tuple.KeyValueTuple;
import org.testng.annotations.Test;

public class TupleToCategoricalValueConverterTest
{

	@Test
	public void toCell() throws ValueConverterException
	{
		DataService dataService = mock(DataService.class);
		String catIdentifier = "category1";
		String catName = "category #1";
		Category category = new Category();
		category.setIdentifier(catIdentifier);
		category.setName(catName);
		CategoricalValue value = new CategoricalValue();
		value.setValue(category);
		Cell<String> cell = new TupleToCategoricalValueConverter(dataService).toCell(value);
		assertEquals(cell.getKey(), catIdentifier);
		assertEquals(cell.getValue(), catName);
	}

	@Test
	public void fromTuple() throws ValueConverterException, DatabaseException
	{
		Category category = new Category();
		String valueCode = "code1";
		ObservableFeature feature = mock(ObservableFeature.class);
		DataService dataService = mock(DataService.class);

		Query q = new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature).and().eq(Category.VALUECODE, valueCode);

		when(dataService.findOne(Category.ENTITY_NAME, q)).thenReturn(category);

		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, valueCode);
		CategoricalValue value = new TupleToCategoricalValueConverter(dataService).fromTuple(tuple, colName, feature);
		assertEquals(value.getValue(), category);
	}

	@Test
	public void updateFromTuple() throws ValueConverterException, DatabaseException
	{
		CategoricalValue value = new CategoricalValue();
		Category category = new Category();
		String valueCode = "code1";
		ObservableFeature feature = mock(ObservableFeature.class);
		DataService dataService = mock(DataService.class);

		Query q = new QueryImpl().eq(Category.OBSERVABLEFEATURE, feature).and().eq(Category.VALUECODE, valueCode);

		when(dataService.findOne(Category.ENTITY_NAME, q)).thenReturn(category);
		String colName = "col";
		KeyValueTuple tuple = new KeyValueTuple();
		tuple.set(colName, valueCode);
		new TupleToCategoricalValueConverter(dataService).updateFromTuple(tuple, colName, feature, value);
		assertEquals(value.getValue(), category);
	}
}
