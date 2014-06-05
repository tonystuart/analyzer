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

public abstract class AbstractStringFunction extends Function
{
  public AbstractStringFunction(Evaluator evaluator)
  {
    super(evaluator, 1);
  }

  @Override
  public Value evaluate(ArrayList<Value> arguments)
  {
    Value returnValue = Value.NULL;
    Value argument = arguments.get(0);
    Value stringValue = evaluator.toString(argument);
    if (stringValue.getType() != ValueType.NULL)
    {
      returnValue = extractValue(stringValue.getString());
    }
    return returnValue;
  }

  public abstract Value extractValue(String string);
}
