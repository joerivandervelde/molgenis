package org.molgenis.omx.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.molgenis.framework.db.Database;
import org.molgenis.framework.db.DatabaseException;
import org.molgenis.framework.db.QueryRule;
import org.molgenis.framework.db.QueryRule.Operator;
import org.molgenis.io.TableReader;
import org.molgenis.io.TableReaderFactory;
import org.molgenis.io.TupleReader;
import org.molgenis.omx.core.DataSet;
import org.molgenis.omx.core.Feature;
import org.molgenis.omx.core.Observation;
import org.molgenis.omx.core.ObservedValue;
import org.molgenis.omx.core.Value;
import org.molgenis.omx.values.TextValue;
import org.molgenis.util.tuple.Tuple;

public class DataSetImporter
{
	private static final Logger LOG = Logger.getLogger(DataSetImporter.class);
	private static final String DATASET_SHEET_PREFIX = "dataset_";
	private Database db;

	public DataSetImporter(Database db)
	{
		if (db == null) throw new IllegalArgumentException();
		this.db = db;
	}

	public void importDataSet(File file, List<String> dataSetEntityNames) throws IOException, DatabaseException
	{
		TableReader tableReader = TableReaderFactory.create(file);
		try
		{
			for (String tableName : tableReader.getTableNames())
			{
				if (dataSetEntityNames.contains(tableName))
				{
					LOG.info("importing dataset " + tableName + " from file " + file + "...");
					importSheet(tableReader.getTupleReader(tableName), tableName);
				}
			}
		}
		finally
		{
			tableReader.close();
		}
	}

	private void importSheet(TupleReader sheetReader, String sheetName) throws DatabaseException, IOException
	{
		String identifier = sheetName.substring(DATASET_SHEET_PREFIX.length());

		List<DataSet> dataSets = db.find(DataSet.class, new QueryRule(DataSet.IDENTIFIER, Operator.EQUALS, identifier));
		if (dataSets == null || dataSets.isEmpty())
		{
			LOG.warn("dataset " + identifier + " does not exist in db");
			return;
		}
		else if (dataSets.size() > 1)
		{
			LOG.warn("multiple datasets exist for identifier " + identifier);
			return;
		}

		DataSet dataSet = dataSets.get(0);

		Iterator<String> colIt = sheetReader.colNamesIterator();
		if (!colIt.hasNext()) throw new IOException("sheet '" + sheetName + "' contains no columns");

		// create observation feature map
		Map<String, Feature> featureMap = new LinkedHashMap<String, Feature>();
		while (colIt.hasNext())
		{
			String observableFeatureIdentifier = colIt.next();
			Feature observableFeature = findObservableFeature(observableFeatureIdentifier);
			featureMap.put(observableFeatureIdentifier, observableFeature);
		}

		boolean doTx = !db.inTx();
		try
		{
			if (doTx) db.beginTx();

			for (Tuple row : sheetReader)
			{
				ArrayList<ObservedValue> obsValueList = new ArrayList<ObservedValue>();

				// create observation set
				Observation observationSet = new Observation();
				observationSet.setPartOfDataSet(dataSet);
				db.add(observationSet);

				for (Map.Entry<String, Feature> entry : featureMap.entrySet())
				{
					// create observed value
					String value = row.getString(entry.getKey());
					ObservedValue observedValue = new ObservedValue();
					observedValue.setFeature(entry.getValue());
					
					//FIXME: SUPPORT VALUE TYPES OTHER THAN STRING!!!
					//observedValue.setValue(value);
					
					TextValue v = new TextValue();
					v.setValue(value);
					db.add(v);
					observedValue.setValue(v);
					
					observedValue.setObservation(observationSet);

					// add to db
					obsValueList.add(observedValue);
				}
				db.add(obsValueList);
			}

			if (doTx) db.commitTx();
		}
		catch (DatabaseException e)
		{
			if (doTx) db.rollbackTx();
			throw e;
		}
		catch (Exception e)
		{
			if (doTx) db.rollbackTx();
			throw new IOException(e);
		}
	}

	private Feature findObservableFeature(String observableFeatureIdentifier) throws DatabaseException,
			IOException
	{
		List<Feature> observableFeatures = db.find(Feature.class, new QueryRule(
				Feature.IDENTIFIER, Operator.EQUALS, observableFeatureIdentifier));
		if (observableFeatures == null || observableFeatures.isEmpty()) throw new IOException("ObservableFeature "
				+ observableFeatureIdentifier + " does not exist in db");
		Feature observableFeature = observableFeatures.get(0);
		return observableFeature;
	}
}
