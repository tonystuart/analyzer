// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expanders.date;

import java.sql.Types;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.LinkedList;

import com.example.analyzer.jdbc.shared.ColumnTypes;
import com.example.analyzer.server.expanders.DbBaseColumnExpander;
import com.example.analyzer.server.expanders.DbBaseColumnExpanderContext;
import com.example.analyzer.server.expanders.DbColumnExpander;
import com.example.analyzer.server.expanders.DbColumnExpanderContext;
import com.example.analyzer.server.expanders.DbIdentityNamedColumnExpanderField;
import com.example.analyzer.server.expanders.DbNamedColumnExpanderField;
import com.example.analyzer.server.expanders.date.fields.Day_Name;
import com.example.analyzer.server.expanders.date.fields.Day_of_Month;
import com.example.analyzer.server.expanders.date.fields.Day_of_Week;
import com.example.analyzer.server.expanders.date.fields.Month_Name;
import com.example.analyzer.server.expanders.date.fields.Month_of_Year;
import com.example.analyzer.server.expanders.date.fields.Quarter_of_Year;
import com.example.analyzer.server.expanders.date.fields.Week_of_Month;
import com.example.analyzer.server.expanders.date.fields.Week_of_Year;
import com.example.analyzer.server.expanders.date.fields.Year;

// See http://en.wikipedia.org/wiki/ISO_8601

public class DbDateColumnExpander extends DbBaseColumnExpander implements DbColumnExpander
{
  public DbDateColumnExpander()
  {
    super("DATE", ColumnTypes.DATE);
    
    add(new Day_Name(), "Day_Name", "In current locale");
    add(new Day_of_Month(), "Day_of_Month", "Range 1 - 31");
    add(new Day_of_Week(), "Day_of_Week", "Monday (1) - Sunday (7)");
    add(new Month_Name(), "Month_Name", "In current locale");
    add(new Month_of_Year(), "Month_of_Year", "Range 1 - 12");
    add(new Quarter_of_Year(), "Quarter_of_Year", "Range 1 - 4");
    add(new Week_of_Month(), "Week_of_Month", "Range 1 - 5");
    add(new Week_of_Year(), "Week_of_Year", "Range 1 - 53");
    add(new Year(), "Year", "Range 1 - 9999");
  }

  @Override
  public DbColumnExpanderContext configure(String columnName, Iterable<String> fieldNames)
  {
    LinkedList<DbNamedColumnExpanderField> fields = getNamedColumnExpanderFields(columnName, fieldNames);
    fields.push(new DbIdentityNamedColumnExpanderField(columnName, new DbDateIdentityColumnExpanderField(Types.DATE)));
    DbDateColumnExpanderContext dateColumnExpanderContext = new DbDateColumnExpanderContext(columnName, fields);
    return dateColumnExpanderContext;
  }

  public class DbDateColumnExpanderContext extends DbBaseColumnExpanderContext
  {
    public DbDateColumnExpanderContext(String columnName, LinkedList<DbNamedColumnExpanderField> fields)
    {
      super(columnName, fields);
    }

    @Override
    public Iterable<Object> getColumnValues(Object sourceValue)
    {
      if (sourceValue != null)
      {
        // Perform calendar initialization just once for all fields in the row
        Date date = (Date)sourceValue;
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        sourceValue = calendar;
      }
      return super.getColumnValues(sourceValue);
    }
  }

}
