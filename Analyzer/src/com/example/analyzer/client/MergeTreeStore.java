// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client;

import java.util.Map;

import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.TreeStore;

public class MergeTreeStore extends TreeStore<ModelData>
{
  private String nameId;

  public MergeTreeStore()
  {
  }

  public MergeTreeStore(String nameId)
  {
    this.nameId = nameId;
  }

  public void merge(BaseTreeModel newTree)
  {
    // NB: Process inserts new items in alphabetical order
    process(null, newTree);
  }

  public void process(ModelData oldParent, BaseTreeModel newParent)
  {
    int oldIndex = 0;
    while (oldIndex < getChildCount(oldParent))
    {
      ModelData oldChild = getChild(oldParent, oldIndex);
      int newIndex = findChild(newParent, oldChild);
      if (newIndex == -1)
      {
        remove(oldParent, oldChild);
      }
      else
      {
        process(oldChild, (BaseTreeModel)newParent.getChild(newIndex));
        oldIndex++;
      }
    }

    int newIndex = 0;
    while (newIndex < newParent.getChildCount())
    {
      ModelData newChild = newParent.getChild(newIndex);
      oldIndex = findChild(oldParent, newChild);
      if (oldIndex == -1)
      {
        if (nameId == null)
        {
          add(oldParent, newChild, true);
        }
        else
        {
          // add in alphabetical order
          int index = findInsertPoint(oldParent, newChild);
          insert(oldParent, newChild, index, true);
        }
      }
      else
      {
        newIndex++;
      }
    }
  }

  /**
   * Root aware version of base class remove method.
   */

  @Override
  public void remove(ModelData parent, ModelData child)
  {
    if (parent == null)
    {
      remove(child);
    }
    else
    {
      super.remove(parent, child);
    }
  }

  /**
   * Root aware version of base class add method.
   */

  @Override
  public void add(ModelData parent, ModelData item, boolean addChildren)
  {
    if (parent == null)
    {
      add(item, addChildren);
    }
    else
    {
      super.add(parent, item, addChildren);
    }
  }

  /**
   * Root aware version of base class insert method.
   */
  public void insert(ModelData parent, ModelData item, int index, boolean addChildren)
  {
    if (parent == null)
    {
      insert(item, index, addChildren);
    }
    else
    {
      super.insert(parent, item, index, addChildren);
    }
  }

  public int findInsertPoint(ModelData parent, ModelData item)
  {
    String itemName = item.get(nameId);
    int itemCount = getChildCount(parent);
    for (int itemOffset = 0; itemOffset < itemCount; itemOffset++)
    {
      ModelData child = getChild(parent, itemOffset);
      String childName = child.get(nameId);
      if (Utilities.compareAlphaNumerically(childName, itemName) > 0)
      {
        return itemOffset;
      }
    }
    return itemCount;
  }

  public int findChild(BaseTreeModel newParent, ModelData oldChild)
  {
    int childCount = newParent.getChildCount();
    for (int childIndex = 0; childIndex < childCount; childIndex++)
    {
      ModelData newChild = newParent.getChild(childIndex);
      if (isEqual(oldChild, newChild))
      {
        return childIndex;
      }
    }
    return -1;
  }

  private int findChild(ModelData oldParent, ModelData newChild)
  {
    int childCount = getChildCount(oldParent);
    for (int childIndex = 0; childIndex < childCount; childIndex++)
    {
      ModelData oldChild = getChild(oldParent, childIndex);
      if (isEqual(oldChild, newChild))
      {
        return childIndex;
      }
    }
    return -1;
  }

  public boolean isEqual(ModelData oldChild, ModelData newChild)
  {
    Map<String, Object> oldChildProperties = oldChild.getProperties();
    int oldSize = oldChildProperties.size();
    Map<String, Object> newChildProperties = newChild.getProperties();
    int newSize = newChildProperties.size();
    if (newSize != oldSize)
    {
      return false;
    }
    for (String propertyName : newChildProperties.keySet())
    {
      Object oldValue = oldChildProperties.get(propertyName);
      Object newValue = newChildProperties.get(propertyName);
      if (oldValue == null || newValue == null || !newValue.equals(oldValue))
      {
        return false;
      }
    }
    return true;
  }

  public ModelData find(String property, Object value)
  {
    return find(null, property, value);
  }

  public ModelData find(ModelData parent, String property, Object value)
  {
    int childCount = getChildCount(parent);
    for (int childOffset = 0; childOffset < childCount; childOffset++)
    {
      ModelData childNode = getChild(parent, childOffset);
      if (value.equals(childNode.get(property)))
      {
        return childNode;
      }
      else
      {
        ModelData item = find(childNode, property, value);
        if (item != null)
        {
          return item;
        }
      }
    }
    return null;
  }

}
