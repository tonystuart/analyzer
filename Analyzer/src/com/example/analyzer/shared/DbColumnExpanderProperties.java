// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.shared;

import java.io.Serializable;
import java.util.HashMap;

public class DbColumnExpanderProperties implements Serializable
{
  private HashMap<String, String> properties = new HashMap<String, String>();
  private String expanderName;
  private int columnType;

  public DbColumnExpanderProperties()
  {
  }

  public DbColumnExpanderProperties(String expanderName, int columnType)
  {
    this.expanderName = expanderName;
    this.columnType = columnType;
  }

  public void addProperty(String name, String description)
  {
    properties.put(name, description);
  }

  public HashMap<String, String> getProperties()
  {
    return properties;
  }

  public String getExpanderName()
  {
    return expanderName;
  }

  public int getColumnType()
  {
    return columnType;
  }

  
}
