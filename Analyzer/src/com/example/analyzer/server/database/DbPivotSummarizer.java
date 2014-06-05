// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;

public class DbPivotSummarizer
{
  private String summarizerName;
  private String columnName;
  private Class<? extends DbSummarizer> summarizerClass;

  public DbPivotSummarizer(String summarizerName, String columnName, Class<? extends DbSummarizer> summarizerClass)
  {
    this.summarizerName = summarizerName;
    this.columnName = columnName;
    this.summarizerClass = summarizerClass;
  }

  public String getSummarizerName()
  {
    return summarizerName;
  }

  public final String getColumnName()
  {
    return columnName;
  }

  public final Class<? extends DbSummarizer> getSummarizerClass()
  {
    return summarizerClass;
  }

}
