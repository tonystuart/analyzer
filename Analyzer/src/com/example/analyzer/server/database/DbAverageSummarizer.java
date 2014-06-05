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
import java.math.RoundingMode;

public class DbAverageSummarizer extends DbTotalSummarizer
{
  private int count;

  public DbAverageSummarizer()
  {
  }

  public DbAverageSummarizer(String oldColumnName, String newColumnName)
  {
    super(oldColumnName, newColumnName);
  }

  @Override
  public String toString()
  {
    return "[" + super.toString() + ", count=" + count + "]";
  }

  public void reset()
  {
    super.reset();
    count = 0;
  }

  @Override
  public void aggregate(int rowOffset)
  {
    super.aggregate(rowOffset);
    count++;
  }

  @Override
  public Object summarize()
  {
    BigDecimal average;
    if (count == 0)
    {
      average = BigDecimal.valueOf(0);
    }
    else
    {
      BigDecimal total = (BigDecimal)super.summarize();
      BigDecimal divisor = BigDecimal.valueOf(count);
      average = total.divide(divisor, 2, RoundingMode.HALF_UP);
    }
    return average;
  }

}
