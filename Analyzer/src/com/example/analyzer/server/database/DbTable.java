// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.analyzer.shared.DbColumnPair;
import com.example.analyzer.shared.DbDirection;
import com.example.analyzer.shared.DbSortColumn;

/**
 * An interface to a huge table (bigger than virtual memory) that can be shared
 * across processors in a cluster, typically implemented using memory mapped
 * files.
 */

public class DbTable
{
  private static Object classDefaultValue = null;

  protected static Object getClassDefaultValue()
  {
    return classDefaultValue;
  }

  protected static void setClassDefaultValue(Object classDefaultValue)
  {
    DbTable.classDefaultValue = classDefaultValue;
  }

  private ArrayList<String> columnNames = new ArrayList<String>();
  private ArrayList<Integer> columnTypes = new ArrayList<Integer>();
  private Object defaultValue = classDefaultValue;
  private HashMap<Integer, DbFullTextIndex> fullTextIndexes = new HashMap<Integer, DbFullTextIndex>();
  private DbIndexes indexes = new DbIndexes(this);

  private ArrayList<Row> rows = new ArrayList<Row>();

  public DbTable()
  {
  }

  public DbTable(Iterable<String> columnNames)
  {
    for (String columnName : columnNames)
    {
      addColumnName(columnName);
    }
  }

  public DbTable(Iterable<String> columnNames, Iterable<Integer> columnTypes)
  {
    for (String columnName : columnNames)
    {
      this.columnNames.add(columnName);
    }
    for (Integer columnType : columnTypes)
    {
      this.columnTypes.add(columnType);
    }
    verifyColumns();
  }

  private void verifyColumns()
  {
    if (this.columnNames.size() != this.columnTypes.size())
    {
      throw new RuntimeException("Number of names and types do not match");
    }
  }

  public DbTable(String... columnNames)
  {
    this(new IterableArray<String>(columnNames));
  }

  public void addColumn(String columnName, int columnType)
  {
    verifyColumns();
    columnNames.add(columnName);
    columnTypes.add(columnType);
  }

  public int addColumnName(String columnName)
  {
    int columnOffset = columnNames.size();
    columnNames.add(columnName);
    return columnOffset;
  }

  public int addColumnType(int columnType)
  {
    int columnOffset = columnTypes.size();
    columnTypes.add(columnType);
    return columnOffset;
  }

  public void append(DbTable table, Iterable<Integer> oldRowOffsets)
  {
    int columnCount = table.getColumnCount();
    int newRowOffset = getRowCount();
    for (int oldRowOffset : oldRowOffsets)
    {
      for (int columnOffset = 0; columnOffset < columnCount; columnOffset++)
      {
        set(newRowOffset, columnOffset, table.get(oldRowOffset, columnOffset));
      }
      newRowOffset++;
    }
  }

  public Object coalesce(int rowOffset, int columnOffset)
  {
    return coalesce(rowOffset, columnOffset, defaultValue);
  }

  public Object coalesce(int rowOffset, int columnOffset, Object defaultValue)
  {
    Object value = get(rowOffset, columnOffset);
    if (value == null)
    {
      value = defaultValue;
    }
    return value;
  }

  public Object get(int rowOffset, int columnOffset)
  {
    return rows.get(rowOffset).get(columnOffset);
  }

  public int getColumnCount()
  {
    return columnNames.size();
  }

  public String getColumnName(int columnOffset)
  {
    return columnNames.get(columnOffset);
  }

  public Iterable<String> getColumnNames()
  {
    return columnNames;
  }

  public int getColumnOffset(String columnName)
  {
    return columnNames.indexOf(columnName);
  }

  public int getColumnOffsetNoCase(String columnName)
  {
    int columnCount = columnNames.size();
    for (int columnOffset = 0; columnOffset < columnCount; columnOffset++)
    {
      if (columnNames.get(columnOffset).equalsIgnoreCase(columnName))
      {
        return columnOffset;
      }
    }
    return -1;
  }

  public Iterable<Integer> getColumnOffsets(Iterable<String> columnNames)
  {
    return new IterableLinkedListAdapter<String, Integer>(columnNames)
    {
      @Override
      public Integer adapt(String columnName)
      {
        return getColumnOffset(columnName);
      }
    };
  }

  public int getColumnType(int columnOffset)
  {
    return columnTypes.get(columnOffset);
  }

  public Iterable<Integer> getColumnTypes()
  {
    return columnTypes;
  }

  public Iterable<Integer> getColumnTypes(Iterable<Integer> columnOffsets)
  {
    return new IterableLinkedListAdapter<Integer, Integer>(columnOffsets)
    {
      @Override
      public Integer adapt(Integer columnOffset)
      {
        return super.adapt(getColumnType(columnOffset));
      }
    };
  }

  protected Object getDefaultValue()
  {
    return defaultValue;
  }

