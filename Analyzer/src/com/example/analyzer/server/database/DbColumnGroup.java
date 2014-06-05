// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;



public class DbColumnGroup
{
  private Iterable<String> columnNames;

  public DbColumnGroup(String... columnNames)
  {
    this(new IterableArray<String>(columnNames));
  }

  public DbColumnGroup(Iterable<String> columnNames)
  {
    this.columnNames = columnNames;
  }

  public final Iterable<String> getColumnNames()
  {
    return columnNames;
  }

}
