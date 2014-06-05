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

public class Concatenate extends Function
{
  public Concatenate(Evaluator evaluator)
  {
    super(evaluator, 2, Integer.MAX_VALUE);
  }

  @Override
  public Value evaluate(ArrayList<Value> arguments)
  {
    StringBuilder s = new StringBuilder();
    for (Value argument : arguments)
    {
      Value stringValue = evaluator.toString(argument);
      String string = stringValue.getString();
      if (string != null)
      {
        s.append(string);
      }
    }
    return new Value(s.toString(), ValueType.STRING);
  }

}
