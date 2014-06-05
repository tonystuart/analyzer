// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expanders.uszipcode;

import java.sql.Types;
import java.util.LinkedList;
import java.util.Random;

import com.example.analyzer.jdbc.shared.ColumnTypes;
import com.example.analyzer.server.expanders.DbBaseColumnExpander;
import com.example.analyzer.server.expanders.DbBaseColumnExpanderContext;
import com.example.analyzer.server.expanders.DbColumnExpander;
import com.example.analyzer.server.expanders.DbColumnExpanderContext;
import com.example.analyzer.server.expanders.DbIdentityColumnExpanderField;
import com.example.analyzer.server.expanders.DbIdentityNamedColumnExpanderField;
import com.example.analyzer.server.expanders.DbNamedColumnExpanderField;

// See http://en.wikipedia.org/wiki/ISO_8601

public class DbUsZipCodeColumnExpander extends DbBaseColumnExpander implements DbColumnExpander
{
  public Random random = new Random();

  public DbUsZipCodeColumnExpander()
  {
    super("US ZIP CODE", ColumnTypes.STRING);

    add(new Random_Field(random, 10, 18, 1), "Education", "In years");
    add(new Random_Field(random, 1, 10, 1), "Ethnicity", "In census designation");
    add(new Random_Field(random, 20, 40, 5), "Median_Age", "In years");
    add(new Random_Field(random, 30000, 80000, 5000), "Median_Income", "In dollars per year");
    add(new Random_Field(random, 2, 4, 1), "Median_Household_Size", "In people");
    add(new Random_Field(random, 50, 500, 1), "Population_Density", "In people per square mile");
  }

  @Override
  public DbColumnExpanderContext configure(String columnName, Iterable<String> fieldNames)
  {
    random.setSeed(columnName.hashCode());
    LinkedList<DbNamedColumnExpanderField> fields = getNamedColumnExpanderFields(columnName, fieldNames);
    fields.push(new DbIdentityNamedColumnExpanderField(columnName, new DbIdentityColumnExpanderField(Types.DATE)));
    DbDateColumnExpanderContext dateColumnExpanderContext = new DbDateColumnExpanderContext(columnName, fields);
    return dateColumnExpanderContext;
  }

  public class DbDateColumnExpanderContext extends DbBaseColumnExpanderContext
  {
    public DbDateColumnExpanderContext(String columnName, LinkedList<DbNamedColumnExpanderField> fields)
    {
      super(columnName, fields);
    }
  }
}
