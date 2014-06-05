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

public class DbCountValueSummarizer extends DbColumnSummarizer
{
  private int count;
  private Object value;

  public DbCountValueSummarizer(String oldColumnName, String newColumnName, Object value)
  {
    super(oldColumnName, newColumnName);
    this.value = value;
  }

  @Override
  public void aggregate(int rowOffset)
  {
    Object value = table.get(rowOffset, columnOffset);
    if (this.value.equals(value))
    {
      count++;
    }
  }

  @Override
  public int getNewColumnType()
  {
    return Types.INTEGER;
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
    return "[" + super.toString() + ", value=" + value + ", count=" + count + "]";
  }

}
