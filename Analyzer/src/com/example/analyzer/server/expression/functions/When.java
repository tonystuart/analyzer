// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expression.functions;

import java.util.ArrayList;

import com.example.analyzer.server.expression.Evaluator;
import com.example.analyzer.server.expression.Function;
import com.example.analyzer.server.expression.Value;
import com.example.analyzer.server.expression.ValueType;

public class When extends Function
{
  public When(Evaluator evaluator)
  {
    super(evaluator, 2);
  }

  @Override
  public Value evaluate(ArrayList<Value> arguments)
  {
    Value returnValue = Value.NULL;
    Value condition = evaluator.getValue(arguments.get(0));
    if (condition.getType() != ValueType.NULL)
    {
      if (condition.getBoolean())
      {
        Value result = evaluator.getValue(arguments.get(1));
        if (result.getType() != ValueType.NULL)
        {
          returnValue = result;
        }
      }
    }
    return returnValue;
  }

}
