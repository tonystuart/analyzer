// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expanders.date.fields;

import java.sql.Types;
import java.util.GregorianCalendar;

import com.example.analyzer.server.database.DbDateUtilities;
import com.example.analyzer.server.expanders.DbColumnExpanderField;

public final class Day_of_Week implements DbColumnExpanderField
{
  @Override
  public Object getValue(Object sourceValue)
  {
    return DbDateUtilities.getOffsetIntoWeek((GregorianCalendar)sourceValue);
  }

  @Override
  public int getType()
  {
    return Types.INTEGER;
  }
}
