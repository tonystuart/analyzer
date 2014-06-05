// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.store.TreeStore;

public class DoubleClickToolTipTreePanel<M extends ModelData> extends ToolTipTreePanel<M>
{
  private boolean isEnableDoubleClickExpandCollapse;

  public DoubleClickToolTipTreePanel(TreeStore<M> store, String toolTipPropertyName)
  {
    super(store, toolTipPropertyName);
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

}
