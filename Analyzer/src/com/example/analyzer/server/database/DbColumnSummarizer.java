// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;

public abstract class DbColumnSummarizer implements DbSummarizer
{
  protected int columnOffset;
  protected String newColumnName;
  protected String oldColumnName;
  protected DbTable table;

  public DbColumnSummarizer()
  {

  }

  public DbColumnSummarizer(String oldColumnName, String newColumnName)
  {
    this.oldColumnName = oldColumnName;
    this.newColumnName = newColumnName;
  }

  @Override
  public String getNewColumnName()
  {
    return newColumnName;
  }

  @Override
  public void initialize(DbTable table)
  {
    this.table = table;
    columnOffset = table.getColumnOffset(oldColumnName);
  }

  @Override
  public void initializePivot(DbTable table, String oldColumnName)
  {
    this.table = table;
    this.oldColumnName = oldColumnName;
    columnOffset = table.getColumnOffset(oldColumnName);
  }

}
