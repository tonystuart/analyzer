// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;

import java.sql.Types;

public class DbCountSummarizer implements DbSummarizer
{
  private int count;
  private String newColumnName;

  public DbCountSummarizer()
  {
  }

  public DbCountSummarizer(String newColumnName)
  {
    this.newColumnName = newColumnName;
  }

  @Override
  public void aggregate(int rowOffset)
  {
    count++;
  }

  @Override
  public String getNewColumnName()
  {
    return newColumnName;
  }

  @Override
  public int getNewColumnType()
  {
    return Types.INTEGER;
  }

  @Override
  public void initialize(DbTable table)
  {
  }

  @Override
  public void initializePivot(DbTable table, String oldColumnName)
  {
  }

  @Override
  public void reset()
  {
    count = 0;
  }

  @Override
  public Object summarize()
  {
    return count;
  }

  @Override
  public String toString()
  {
    return "[newColumnName=" + newColumnName + ", count=" + count + "]";
  }

}
