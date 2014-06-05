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
import java.util.HashSet;

public class DbCountDistinctSummarizer extends DbColumnSummarizer
{
  private HashSet<Object> values; // TODO: Use whatever approach DbColumnIndex uses to support a huge index

  public DbCountDistinctSummarizer(String oldColumnName, String newColumnName)
  {
    super(oldColumnName, newColumnName);
  }

  @Override
  public void aggregate(int rowOffset)
  {
    Object value = table.get(rowOffset, columnOffset);
    values.add(value);
  }

  @Override
  public int getNewColumnType()
  {
    return Types.INTEGER;
  }

  @Override
  public void reset()
  {
    values = new HashSet<Object>(); // generally more efficient than clear();
  }

  @Override
  public Object summarize()
  {
    return values == null ? 0 : values.size();
  }

  @Override
  public String toString()
  {
    return "[" + super.toString() + ", count=" + summarize() + "]";
  }

}
