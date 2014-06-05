// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class ManagedTabItem extends TabItem implements ManagedItem
{
  private TablePanel tablePanel;
  private boolean isLayoutRequired = true;

  public ManagedTabItem(ViewManager viewManager, TablePanel tablePanel)
  {
    super(tablePanel.getHeading());
    this.tablePanel = tablePanel;
    setLayout(new FitLayout());
    setClosable(true);
    add(tablePanel);
    addListener(Events.Select, new Listener<TabPanelEvent>()
    {
      @Override
      public void handleEvent(TabPanelEvent be)
      {
        if (isLayoutRequired)
        {
          isLayoutRequired = false;
          System.out.println("ManagedTabItem.select: Invoking doLayout(true)");
          doLayout(true);
        }
        else
        {
          System.out.println("ManagedTabItem.select: Skipping doLayout(true)");
        }
      }
    });
  }

  @Override
  public void activate()
  {
    add(tablePanel);
    tablePanel.setSize(getOffsetWidth(), getOffsetHeight());
    isLayoutRequired = true;
  }

  @Override
  public void deactivate()
  {
    // WindowWrapper will adopt(viewPanel) efficiently on add
  }

  @Override
  public String getHeading()
  {
    return tablePanel.getHeading();
  }

  public int getUserTableId()
  {
    return tablePanel.getUserTableId();
  }

  @Override
  public ViewPanel getViewPanel()
  {
    return tablePanel;
  }

  @Override
  public boolean isCollapsed()
  {
    boolean isCollapsed = !isVisible();
    return isCollapsed;
  }

}
