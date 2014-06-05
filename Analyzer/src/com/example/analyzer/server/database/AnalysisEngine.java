// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.example.analyzer.jdbc.server.ColumnTypeConverter;
import com.example.analyzer.server.database.DbTable.ColumnPairType;
import com.example.analyzer.server.expanders.DbColumnExpanderContext;
import com.example.analyzer.server.expression.Evaluator;
import com.example.analyzer.server.expression.Value;
import com.example.analyzer.shared.DbColumnPair;
import com.example.analyzer.shared.DbCompareOperation;
import com.example.analyzer.shared.DbSortColumn;
import com.example.analyzer.shared.DbVerticalJoinType;

public final class AnalysisEngine implements DbJoinColumnNameHandler
{
  public DbTable addCalculatedColumn(DbTable oldTable, String columnName, String formula)
  {
    Evaluator expression = new Evaluator(oldTable, formula);
    Iterable<String> columnNames = oldTable.getColumnNames();
    Iterable<Integer> columnTypes = oldTable.getColumnTypes();
    DbTable newTable = new DbTable(columnNames, columnTypes);
    newTable.addColumnName(columnName);
    int rowCount = oldTable.getRowCount();
    int columnCount = oldTable.getColumnCount();
    int columnType = Types.NULL;
    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
    {
      for (int columnOffset = 0; columnOffset < columnCount; columnOffset++)
      {
        newTable.set(rowOffset, columnOffset, oldTable.get(rowOffset, columnOffset));
      }
      Value value = expression.evaluateAt(rowOffset);
      newTable.set(rowOffset, columnCount, value.getValue());
      if (columnType == Types.NULL)
      {
        columnType = expression.getColumnType(value.getType());
      }
    }
    newTable.addColumnType(columnType);
    return newTable;
  }

  public DbTable changeColumnNames(DbTable oldTable, Iterable<String> columnNames)
  {
    Iterable<Integer> columnTypes = oldTable.getColumnTypes();
    DbTable newTable = new DbTable(columnNames, columnTypes);
    int rowCount = oldTable.getRowCount();
    int columnCount = oldTable.getColumnCount();
    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
    {
      for (int columnOffset = 0; columnOffset < columnCount; columnOffset++)
      {
        newTable.set(rowOffset, columnOffset, oldTable.get(rowOffset, columnOffset));
      }
    }
    return newTable;
  }

  public DbTable complement(DbTable left, DbTable right)
  {
    return null;
  }

