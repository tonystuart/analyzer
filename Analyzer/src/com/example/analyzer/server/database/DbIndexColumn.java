// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;

import com.example.analyzer.shared.DbDirection;

public class DbIndexColumn implements Comparable<DbIndexColumn>
{
  private int columnOffset;
  private DbDirection dbDirection;

  public DbIndexColumn(int columnOffset)
  {
    this(columnOffset, DbDirection.ASCENDING);
  }

  public DbIndexColumn(int columnOffset, DbDirection dbDirection)
  {
    this.columnOffset = columnOffset;
    this.dbDirection = dbDirection;
  }

  public final int getColumnOffset()
  {
    return columnOffset;
  }

  public final DbDirection getDirection()
  {
    return dbDirection;
  }

  @Override
  public int compareTo(DbIndexColumn that)
  {
    int delta = this.columnOffset - that.columnOffset;
    if (delta != 0)
    {
      return delta;
    }
    delta = this.dbDirection.compareTo(that.dbDirection);
    return delta;
  }

}
