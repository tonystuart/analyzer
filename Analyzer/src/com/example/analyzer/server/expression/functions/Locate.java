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

public class Locate extends Function
{
  public Locate(Evaluator evaluator)
  {
    super(evaluator, 2, 3);
  }

  @Override
  public Value evaluate(ArrayList<Value> arguments)
  {
    Value returnValue = Value.NULL;
    Value needle = evaluator.getValue(arguments.get(0));
    if (needle.getType() != ValueType.NULL)
    {
      Value haystack = evaluator.getValue(arguments.get(1));
      int fromPosition = 1;
      if (arguments.size() == 3)
      {
        Value startPosition = arguments.get(2);
        fromPosition = startPosition.getInteger();
      }
      String haystackString = evaluator.toString(haystack).getString();
      int foundPosition;
      if (haystackString.length() == 0)
      {
        // Handle special (weird) case defined for SQL LOCATE()
        foundPosition = fromPosition;
      }
      else
      {
        String needleString = evaluator.toString(needle).getString();
        foundPosition = haystackString.indexOf(needleString, fromPosition - 1) + 1;
      }
      returnValue = new Value(foundPosition, ValueType.INTEGER);
    }
    return returnValue;
  }

}
