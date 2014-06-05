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

public class Substr extends Function
{
  public Substr(Evaluator evaluator)
  {
    super(evaluator, 2, 3);
  }

  @Override
  public Value evaluate(ArrayList<Value> arguments)
  {
    Value returnValue = Value.NULL;
    Value stringValue = evaluator.getValue(arguments.get(0));
    if (stringValue.getType() != ValueType.NULL)
    {
      Value startPositionValue = evaluator.getValue(arguments.get(1));
      int startPosition = startPositionValue.getInteger();
      String string = evaluator.toString(stringValue).getString();
      int startOffset = startPosition - 1;
      int length = string.length() - startOffset;
      if (arguments.size() == 3)
      {
        Value lengthValue = arguments.get(2);
        length = lengthValue.getInteger();
      }
      String substring = string.substring(startOffset, startOffset+length);
      returnValue = new Value(substring, ValueType.STRING);
    }
    return returnValue;
  }

}
