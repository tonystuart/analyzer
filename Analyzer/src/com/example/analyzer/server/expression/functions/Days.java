// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expression.functions;

import java.util.GregorianCalendar;

import com.example.analyzer.server.database.DbDateUtilities;
import com.example.analyzer.server.expression.AbstractCalendarFunction;
import com.example.analyzer.server.expression.Evaluator;
import com.example.analyzer.server.expression.Value;
import com.example.analyzer.server.expression.ValueType;

public class Days extends AbstractCalendarFunction
{
  public Days(Evaluator evaluator)
  {
    super(evaluator);
  }

  @Override
  public Value extractValue(GregorianCalendar calendar)
  {
    int days = DbDateUtilities.getDayOfEpoch(calendar);
    return new Value(days, ValueType.INTEGER);
  }

}