  public DbFullTextIndex getFullTextIndex(int columnOffset)
  {
    DbFullTextIndex fullTextIndex = fullTextIndexes.get(columnOffset);
    if (fullTextIndex == null)
    {
      fullTextIndex = new DbFullTextIndex(this, columnOffset);
      fullTextIndexes.put(columnOffset, fullTextIndex);
    }
    return fullTextIndex;
  }

  public DbIndex getIndex(Iterable<DbIndexColumn> indexColumns)
  {
    return indexes.getColumnIndex(indexColumns);
  }

  public DbIndex getIndexByColumnNames(Iterable<String> columnNames)
  {
    return getIndex(new IterableLinkedListAdapter<String, DbIndexColumn>(columnNames)
    {
      @Override
      public DbIndexColumn adapt(String columnName)
      {
        int columnOffset = getColumnOffset(columnName);
        DbIndexColumn indexColumn = new DbIndexColumn(columnOffset);
        return indexColumn;
      }
    });
  }

  public DbIndex getIndexByColumnNames(String... columnNames)
  {
    return getIndexByColumnNames(new IterableArray<String>(columnNames));
  }

  public DbIndex getIndexByColumnPairs(Iterable<DbColumnPair> columnPairs, ColumnPairType type)
  {
    return getIndex(new ColumnPairAdapter(columnPairs, type));
  }

  public DbIndex getIndexBySortColumns(Iterable<DbSortColumn> sortColumns)
  {
    return getIndex(new IterableLinkedListAdapter<DbSortColumn, DbIndexColumn>(sortColumns)
    {
      @Override
      public DbIndexColumn adapt(DbSortColumn sortColumn)
      {
        String columnName = sortColumn.getColumnName();
        int columnOffset = getColumnOffset(columnName);
        DbDirection dbDirection = sortColumn.getDirection();
        DbIndexColumn indexColumn = new DbIndexColumn(columnOffset, dbDirection);
        return indexColumn;
      }
    });
  }

  public int getRowCount()
  {
    return rows.size();
  }

  public void set(int rowOffset, int columnOffset, Object value)
  {
    int size = rows.size();
    if (size == rowOffset)
    {
      rows.add(new Row(columnOffset, value));
    }
    else if (size > rowOffset)
    {
      rows.get(rowOffset).set(columnOffset, value);
    }
    else
    {
      throw new UnsupportedOperationException();
    }
  }

  protected void setDefaultValue(Object defaultValue)
  {
    this.defaultValue = defaultValue;
  }

  @Override
  public String toString()
  {
    int rowCount = getRowCount();
    int columnCount = getColumnCount();
    StringBuilder s = new StringBuilder();
    for (int columnOffset = 0; columnOffset < columnCount; columnOffset++)
    {
      if (columnOffset > 0)
      {
        s.append("|");
      }
      s.append(columnNames.get(columnOffset));
    }
    s.append("\n");
    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
    {
      for (int columnOffset = 0; columnOffset < columnCount; columnOffset++)
      {
        if (columnOffset > 0)
        {
          s.append("|");
        }
        s.append(get(rowOffset, columnOffset));
      }
      s.append("\n");
    }
    s.append("columnIndexes=" + indexes);
    s.append("\n");

    return s.toString();
  }

  public class ColumnPairAdapter extends IterableLinkedListAdapter<DbColumnPair, DbIndexColumn>
  {
    private ColumnPairType type;

    public ColumnPairAdapter(Iterable<DbColumnPair> columnPairs, ColumnPairType type)
    {
      this.type = type;
      adaptAll(columnPairs);
    }

    @Override
    public DbIndexColumn adapt(DbColumnPair sortColumn)
    {
      String columnName;
      switch (type)
      {
        case FROM:
          columnName = sortColumn.getFromColumnName();
          break;
        case TO:
          columnName = sortColumn.getToColumnName();
          break;
        default:
          throw new UnsupportedOperationException();
      }
      int columnOffset = getColumnOffset(columnName);
      DbIndexColumn indexColumn = new DbIndexColumn(columnOffset);
      return indexColumn;
    }
  }

  public enum ColumnPairType
  {
    FROM, TO
  }

  public class Row
  {
    private ArrayList<Object> columns = new ArrayList<Object>();

    public Row(int columnOffset, Object value)
    {
      set(columnOffset, value);
    }

    public Object get(int columnOffset)
    {
      return columnOffset < columns.size() ? columns.get(columnOffset) : defaultValue;
    }

    public void set(int columnOffset, Object value)
    {
      int size = columns.size();
      if (size == columnOffset)
      {
        columns.add(value);
      }
      else if (columnOffset < size)
      {
        columns.set(columnOffset, value);
      }
      else
      {
        for (int i = size; i < columnOffset; i++)
        {
          columns.add(defaultValue);
        }
        columns.add(value);
      }
    }
  }

}
