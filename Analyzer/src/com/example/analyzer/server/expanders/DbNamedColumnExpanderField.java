// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expanders;

public class DbNamedColumnExpanderField
{
  private String name;
  private DbColumnExpanderField expanderField;

  public DbNamedColumnExpanderField(String name, DbColumnExpanderField expanderField)
  {
    this.name = name;
    this.expanderField = expanderField;
  }

  public String getName()
  {
    return name;
  }

  public String getTargetName(String sourceColumnName, boolean isUpperCase)
  {
    if (isUpperCase)
    {
      name = name.toUpperCase();
    }
    String targetColumnName = sourceColumnName + "." + name;
    return targetColumnName;
  }

  public DbColumnExpanderField getExpanderField()
  {
    return expanderField;
  }

}
