// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expression.operators;

import java.math.BigDecimal;

import com.example.analyzer.server.expression.Evaluator;
import com.example.analyzer.server.expression.Operator;
import com.example.analyzer.server.expression.Value;

public class Divide extends Operator
{
  public Divide(Evaluator evaluator)
  {
    super("/", evaluator);
  }

  public final Value evaluate(Value leftSubexpression, Value rightSubexpression)
  {
    return evaluator.toNumber(leftSubexpression).divide(evaluator.toNumber(rightSubexpression), BigDecimal.ROUND_HALF_UP);
  }
}
