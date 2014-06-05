// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;

import java.math.BigDecimal;
import java.sql.Types;

public class DbTotalSummarizer extends DbColumnSummarizer
{
  private BigDecimal sum;

  public DbTotalSummarizer()
  {
    
  }
  
  public DbTotalSummarizer(String oldColumnName, String newColumnName)
  {
    super(oldColumnName, newColumnName);
  }

  @Override
  public void aggregate(int rowOffset)
  {
    Object value = table.get(rowOffset, columnOffset);
    if (value instanceof BigDecimal)
    {
      sum = sum.add((BigDecimal)value);
    }
    else
    {
      sum = sum.add(new BigDecimal(value.toString()));
    }
  }

  @Override
  public int getNewColumnType()
  {
    return Types.DECIMAL;
  }

  @Override
  public void reset()
  {
    sum = new BigDecimal(0);
  }

  @Override
  public Object summarize()
  {
    return sum;
  }

  @Override
  public String toString()
  {
    return "[" + super.toString() + ", sum=" + sum + "]";
  }

}
