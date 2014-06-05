// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expanders.uszipcode;

import java.sql.Types;
import java.util.Random;

import com.example.analyzer.server.expanders.DbColumnExpanderField;

public class Random_Field implements DbColumnExpanderField
{
  private Random random;
  private int rangeHigh;
  private int rangeLow;
  private int increment;

  public Random_Field(Random random, int rangeLow, int rangeHigh, int increment)
  {
    this.random = random;
    this.rangeLow = rangeLow;
    this.rangeHigh = rangeHigh;
    this.increment = increment;
  }

  @Override
  public int getType()
  {
    return Types.INTEGER;
  }

  @Override
  public Object getValue(Object sourceValue)
  {
    return rangeLow + (random.nextInt((rangeHigh - rangeLow) / increment) * increment);
  }

}
