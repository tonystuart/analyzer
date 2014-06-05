// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;

import java.util.LinkedList;
import java.util.List;

public class DbName
{
  public static final String DEFAULT_DELIMITER = "/";
  public static final DbName DEFAULT_NAME = new DbName();

  private String name;
  private DbName parent;
  private List<DbName> children;

  public DbName()
  {
  }

  public DbName(String name)
  {
    this.name = name;
  }

  public DbName(DbName parent, DbName child)
  {
    this(parent, child.getName());
  }

  public DbName(DbName parent, String name)
  {
    this.name = name;
    parent.addChild(this);
  }

  public String toString()
  {
    return getPath();
  }

  @Override
  public int hashCode()
  {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((children == null) ? 0 : children.hashCode());
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    result = prime * result + ((parent == null) ? 0 : parent.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj)
  {
    if (this == obj)
    {
      return true;
    }
    if (obj == null)
    {
      return false;
    }
    if (getClass() != obj.getClass())
    {
      return false;
    }
    DbName other = (DbName)obj;
    if (children == null)
    {
      if (other.children != null)
      {
        return false;
      }
    }
    else if (!children.equals(other.children))
    {
      return false;
    }
    if (name == null)
    {
      if (other.name != null)
      {
        return false;
      }
    }
    else if (!name.equals(other.name))
    {
      return false;
    }
    if (parent == null)
    {
      if (other.parent != null)
      {
        return false;
      }
    }
    else if (!parent.equals(other.parent))
    {
      return false;
    }
    return true;
  }

  public final DbName getRoot()
  {
    DbName root;
    if (parent == null)
    {
      root = this;
    }
    else
    {
      root = parent.getRoot();
    }
    return root;
  }

  public final String getPath()
  {
    return getPath(DEFAULT_DELIMITER);
  }

  public final String getPath(String delimiter)
  {
    StringBuilder s = new StringBuilder();
    for (DbName name = this; name != null; name = name.getParent())
    {
      if (s.length() > 0)
      {
        s.insert(0, delimiter);
      }
      s.insert(0, name.name);
    }
    return s.toString();
  }

  public final DbName getParent()
  {
    return parent;
  }

  public final void setParent(DbName parent)
  {
    this.parent = parent;
  }

  public final String getName()
  {
    return name;
  }

  public final void setName(String name)
  {
    this.name = name;
  }

  public final Iterable<DbName> getChildren()
  {
    return getChildrenList();
  }

  protected final List<DbName> getChildrenList()
  {
    if (children == null)
    {
      children = new LinkedList<DbName>();
    }
    return children;
  }

  public final void addChild(DbName child)
  {
    DbName oldParent = child.getParent();
    if (oldParent != null)
    {
      oldParent.removeChild(child);
    }
    getChildrenList().add(child);
    child.setParent(this);
  }

  public final void removeChild(DbName child)
  {
    getChildrenList().remove(child);
  }

}
