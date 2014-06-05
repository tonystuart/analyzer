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
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.example.analyzer.shared.DbCompareOperation;
import com.example.analyzer.shared.DbDirection;

/**
 * An interface to a huge index (bigger than virtual memory) that can be shared
 * across processors in a cluster, typically implemented using memory mapped
 * files. Represents a mapping of one or more column values to rows that contain
 * those values. The rows are handled internally using a List, but as we may not
 * wish to implement List semantics in the disk based version, they are returned
 * outside this class as an Iterable.
 */
public class DbIndex
{

  private DbTable table;
  private Iterable<DbIndexColumn> indexColumns;

  private List<Integer> nullList = new LinkedList<Integer>();
  private TreeMap<DbMultipleValue, List<Integer>> index = new TreeMap<DbMultipleValue, List<Integer>>();

  public DbIndex(DbTable table, Iterable<DbIndexColumn> indexColumns)
  {
    this.table = table;
    this.indexColumns = indexColumns;

    int rowCount = table.getRowCount();
    for (int rowOffset = 0; rowOffset < rowCount; rowOffset++)
    {
      DbColumnMultipleValue value = new DbColumnMultipleValue(rowOffset);
      List<Integer> rowList = index.get(value);
      if (rowList == null)
      {
        rowList = new LinkedList<Integer>();
        index.put(value, rowList);
      }
      rowList.add(rowOffset);
    }
  }

  @Override
  public String toString()
  {
    StringBuilder s = new StringBuilder();
    s.append("[");
    s.append(IterableArray.toString(indexColumns));
    s.append(", n=");
    s.append(index.size());
    s.append("]");
    return s.toString();
  }

  public int size()
  {
    return index.size();
  }

  public Iterable<Integer> get(DbMultipleValue key)
  {
    return get(key, DbCompareOperation.EqualTo);
  }

  @SuppressWarnings("unchecked")
  public Iterable<Integer> get(DbMultipleValue key, DbCompareOperation compareOperation)
  {
    Iterable<Integer> iterable;
    if (key == null)
    {
      iterable = nullList;
    }
    else
    {
      switch (compareOperation)
      {
        case LessThan:
          iterable = new IterableMapList<DbMultipleValue, Integer>(index.headMap(key, false));
          break;
        case LessThanOrEqualTo:
          iterable = new IterableMapList<DbMultipleValue, Integer>(index.headMap(key, true));
          break;
        case EqualTo:
          iterable = index.get(key);
          break;
        case GreaterThan:
          iterable = new IterableMapList<DbMultipleValue, Integer>(index.tailMap(key, false));
          break;
        case GreaterThanOrEqualTo:
          iterable = new IterableMapList<DbMultipleValue, Integer>(index.tailMap(key, true));
          break;
        case NotEqualTo:
          iterable = new IterableMapList<DbMultipleValue, Integer>(index.headMap(key, false), index.tailMap(key, false));
          break;
        default:
          throw new UnsupportedOperationException();
      }
    }
    return iterable;
  }

  public Iterable<Entry<DbMultipleValue, Iterable<Integer>>> entrySet()
  {
    Set<Entry<DbMultipleValue, List<Integer>>> indexIterable = index.entrySet();
    return new ListIterableAdapter(indexIterable); // TODO: Make sure this still works now that we have migrated to external IterableAdapter
  }

  public class DbColumnMultipleValue extends DbMultipleValue
  {
    private int rowOffset;

    public DbColumnMultipleValue(int rowOffset)
    {
      this.rowOffset = rowOffset;
    }

    @Override
    public Iterator<Object> iterator()
    {
      return new DbMultipleColumnValueIterator(indexColumns.iterator());
    }

    public class DbMultipleColumnValueIterator implements Iterator<Object>
    {
      private Iterator<DbIndexColumn> iterator;
      private InvertedComparable<Object> invertedComparable = new InvertedComparable<Object>();

      public DbMultipleColumnValueIterator(Iterator<DbIndexColumn> iterator)
      {
        this.iterator = iterator;
      }

      @Override
      public boolean hasNext()
      {
        return iterator.hasNext();
      }

      @Override
      public Object next()
      {
        DbIndexColumn indexColumn = iterator.next();
        int columnOffset = indexColumn.getColumnOffset();
        Object value = table.get(rowOffset, columnOffset);
        DbDirection dbDirection = indexColumn.getDirection();
        if (dbDirection == DbDirection.DESCENDING)
        {
          value = invertedComparable.wrap(value);
        }
        return value;
      }

      @Override
      public void remove()
      {
        throw new UnsupportedOperationException();
      }

      public final class InvertedComparable<T> implements Comparable<T>
      {
        private T value;

        @Override
        @SuppressWarnings("unchecked")
        public final int compareTo(T o)
        {
          if (value == null)
          {
            if (((InvertedComparable<T>)o).value == null)
            {
              return 0;
            }
            return 1; // inverted in-order
          }
          else if (((InvertedComparable<T>)o).value == null)
          {
            return -1; // inverted out-of-order
          }
          else
          {
            return -((Comparable<T>)value).compareTo(((InvertedComparable<T>)o).value);
          }
        }

        public final InvertedComparable<T> wrap(T value)
        {
          this.value = value;
          return this;
        }

      }
    }
  }

  /**
   * Adapts Iterable<Entry<DbMultipleValue, List<Integer>>> to
   * Iterable<Entry<DbMultipleValue, Iterable<Integer>>>
   */
  public class ListIterableAdapter extends IterableAdapter<Entry<DbMultipleValue, List<Integer>>, Entry<DbMultipleValue, Iterable<Integer>>>
  {
    public ListIterableAdapter(Iterable<Entry<DbMultipleValue, List<Integer>>> fromIterable)
    {
      super(fromIterable);
    }

    @Override
    public Entry<DbMultipleValue, Iterable<Integer>> adapt(Entry<DbMultipleValue, List<Integer>> fromValue)
    {
      Entry<DbMultipleValue, Iterable<Integer>> newEntry = new EntryAdapter(fromValue);
      return newEntry;
    }
  }

  /**
   * Adapts Entry<DbMultipleColumnValue, List<Integer>> to
   * Entry<DbMultipleColumnValue, Iterable<Integer>>
   */
  public class EntryAdapter implements Entry<DbMultipleValue, Iterable<Integer>>
  {
    private Entry<DbMultipleValue, List<Integer>> entry;

    public EntryAdapter(Entry<DbMultipleValue, List<Integer>> entry)
    {
      this.entry = entry;
    }

    @Override
    public Iterable<Integer> setValue(Iterable<Integer> value)
    {
      throw new UnsupportedOperationException();
    }

    @Override
    public Iterable<Integer> getValue()
    {
      return entry.getValue(); // all this work for this!
    }

    @Override
    public DbMultipleValue getKey()
    {
      return entry.getKey();
    }
  }

}
