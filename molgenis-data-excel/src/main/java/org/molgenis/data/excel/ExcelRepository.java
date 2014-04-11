package org.molgenis.data.excel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellReference;
import org.molgenis.MolgenisFieldTypes.FieldTypeEnum;
import org.molgenis.data.Entity;
import org.molgenis.data.EntityMetaData;
import org.molgenis.data.processor.AbstractCellProcessor;
import org.molgenis.data.processor.CellProcessor;
import org.molgenis.data.support.AbstractRepository;
import org.molgenis.data.support.DefaultAttributeMetaData;
import org.molgenis.data.support.DefaultEntityMetaData;
import org.molgenis.util.CaseInsensitiveLinkedHashMap;

/**
 * ExcelSheet {@link org.molgenis.data.Repository} implementation
 * 
 * It is assumed that the first row of the sheet is the header row.
 * 
 * All attributes will be of the string type. The cell values are converted to string.
 * 
 * The url of this Repository is defined as excel://${filename}/${sheetname}
 */
public class ExcelRepository extends AbstractRepository
{
	public static final String BASE_URL = "excel://";
	private final Sheet sheet;

	/** process cells after reading */
	private List<CellProcessor> cellProcessors;
	/** column names index */
	private Map<String, Integer> colNamesMap;
	private EntityMetaData entityMetaData;

	public ExcelRepository(String fileName, Sheet sheet)
	{
		this(fileName, sheet, null);
	}

	public ExcelRepository(String fileName, Sheet sheet, List<CellProcessor> cellProcessors)
	{
		super(BASE_URL + fileName + "/" + sheet.getSheetName());
		this.sheet = sheet;
		this.cellProcessors = cellProcessors;
	}

	public int getNrRows()
	{
		return sheet.getLastRowNum() + 1; // getLastRowNum is 0-based
	}

	@Override
	public Iterator<Entity> iterator()
	{
		final Iterator<Row> it = sheet.iterator();
		if (!it.hasNext()) return Collections.<Entity> emptyList().iterator();

		// create column header index once and reuse
		Row headerRow = it.next();
		if (colNamesMap == null)
		{
			colNamesMap = toColNamesMap(headerRow);
		}

		if (!it.hasNext()) return Collections.<Entity> emptyList().iterator();

		return new Iterator<Entity>()
		{
			@Override
			public boolean hasNext()
			{
				return it.hasNext();
			}

			@Override
			public ExcelEntity next()
			{
				return new ExcelEntity(it.next(), colNamesMap, cellProcessors, getEntityMetaData());
			}

			@Override
			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}

	public void addCellProcessor(CellProcessor cellProcessor)
	{
		if (cellProcessors == null) cellProcessors = new ArrayList<CellProcessor>();
		cellProcessors.add(cellProcessor);
	}

	@Override
	public EntityMetaData getEntityMetaData()
	{
		if (entityMetaData == null)
		{
			entityMetaData = new DefaultEntityMetaData(sheet.getSheetName(), ExcelEntity.class);

			if (colNamesMap == null)
			{
				Iterator<Row> it = sheet.iterator();
				if (it.hasNext())
				{
					// First row contains the headers
					colNamesMap = toColNamesMap(it.next());
				}
			}

			if (colNamesMap != null)
			{
				for (String colName : colNamesMap.keySet())
				{
					((DefaultEntityMetaData) entityMetaData).addAttributeMetaData(new DefaultAttributeMetaData(colName,
							FieldTypeEnum.STRING));
				}
			}
		}

		return entityMetaData;
	}

	private Map<String, Integer> toColNamesMap(Row headerRow)
	{
		if (headerRow == null) return null;

		Map<String, Integer> columnIdx = new CaseInsensitiveLinkedHashMap<Integer>();
		int i = 0;
		for (Iterator<Cell> it = headerRow.cellIterator(); it.hasNext();)
		{
			try
			{
				String header = AbstractCellProcessor.processCell(it.next().getStringCellValue(), true, cellProcessors);
				columnIdx.put(header, i++);
			}
			catch (final IllegalStateException ex)
			{
				final int row = headerRow.getRowNum();
				final String column = CellReference.convertNumToColString(i);
				throw new IllegalStateException("Invalid value at [" + sheet.getSheetName() + "] " + column + row + 1,
						ex);
			}
		}
		return columnIdx;
	}

	@Override
	public void close() throws IOException
	{
		// Nothing
	}
}
