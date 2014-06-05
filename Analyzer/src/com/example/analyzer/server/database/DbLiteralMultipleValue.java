// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;

import java.util.Iterator;


public class DbLiteralMultipleValue extends DbMultipleValue
{
  private Iterable<Object> values;

  public DbLiteralMultipleValue(Object... values)
  {
    this(new IterableArray<Object>(values));
  }

  public DbLiteralMultipleValue(Iterable<Object> values)
  {
    this.values = values;
  }

  @Override
  public Iterator<Object> iterator()
  {
    return values.iterator();
  }
}
