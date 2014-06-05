// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expression;

import java.util.ArrayList;

public abstract class Function
{
  protected Evaluator evaluator;
  protected int minimumArgumentCount;
  protected int maximumArgumentCount;

  public Function(Evaluator evaluator, int argumentCount)
  {
    this(evaluator, argumentCount, argumentCount);
  }

  public Function(Evaluator evaluator, int minimumArgumentCount, int maximumArgumentCount)
  {
    this.evaluator = evaluator;
    this.minimumArgumentCount = minimumArgumentCount;
    this.maximumArgumentCount = maximumArgumentCount;
  }

  public abstract Value evaluate(ArrayList<Value> argumentValues);
  
  public int getMinimumArgumentCount()
  {
    return minimumArgumentCount;
  }

  public int getMaximumArgumentCount()
  {
    return maximumArgumentCount;
  }

}
