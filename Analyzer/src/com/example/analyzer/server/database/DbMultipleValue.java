// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;



public abstract class DbMultipleValue extends IterableComparator<Object> implements Comparable<DbMultipleValue>, Iterable<Object>
{
  @Override
  public String toString()
  {
    return IterableArray.toString(this);
  }

  @Override
  public int compareTo(DbMultipleValue that)
  {
    return compare(this, that);
  }
}
