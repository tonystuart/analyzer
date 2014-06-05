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

public class DbPercentCountValueSummarizer extends DbCountValueSummarizer
{
  public static final int DEFAULT_SCALE = 2;
  
  private int total;
  private int scale;

  public DbPercentCountValueSummarizer(String oldColumnName, String newColumnName, Object value)
  {
    this(oldColumnName, newColumnName, value, DEFAULT_SCALE);
  }

  public DbPercentCountValueSummarizer(String oldColumnName, String newColumnName, Object value, int scale)
  {
    super(oldColumnName, newColumnName, value);
    this.scale = scale;
  }

  @Override
  public String toString()
  {
    return "[" + super.toString() + ", total=" + total + ", scale=" + scale + "]";
  }

  @Override
  public void reset()
  {
    super.reset();
    total = 0;
  }

  @Override
  public void aggregate(int rowOffset)
  {
    super.aggregate(rowOffset);
    total++;
  }

  @Override
  public Object summarize()
  {
    BigDecimal percent;
    int count = (Integer)super.summarize();
    if (count == 0 || total == 0)
    {
      percent = BigDecimal.valueOf(0);
    }
    else
    {
      percent = BigDecimal.valueOf(count * 100).divide(BigDecimal.valueOf(total), scale, RoundingMode.HALF_UP);
    }
    return percent;
  }

}
