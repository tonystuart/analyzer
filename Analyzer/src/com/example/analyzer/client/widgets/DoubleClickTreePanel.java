// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import java.util.List;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;

public class DoubleClickTreePanel<M extends ModelData> extends TreePanel<M>
{
  private boolean isEnableDoubleClickExpandCollapse;

  public DoubleClickTreePanel(TreeStore<M> store)
  {
    super(store);
  }

  public boolean isEnableDoubleClickExpandCollapse()
  {
    return isEnableDoubleClickExpandCollapse;
  }

  @SuppressWarnings("rawtypes")
  protected void onDoubleClick(TreePanelEvent tpe)
  {
    if (isEnableDoubleClickExpandCollapse)
    {
      super.onDoubleClick(tpe);
    }
  }

  public void setEnableDoubleClickExpandCollapse(boolean isEnableDoubleClickExpandCollapse)
  {
    this.isEnableDoubleClickExpandCollapse = isEnableDoubleClickExpandCollapse;
  }

  public void expand(String key, Object value)
  {
    expand(null, key, value);
  }

  public void expand(M parent, String key, Object value)
  {
    List<M> children = parent == null ? store.getRootItems() : store.getChildren(parent);
    for (M child : children)
    {
      setExpanded(child, true, false);
      if (!child.get(key).equals(value))
      {
        expand(child, key, value);
      }
    }
  }

}
