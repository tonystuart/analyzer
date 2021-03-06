// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expanders;


public class DbIdentityColumnExpanderField implements DbColumnExpanderField
{
  private int type;

  public DbIdentityColumnExpanderField(int type)
  {
    this.type = type;
  }

  @Override
  public Object getValue(Object sourceValue)
  {
    return sourceValue;
  }

  @Override
  public int getType()
  {
    return type;
  }

}
