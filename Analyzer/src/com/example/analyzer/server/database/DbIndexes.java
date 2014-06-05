// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;

import java.util.TreeMap;


public class DbIndexes
{
  private DbTable table;
  private TreeMap<Iterable<DbIndexColumn>, DbIndex> indexes = new TreeMap<Iterable<DbIndexColumn>, DbIndex>(new IterableComparator<DbIndexColumn>());

  public DbIndexes(DbTable table)
  {
    this.table = table;
  }

  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();
    s.append("[");
    IterableArray.toString(indexes.values());
    s.append("]");
    return s.toString();
  }

  public DbIndex getColumnIndex(Iterable<DbIndexColumn> indexColumns)
  {
    DbIndex index = indexes.get(indexColumns);
    if (index == null)
    {
      index = new DbIndex(table, indexColumns);
      indexes.put(indexColumns, index);
    }
    return index;
  }

}
