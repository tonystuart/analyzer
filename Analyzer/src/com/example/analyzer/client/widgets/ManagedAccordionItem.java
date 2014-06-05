// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class ManagedAccordionItem extends ContentPanel implements ManagedItem
{
  private ViewManager viewManager;
  private ViewPanel viewPanel;

  public ManagedAccordionItem(ViewManager viewManager, ViewPanel viewPanel)
  {
    this.viewManager = viewManager;
    this.viewPanel = viewPanel;
    setIcon(viewPanel.getIcon());
    getHeader().addTool(new ToolButton("x-tool-restore", new RestoreListener()));
    setAnimCollapse(false); // defaults to true, pre-render
    setHeading(viewPanel.getHeading());
    setLayout(new FitLayout());
    setLayoutOnChange(true);
    add(viewPanel);
    addListener(Events.Expand, new ExpandListener());
  }

  public void activate()
  {
    add(viewPanel);
    viewPanel.setSize(getInnerWidth(), getInnerHeight());
  }

  public void deactivate()
  {
    // WindowWrapper will adopt(viewPanel) efficiently on add
  }

  @Override
  public String getHeading()
  {
    return viewPanel.getHeading();
  }

  public ViewPanel getViewPanel()
  {
    return viewPanel;
  }

  public void morphIn()
  {
    viewManager.morphIn(this);
  }

  private final class ExpandListener implements Listener<BaseEvent>
  {
    @Override
    public void handleEvent(BaseEvent be)
    {
      if (viewPanel instanceof AccordionViewPanel)
      {
        ((AccordionViewPanel)viewPanel).onExpand();
      }
    }
  }

  public class RestoreListener extends SelectionListener<IconButtonEvent>
  {
    @Override
    public void componentSelected(IconButtonEvent ce)
    {
      morphIn();
    }
  }

}
