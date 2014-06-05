// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expanders;

import java.util.HashMap;
import java.util.LinkedList;

import com.example.analyzer.shared.DbColumnExpanderProperties;

public class DbBaseColumnExpander
{
  protected DbColumnExpanderProperties columnExpanderProperties;
  protected HashMap<String, DbNamedColumnExpanderField> expanderFields = new HashMap<String, DbNamedColumnExpanderField>();

  public DbBaseColumnExpander(String expanderName, int columnType)
  {
    this.columnExpanderProperties = new DbColumnExpanderProperties(expanderName, columnType);
  }

  protected void add(DbColumnExpanderField expanderField, String fieldName, String fieldDescription)
  {
    DbNamedColumnExpanderField namedColumnExpanderField = new DbNamedColumnExpanderField(fieldName, expanderField);
    expanderFields.put(fieldName, namedColumnExpanderField);
    columnExpanderProperties.addProperty(fieldName, fieldDescription);
  }

  protected LinkedList<DbNamedColumnExpanderField> getNamedColumnExpanderFields(String columnName, Iterable<String> fieldNames)
  {
    LinkedList<DbNamedColumnExpanderField> fields = new LinkedList<DbNamedColumnExpanderField>();
    for (String fieldName : fieldNames)
    {
      DbNamedColumnExpanderField namedColumnExpanderField = expanderFields.get(fieldName);
      if (namedColumnExpanderField == null)
      {
        throw new UnsupportedOperationException("Field " + fieldName + " is not supported");
      }
      fields.add(namedColumnExpanderField);
    }
    return fields;
  }

  public DbColumnExpanderProperties getColumnExpanderProperties()
  {
    return columnExpanderProperties;
  }

}
