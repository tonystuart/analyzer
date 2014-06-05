// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expanders;

import java.util.LinkedList;

import com.example.analyzer.server.database.DbUtilities;
import com.example.analyzer.server.database.IterableArray;

public abstract class DbBaseColumnExpanderContext implements DbColumnExpanderContext
{
  private String columnName;
  private String[] columnNames;
  private Integer[] columnTypes;

  private LinkedList<DbNamedColumnExpanderField> expanderFields;
  private LinkedList<Object> nullValues;

  public DbBaseColumnExpanderContext(String columnName, LinkedList<DbNamedColumnExpanderField> fields)
  {
    this.columnName = columnName;
    this.expanderFields = fields;
  }

  @Override
  public int getColumnCount()
  {
    return expanderFields.size();
  }

  @Override
  public String getColumnName()
  {
    return columnName;
  }

  @Override
  public Iterable<String> getColumnNames()
  {
    if (columnNames == null)
    {
      int offset = 0;
      boolean isUpperCase = DbUtilities.isUpperCase(columnName);
      int columnCount = getColumnCount();
      columnNames = new String[columnCount];
      for (DbNamedColumnExpanderField expanderField : expanderFields)
      {
        String name = expanderField.getTargetName(columnName, isUpperCase);
        columnNames[offset++] = name;
      }
    }
    return new IterableArray<String>(columnNames);
  }

  @Override
  public Iterable<Integer> getColumnTypes()
  {
    if (columnTypes == null)
    {
      int offset = 0;
      int columnCount = getColumnCount();
      columnTypes = new Integer[columnCount];
      for (DbNamedColumnExpanderField expanderField : expanderFields)
      {
        columnTypes[offset++] = expanderField.getExpanderField().getType();
      }
    }
    return new IterableArray<Integer>(columnTypes);
  }

  @Override
  public Iterable<Object> getColumnValues(Object sourceValue)
  {
    if (sourceValue == null)
    {
      return getNullValues();
    }

    int offset = 0;
    int columnCount = getColumnCount();
    Object[] columnValues = new Object[columnCount];
    for (DbNamedColumnExpanderField expanderField : expanderFields)
    {
      columnValues[offset++] = expanderField.getExpanderField().getValue(sourceValue);
    }

    return new IterableArray<Object>(columnValues);
  }

  private Iterable<Object> getNullValues()
  {
    if (nullValues == null)
    {
      nullValues = new LinkedList<Object>();
      int columnCount = getColumnCount();
      for (int i = 0; i < columnCount; i++)
      {
        nullValues.add(null);
      }
    }
    return nullValues;
  }
}
