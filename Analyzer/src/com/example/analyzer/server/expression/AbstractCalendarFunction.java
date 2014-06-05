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
import java.util.Date;
import java.util.GregorianCalendar;

public abstract class AbstractCalendarFunction extends Function
{
  public AbstractCalendarFunction(Evaluator evaluator)
  {
    super(evaluator, 1);
  }

  @Override
  public Value evaluate(ArrayList<Value> arguments)
  {
    Value returnValue = Value.NULL;
    Value argument = arguments.get(0);
    Value dateValue = evaluator.toDate(argument);
    if (dateValue.getType() != ValueType.NULL)
    {
      Date date = dateValue.getDate();
      GregorianCalendar calendar = new GregorianCalendar();
      calendar.setTime(date);
      returnValue = extractValue(calendar);
    }
    return returnValue;
  }

  public abstract Value extractValue(GregorianCalendar calendar);
}
