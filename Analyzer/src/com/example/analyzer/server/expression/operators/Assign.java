// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expression.operators;

import com.example.analyzer.server.expression.Evaluator;
import com.example.analyzer.server.expression.EvaluatorException;
import com.example.analyzer.server.expression.Operator;
import com.example.analyzer.server.expression.Value;
import com.example.analyzer.server.expression.ValueType;
import com.example.analyzer.server.expression.Variable;

public class Assign extends Operator
{
  public Assign(Evaluator evaluator)
  {
    super(":=", evaluator);
  }

  public final Value evaluate(Value leftSubexpression, Value rightSubexpression)
  {
    if (leftSubexpression instanceof Value)
    {
      Value value = (Value)leftSubexpression;
      if (value.getType() == ValueType.VARIABLE)
      {
        Variable variable = (Variable)value.getValue();
        variable.setValue(rightSubexpression);
        return rightSubexpression;
      }
    }
    throw new EvaluatorException("Expected variable name, received " + leftSubexpression);
  }
}
