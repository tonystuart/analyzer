// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.jdbc.shared;

import java.io.Serializable;

public class Table implements Serializable
{
  private String[] columnNames;
  private int[] columnTypes;
  private String[][] data;

  public Table()
  {
  }

  public Table(String[] columnNames, int[] columnTypes, String[][] data)
  {
    this.columnNames = columnNames;
    this.columnTypes = columnTypes;
    this.data = data;
  }

  public final String[] getColumnNames()
  {
    return columnNames;
  }

  public final void setColumnNames(String[] columnNames)
  {
    this.columnNames = columnNames;
  }

  public final int[] getColumnTypes()
  {
    return columnTypes;
  }

  public final void setColumnTypes(int[] columnTypes)
  {
    this.columnTypes = columnTypes;
  }

  public final String[][] getData()
  {
    return data;
  }

  public final void setData(String[][] data)
  {
    this.data = data;
  }

  public String getColumnName(int columnIndex)
  {
    return columnNames[columnIndex];
  }

  public int getRowCount()
  {
    return data.length;
  }

  public int getColumnCount()
  {
    return columnNames.length;
  }

  public String get(int rowIndex, int columnIndex)
  {
    return data[rowIndex][columnIndex];
  }

  public Object getValueAt(int rowIndex, int columnIndex)
  {
    Object typedValue = null;
    String stringValue = data[rowIndex][columnIndex];
    switch (columnTypes[columnIndex])
    {
      case ColumnTypes.STRING:
        typedValue = stringValue;
        break;
      case ColumnTypes.INTEGER:
        typedValue = parseInt(stringValue);
        break;
      case ColumnTypes.LONG:
        typedValue = parseLong(stringValue);
        break;
      case ColumnTypes.DECIMAL:
        typedValue = parseDouble(stringValue); // Change to BigDecimal when GWT supports it
        break;
      case ColumnTypes.DOUBLE:
        typedValue = parseDouble(stringValue);
        break;
      case ColumnTypes.DATE:
        typedValue = stringValue; // Right now there is no portable and reliable way to do this
        break;
      default:
    }
    return typedValue;
  }

  private Integer parseInt(String stringValue)
  {
    Integer value = null;
    try
    {
      value = Integer.valueOf(stringValue);
    }
    catch (Exception e)
    {

    }
    return value;
  }

  private Long parseLong(String stringValue)
  {
    Long value = null;
    try
    {
      value = Long.valueOf(stringValue);
    }
    catch (Exception e)
    {

    }
    return value;
  }

  private Double parseDouble(String stringValue)
  {
    Double value = null;
    try
    {
      value = Double.valueOf(stringValue);
    }
    catch (Exception e)
    {

    }
    return value;
  }

}
