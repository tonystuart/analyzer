// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expression.functions;

import java.sql.Date;
import java.util.ArrayList;

import com.example.analyzer.server.expression.Evaluator;
import com.example.analyzer.server.expression.Function;
import com.example.analyzer.server.expression.Value;
import com.example.analyzer.server.expression.ValueType;

public class Today extends Function
{
  public Today(Evaluator evaluator)
  {
    super(evaluator, 0);
  }

  @Override
  public Value evaluate(ArrayList<Value> arguments)
  {
    Value returnValue = new Value(new Date(System.currentTimeMillis()), ValueType.DATE);
    return returnValue;
  }

}
