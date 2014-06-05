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

public class DbSortColumn implements Serializable
{
  private String columnName;
  private DbDirection dbDirection;

  public DbSortColumn()
  {
  }

  public DbSortColumn(String columnName)
  {
    this(columnName, DbDirection.ASCENDING);
  }

  public DbSortColumn(String columnName, DbDirection dbDirection)
  {
    this.columnName = columnName;
    this.dbDirection = dbDirection;
  }

  @Override
  public String toString()
  {
    return columnName + " " + dbDirection;
  }

  public final String getColumnName()
  {
    return columnName;
  }

  public final DbDirection getDirection()
  {
    return dbDirection;
  }

}
