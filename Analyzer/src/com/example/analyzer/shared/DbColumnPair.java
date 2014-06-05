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

public class DbColumnPair implements Serializable
{
  private String fromColumnName;
  private String toColumnName;

  public DbColumnPair()
  {
  }

  public DbColumnPair(String fromColumnName, String toColumnName)
  {
    this.fromColumnName = fromColumnName;
    this.toColumnName = toColumnName;
  }

  @Override
  public String toString()
  {
    return fromColumnName + " " + toColumnName;
  }

  public String getFromColumnName()
  {
    return fromColumnName;
  }

  public void setFromColumnName(String fromColumnName)
  {
    this.fromColumnName = fromColumnName;
  }

  public String getToColumnName()
  {
    return toColumnName;
  }

  public void setToColumnName(String toColumnName)
  {
    this.toColumnName = toColumnName;
  }

}
