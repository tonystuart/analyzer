// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.shared;

import java.io.Serializable;

public class ColumnDescriptor implements Serializable
{
  private String columnName;
  private int columnType;

  public ColumnDescriptor()
  {
  }

  public ColumnDescriptor(String columnName, int columnType)
  {
    this.columnName = columnName;
    this.columnType = columnType;
  }

  public String getColumnName()
  {
    return columnName;
  }

  public void setColumnName(String name)
  {
    this.columnName = name;
  }

  public int getColumnType()
  {
    return columnType;
  }

  public void setColumnType(int type)
  {
    this.columnType = type;
  }

}