  private DbSummarizer createSummarizer(DbPivotSummarizer pivotSummarizer)
  {
    try
    {
      Class<? extends DbSummarizer> summarizerClass = pivotSummarizer.getSummarizerClass();
      Constructor<? extends DbSummarizer> constructor = summarizerClass.getConstructor();
      DbSummarizer summarizer = constructor.newInstance();
      return summarizer;
    }
    catch (NoSuchMethodException e)
    {
      throw new RuntimeException("Class does not have an accessible no-arg constructor", e); // what brilliant engineer decided interfaces should not be able to specify constructors?!
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public DbTable crosstab(DbTable oldTable, DbColumnGroup horizontalColumnGroup, DbSummarizer dbHorizontalSummarizer, DbColumnGroup verticalColumnGroup, DbSummarizer dbVerticalSummarizer, DbSummarizer dbGrandTotalSummarizer)
  {
    return null;
  }

  private void defineColumn(ColumnPairType columnPairType, DbTable thisTable, DbTable thatTable, DbTable newTable, int thisOffset, int columnOffset, Iterable<DbColumnPair> columnPairs, LinkedList<Integer> excludedColumns, DbJoinColumnNameHandler joinColumnNameHandler)
  {
    String columnName = thisTable.getColumnName(thisOffset);
    boolean isDuplicate = thatTable.getColumnOffset(columnName) != -1;
    boolean isJoinColumn = isJoinColumn(columnPairs, columnPairType, columnName);
    int columnType = thisTable.getColumnType(thisOffset);
    String newColumnName = joinColumnNameHandler.getColumnName(columnPairType, columnName, columnType, isDuplicate, isJoinColumn);
    if (newColumnName == null)
    {
      excludedColumns.add(columnOffset);
    }
    else
    {
      newTable.addColumnName(newColumnName);
      newTable.addColumnType(columnType);
    }
  }

  public DbTable expand(DbTable oldTable, DbColumnExpanderContext... columnExpanderContexts)
  {
    return expand(oldTable, new IterableArray<DbColumnExpanderContext>(columnExpanderContexts));
  }

  public DbTable expand(DbTable oldTable, Iterable<DbColumnExpanderContext> columnExpanders)
  {
    DbTable newTable = new DbTable();
    int oldColumnCount = oldTable.getColumnCount();
    DbColumnExpanderContext[] columnExpanderContexts = new DbColumnExpanderContext[oldColumnCount]; // optimization, to avoid string compare for every cell in table

    for (int oldColumnOffset = 0; oldColumnOffset < oldColumnCount; oldColumnOffset++)
    {
      String oldColumnName = oldTable.getColumnName(oldColumnOffset);
      DbColumnExpanderContext columnExpanderContext = findColumnExpander(columnExpanders, oldColumnName);
      if (columnExpanderContext == null)
      {
        newTable.addColumnName(oldColumnName);
        newTable.addColumnType(oldTable.getColumnType(oldColumnOffset));
      }
      else
      {
        for (String expansionColumnName : columnExpanderContext.getColumnNames())
        {
          newTable.addColumnName(expansionColumnName);
        }
        for (int expansionColumnType : columnExpanderContext.getColumnTypes())
        {
          newTable.addColumnType(expansionColumnType);
        }
        columnExpanderContexts[oldColumnOffset] = columnExpanderContext;
      }
    }

    int rowCount = oldTable.getRowCount();

    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
    {
      int newColumnOffset = 0;
      for (int oldColumnOffset = 0; oldColumnOffset < oldColumnCount; oldColumnOffset++)
      {
        Object oldValue = oldTable.get(rowOffset, oldColumnOffset);
        DbColumnExpanderContext columnExpanderContext = columnExpanderContexts[oldColumnOffset];
        if (columnExpanderContext == null)
        {
          newTable.set(rowOffset, newColumnOffset++, oldValue);
        }
        else
        {
          for (Object newValue : columnExpanderContext.getColumnValues(oldValue))
          {
            newTable.set(rowOffset, newColumnOffset++, newValue);
          }
        }
      }
    }

    return newTable;
  }

  private DbColumnExpanderContext findColumnExpander(Iterable<DbColumnExpanderContext> columnExpanders, String oldColumnName)
  {
    for (DbColumnExpanderContext columnExpanderContext : columnExpanders)
    {
      if (oldColumnName.equals(columnExpanderContext.getColumnName()))
      {
        return columnExpanderContext;
      }
    }
    return null;
  }

  @Override
  public String getColumnName(ColumnPairType columnPairType, String oldColumnName, int oldColumnType, boolean isDuplicate, boolean isJoinColumn)
  {
    String newColumnName;
    if (isDuplicate)
    {
      if (columnPairType == ColumnPairType.FROM)
      {
        if (isJoinColumn)
        {
          newColumnName = oldColumnName;
        }
        else
        {
          newColumnName = oldColumnName + ".1";
        }
      }
      else
      {
        if (isJoinColumn)
        {
          newColumnName = null;
        }
        else
        {
          newColumnName = oldColumnName + ".2";
        }
      }
    }
    else
    {
      newColumnName = oldColumnName;
    }
    return newColumnName;
  }

  public DbTable intersect(DbTable left, DbTable right)
  {
    return null;
  }

  private boolean isJoinColumn(Iterable<DbColumnPair> columnPairs, ColumnPairType from, String columnName)
  {
    for (DbColumnPair columnPair : columnPairs)
    {
      switch (from)
      {
        case FROM:
          if (columnPair.getFromColumnName().equals(columnName))
            return true;
          break;
        case TO:
          if (columnPair.getToColumnName().equals(columnName))
            return true;
          break;
      }

    }
    return false;
  }

  public DbTable joinTablesHorizontally(DbTable fromTable, DbTable toTable, DbColumnPair... columnPairs)
  {
    return joinTablesHorizontally(fromTable, toTable, new IterableArray<DbColumnPair>(columnPairs));
  }

  public DbTable joinTablesHorizontally(DbTable leftTable, DbTable rightTable, Iterable<DbColumnPair> columnPairs)
  {
    return joinTablesHorizontally(leftTable, rightTable, columnPairs, this);
  }

  public DbTable joinTablesHorizontally(DbTable leftTable, DbTable rightTable, Iterable<DbColumnPair> columnPairs, DbJoinColumnNameHandler joinColumnNameHandler)
  {
    int leftColumnCount = leftTable.getColumnCount();
    int rightColumnCount = rightTable.getColumnCount();
    int newColumnCount = leftColumnCount + rightColumnCount;

    DbTable newTable = new DbTable();

    LinkedList<Integer> excludedColumns = new LinkedList<Integer>();
    for (int columnOffset = 0; columnOffset < newColumnCount; columnOffset++)
    {
      if (columnOffset < leftColumnCount)
      {
        defineColumn(ColumnPairType.FROM, leftTable, rightTable, newTable, columnOffset, columnOffset, columnPairs, excludedColumns, joinColumnNameHandler);
      }
      else
      {
        int rightOffset = columnOffset - leftColumnCount;
        defineColumn(ColumnPairType.TO, rightTable, leftTable, newTable, rightOffset, columnOffset, columnPairs, excludedColumns, joinColumnNameHandler);
      }
    }

    DbIndex leftIndex = leftTable.getIndexByColumnPairs(columnPairs, ColumnPairType.FROM);
    DbIndex rightIndex = rightTable.getIndexByColumnPairs(columnPairs, ColumnPairType.TO);

    int newRowOffset = 0;

    for (Entry<DbMultipleValue, Iterable<Integer>> leftKeyEntry : leftIndex.entrySet())
    {
      DbMultipleValue leftKey = leftKeyEntry.getKey();
      Iterable<Integer> leftRows = leftKeyEntry.getValue();
      Iterable<Integer> rightRows = rightIndex.get(leftKey);
      if (rightRows != null)
      {
        // rightRows is rows in rightTable that matched leftKey in leftTable
        for (Integer leftRowOffset : leftRows)
        {
          for (Integer rightRowOffset : rightRows)
          {
            int newColumnOffset = 0;
            for (int oldColumnOffset = 0; oldColumnOffset < newColumnCount; oldColumnOffset++)
            {
              Object value;
              if (!excludedColumns.contains(oldColumnOffset))
              {
                if (oldColumnOffset < leftColumnCount)
                {
                  value = leftTable.get(leftRowOffset, oldColumnOffset);
                }
                else
                {
                  int rightOffset = oldColumnOffset - leftColumnCount;
                  value = rightTable.get(rightRowOffset, rightOffset);
                }
                newTable.set(newRowOffset, newColumnOffset, value);
                newColumnOffset++;
              }
            }
            newRowOffset++;
          }
        }
      }
    }

    return newTable;
  }

  public DbTable joinTablesVertically(DbTable topTable, DbTable bottomTable, DbVerticalJoinType verticalJoinType)
  {
    int topColumnCount = topTable.getColumnCount();
    Iterable<String> topColumnNames = topTable.getColumnNames();
    Iterable<Integer> topColumnTypes = topTable.getColumnTypes();

    DbTable newTable = new DbTable(topColumnNames, topColumnTypes);

    int bottomColumnCount = bottomTable.getColumnCount();
    int[] bottomColumns = new int[bottomColumnCount];

    for (int bottomColumnOffset = 0; bottomColumnOffset < bottomColumnCount; bottomColumnOffset++)
    {
      String bottomColumnName = bottomTable.getColumnName(bottomColumnOffset);
      int columnOffset = topTable.getColumnOffset(bottomColumnName);
      if (columnOffset == -1)
      {
        int bottomColumnType = bottomTable.getColumnType(bottomColumnOffset);
        bottomColumns[bottomColumnOffset] = newTable.getColumnCount();
        newTable.addColumn(bottomColumnName, bottomColumnType);
      }
      else
      {
        // TODO: Handle columns with same name but different type
        bottomColumns[bottomColumnOffset] = columnOffset;
      }
    }

    int topRowCount = topTable.getRowCount();

    for (int topRowOffset = 0; topRowOffset < topRowCount; topRowOffset++)
    {
      for (int topColumnOffset = 0; topColumnOffset < topColumnCount; topColumnOffset++)
      {
        newTable.set(topRowOffset, topColumnOffset, topTable.get(topRowOffset, topColumnOffset));
      }
    }

    int bottomRowCount = bottomTable.getRowCount();
    int rowOffset = topRowCount;

    for (int bottomRowOffset = 0; bottomRowOffset < bottomRowCount; bottomRowOffset++)
    {
      for (int bottomColumnOffset = 0; bottomColumnOffset < bottomColumnCount; bottomColumnOffset++)
      {
        newTable.set(rowOffset, bottomColumns[bottomColumnOffset], bottomTable.get(bottomRowOffset, bottomColumnOffset));
      }
      rowOffset++;
    }

    return newTable;
  }

  public DbTable pivot(final DbTable oldTable, DbColumnGroup columnGroup, DbColumnGroup pivotGroup, Iterable<DbPivotSummarizer> pivotSummarizers)
  {
    return pivot(oldTable, columnGroup, pivotGroup, pivotSummarizers, new DbDefaultPivotColumnNameHandler());
  }

  public DbTable pivot(final DbTable oldTable, DbColumnGroup columnGroup, DbColumnGroup pivotGroup, Iterable<DbPivotSummarizer> pivotSummarizers, DbPivotColumnNameHandler pivotColumnNameHandler)
  {
    Iterable<String> columnGroupColumnNames = columnGroup.getColumnNames();
    Iterable<String> pivotGroupColumnNames = pivotGroup.getColumnNames();

    DbIndex index = oldTable.getIndexByColumnNames(columnGroupColumnNames);

    TreeSet<String> seriesColumnNames = new TreeSet<String>();
    TreeMap<DbMultipleValue, Iterable<PivotColumn>> pivotResults = new TreeMap<DbMultipleValue, Iterable<PivotColumn>>();

    for (Entry<DbMultipleValue, Iterable<Integer>> indexEntry : index.entrySet())
    {
      List<PivotColumn> pivotColumns = new LinkedList<PivotColumn>();
      for (String pivotColumnName : pivotGroupColumnNames)
      {
        // TODO: Evaluate performance impact of mapping name to offset repeatedly and identify alternatives (e.g. caching offset in name, etc.)
        int pivotColumnOffset = oldTable.getColumnOffset(pivotColumnName);
        pivotColumns.add(new PivotColumn(pivotColumnName, pivotColumnOffset, new TreeMap<Object, Iterable<NamedSummarizer>>()));
      }
      pivotResults.put(indexEntry.getKey(), pivotColumns);
      for (int rowOffset : indexEntry.getValue())
      {
        for (PivotColumn pivotColumn : pivotColumns)
        {
          int columnOffset = pivotColumn.getColumnOffset();
          Object seriesValue = oldTable.get(rowOffset, columnOffset);
          Map<Object, Iterable<NamedSummarizer>> pivotColumnSummarizers = pivotColumn.getPivotColumnSummarizers();
          Iterable<NamedSummarizer> namedSummarizers = pivotColumnSummarizers.get(seriesValue);
          if (namedSummarizers == null)
          {
            List<NamedSummarizer> summarizerList = new LinkedList<NamedSummarizer>();
            for (DbPivotSummarizer pivotSummarizer : pivotSummarizers)
            {
              DbSummarizer summarizer = createSummarizer(pivotSummarizer);
              String summarizerColumnName = pivotSummarizer.getColumnName();
              summarizer.initializePivot(oldTable, summarizerColumnName); // TODO: Consider alternatives (e.g. constructor, etc.)
              summarizer.reset();
              String summarizerName = pivotSummarizer.getSummarizerName();
              String seriesColumnName = pivotColumnNameHandler.getPivotColumnName(pivotColumn.getColumnName(), seriesValue, summarizerName, summarizerColumnName);
              seriesColumnNames.add(seriesColumnName);
              NamedSummarizer namedSummarizer = new NamedSummarizer(seriesColumnName, summarizer);
              summarizerList.add(namedSummarizer);
            }
            pivotColumnSummarizers.put(seriesValue, summarizerList);
            namedSummarizers = summarizerList;
          }
          for (NamedSummarizer namedSummarizer : namedSummarizers)
          {
            DbSummarizer summarizer = namedSummarizer.getSummarizer();
            summarizer.aggregate(rowOffset);
          }
        }
      }
    }

    DbTable newTable = new DbTable(columnGroupColumnNames);

    // Note: Use sorted column names because some rows may be missing values
    for (String seriesColumnName : seriesColumnNames)
    {
      newTable.addColumnName(seriesColumnName);
    }

    int rowOffset = 0;

    for (Entry<DbMultipleValue, Iterable<PivotColumn>> pivotResultsEntry : pivotResults.entrySet())
    {
      int columnGroupColumnOffset = 0;
      DbMultipleValue columnGroupMultipleValue = pivotResultsEntry.getKey();
      for (Object columnGroupColumnValue : columnGroupMultipleValue)
      {
        newTable.set(rowOffset, columnGroupColumnOffset++, columnGroupColumnValue);
      }
      Iterable<PivotColumn> pivotColumns = pivotResultsEntry.getValue();
      for (PivotColumn pivotColumn : pivotColumns)
      {
        Map<Object, Iterable<NamedSummarizer>> pivotColumnSummarizers = pivotColumn.getPivotColumnSummarizers();
        for (Entry<Object, Iterable<NamedSummarizer>> pivotColumnSummarizer : pivotColumnSummarizers.entrySet())
        {
          for (NamedSummarizer namedSummarizer : pivotColumnSummarizer.getValue())
          {
            String columnName = namedSummarizer.getColumnName();
            // Look up the column name because some rows may be missing values.
            int columnOffset = newTable.getColumnOffset(columnName); // may be faster to get this from the tree...
            DbSummarizer summarizer = namedSummarizer.getSummarizer();
            Object summarizerValue = summarizer.summarize();
            newTable.set(rowOffset, columnOffset, summarizerValue);
          }
        }
      }
      rowOffset++;
    }

    return newTable;
  }

  public DbTable selectColumns(DbTable oldTable, Iterable<String> newColumnNames)
  {
    Iterable<Integer> oldColumnOffsets = oldTable.getColumnOffsets(newColumnNames);
    Iterable<Integer> newColumnTypes = oldTable.getColumnTypes(oldColumnOffsets);
    DbTable newTable = new DbTable(newColumnNames, newColumnTypes);

    int rowCount = oldTable.getRowCount();

    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
    {
      int newColumnOffset = 0;
      for (int oldColumnOffset : oldColumnOffsets)
      {
        Object value = oldTable.get(rowOffset, oldColumnOffset);
        newTable.set(rowOffset, newColumnOffset++, value);
      }
    }

    return newTable;
  }

  public DbTable selectColumns(DbTable oldTable, Iterable<String> newColumnNames, Iterable<DbSortColumn> sortColumns)
  {
    Iterable<Integer> oldColumnOffsets = oldTable.getColumnOffsets(newColumnNames);
    Iterable<Integer> newColumnTypes = oldTable.getColumnTypes(oldColumnOffsets);
    DbTable newTable = new DbTable(newColumnNames, newColumnTypes);

    int newRowOffset = 0;
    DbIndex index = oldTable.getIndexBySortColumns(sortColumns);
    for (Entry<DbMultipleValue, Iterable<Integer>> entry : index.entrySet())
    {
      for (int oldRowOffset : entry.getValue())
      {
        int newColumnOffset = 0;
        for (int oldColumnOffset : oldColumnOffsets)
        {
          Object value = oldTable.get(oldRowOffset, oldColumnOffset);
          newTable.set(newRowOffset, newColumnOffset++, value);
        }
        newRowOffset++;
      }
    }

    return newTable;
  }

  public DbTable selectColumns(DbTable oldTable, String... newColumnNames)
  {
    return selectColumns(oldTable, new IterableArray<String>(newColumnNames));
  }

  public DbTable selectRows(DbTable oldTable, String columnName, DbCompareOperation compareOperation, Object... values)
  {
    Iterable<String> columnNames = oldTable.getColumnNames();
    Iterable<Integer> columnTypes = oldTable.getColumnTypes();
    DbTable newTable = new DbTable(columnNames, columnTypes);
    switch (compareOperation)
    {
      case Contains:
      {
        int columnOffset = oldTable.getColumnOffset(columnName);
        DbFullTextIndex fullTextIndex = oldTable.getFullTextIndex(columnOffset);
        String searchText = values[0].toString();
        fullTextIndex.search(searchText, new RowCollector(oldTable, newTable));
        break;
      }
      case LessThan:
      case LessThanOrEqualTo:
      case EqualTo:
      case GreaterThan:
      case GreaterThanOrEqualTo:
      case NotEqualTo:
      {
        DbIndex index = oldTable.getIndexByColumnNames(columnName);
        int columnOffset = oldTable.getColumnOffset(columnName);
        int columnType = oldTable.getColumnType(columnOffset);
        Object value = ColumnTypeConverter.toJavaType(columnType, values[0].toString());
        DbLiteralMultipleValue key = new DbLiteralMultipleValue(value);
        Iterable<Integer> rows = index.get(key, compareOperation);
        newTable.append(oldTable, rows);
        break;
      }
      default:
      {
        throw new UnsupportedOperationException();
      }
    }
    return newTable;
  }

  public DbTable sort(DbTable oldTable, DbSortColumn... sortColumns)
  {
    return sort(oldTable, new IterableArray<DbSortColumn>(sortColumns));
  }

  public DbTable sort(DbTable oldTable, Iterable<DbSortColumn> sortColumns)
  {
    int newRowOffset = 0;
    Iterable<String> columnNames = oldTable.getColumnNames();
    Iterable<Integer> columnTypes = oldTable.getColumnTypes();
    DbTable newTable = new DbTable(columnNames, columnTypes);
    int columnCount = oldTable.getColumnCount();

    DbIndex index = oldTable.getIndexBySortColumns(sortColumns);
    for (Entry<DbMultipleValue, Iterable<Integer>> entry : index.entrySet())
    {
      for (int oldRowOffset : entry.getValue())
      {
        for (int columnOffset = 0; columnOffset < columnCount; columnOffset++)
        {
          newTable.set(newRowOffset, columnOffset, oldTable.get(oldRowOffset, columnOffset));
        }
        newRowOffset++;
      }
    }

    return newTable;
  }

  public DbTable summarize(DbTable oldTable, DbColumnGroup columnGroup, DbSummarizer... summarizers)
  {
    return summarize(oldTable, columnGroup, new IterableArray<DbSummarizer>(summarizers));
  }

  public DbTable summarize(DbTable oldTable, DbColumnGroup columnGroup, Iterable<DbSummarizer> summarizers)
  {
    DbTable newTable = new DbTable();

    Iterable<String> columnNames = columnGroup.getColumnNames();
    for (String columnName : columnNames)
    {
      newTable.addColumnName(columnName);
      int columnOffset = oldTable.getColumnOffset(columnName);
      int columnType = oldTable.getColumnType(columnOffset);
      newTable.addColumnType(columnType);
    }
    for (DbSummarizer summarizer : summarizers)
    {
      summarizer.initialize(oldTable);
      String newColumnName = summarizer.getNewColumnName();
      newTable.addColumnName(newColumnName);
      int columnType = summarizer.getNewColumnType();
      newTable.addColumnType(columnType);
    }

    int newRowOffset = 0;
    DbIndex index = oldTable.getIndexByColumnNames(columnNames);
    for (Entry<DbMultipleValue, Iterable<Integer>> entry : index.entrySet())
    {
      for (DbSummarizer summarizer : summarizers)
      {
        summarizer.reset();
      }

      for (int rowOffset : entry.getValue())
      {
        for (DbSummarizer summarizer : summarizers)
        {
          summarizer.aggregate(rowOffset);
        }
      }

      int newColumnOffset = 0;
      DbMultipleValue key = entry.getKey();
      for (Object keyColumnValue : key)
      {
        newTable.set(newRowOffset, newColumnOffset++, keyColumnValue);
      }
      for (DbSummarizer summarizer : summarizers)
      {
        Object summarizerValue = summarizer.summarize();
        newTable.set(newRowOffset, newColumnOffset++, summarizerValue);
      }

      newRowOffset++;
    }

    return newTable;
  }

  public DbTable trend(DbTable table, DbTrendSpecification... trendSpecification)
  {
    return null;
  }

  public DbTable union(DbTable left, DbTable right)
  {
    return null;
  }

  private class NamedSummarizer
  {
    private String columnName;
    private DbSummarizer summarizer;

    public NamedSummarizer(String seriesColumnName, DbSummarizer summarizer)
    {
      this.columnName = seriesColumnName;
      this.summarizer = summarizer;
    }

    public final String getColumnName()
    {
      return columnName;
    }

    public final DbSummarizer getSummarizer()
    {
      return summarizer;
    }

  }

  public class PivotColumn
  {
    private String columnName;
    private int columnOffset;
    private Map<Object, Iterable<NamedSummarizer>> pivotColumnSummarizers;

    public PivotColumn(String pivotColumnName, Integer columnOffset, TreeMap<Object, Iterable<NamedSummarizer>> pivotColumnSummarizers)
    {
      this.columnName = pivotColumnName;
      this.columnOffset = columnOffset;
      this.pivotColumnSummarizers = pivotColumnSummarizers;
    }

    public final String getColumnName()
    {
      return columnName;
    }

    public final int getColumnOffset()
    {
      return columnOffset;
    }

    public final Map<Object, Iterable<NamedSummarizer>> getPivotColumnSummarizers()
    {
      return pivotColumnSummarizers;
    }

  }

  public final class RowCollector extends DbFullTextCollector
  {
    private int newRowOffset;
    private DbTable newTable;
    private DbTable oldTable;

    public RowCollector(DbTable oldTable, DbTable newTable)
    {
      this.oldTable = oldTable;
      this.newTable = newTable;
    }

    @Override
    public void collect(int docid) throws IOException
    {
      Document document = searcher.doc(docid);
      Field id = document.getField(DbFullTextIndex.ID);
      byte[] binaryValue = id.getBinaryValue();
      int rowOffset = DbFullTextIndex.getInt(binaryValue);
      //Field name = document.getField(DbFullTextIndex.VALUE);
      int columnCount = oldTable.getColumnCount();
      for (int columnOffset = 0; columnOffset < columnCount; columnOffset++)
      {
        newTable.set(newRowOffset, columnOffset, oldTable.get(rowOffset, columnOffset));
      }
      newRowOffset++;
    }
  }

}
