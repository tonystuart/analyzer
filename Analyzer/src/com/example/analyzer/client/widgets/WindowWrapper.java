// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import com.example.analyzer.client.Analyzer;
import com.extjs.gxt.ui.client.core.XDOM;
import com.extjs.gxt.ui.client.event.IconButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.ToolButton;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;

public class WindowWrapper extends Window
{
  private Rectangle restoreBounds;
  private ViewManager viewManager;
  private ViewPanel viewPanel;

  public WindowWrapper(ViewManager viewManager, ViewPanel viewPanel)
  {
    this.viewManager = viewManager;
    this.viewPanel = viewPanel;
    setIcon(viewPanel.getIcon());
    setClosable(false);
    getHeader().addTool(new ToolButton("x-tool-minimize", new MinimizeListener()));
    restoreBounds = new Rectangle((XDOM.getBody().getOffsetWidth() - Analyzer.DEFAULT_POPUP_WIDTH) / 2, (XDOM.getBody().getOffsetHeight() - Analyzer.DEFAULT_POPUP_HEIGHT) / 2, Analyzer.DEFAULT_POPUP_WIDTH, Analyzer.DEFAULT_POPUP_HEIGHT);
    setBounds(restoreBounds);
    setAnimCollapse(false);
    setHeading(viewPanel.getHeading());
    setLayout(new FitLayout());
    setLayoutOnChange(true);
  }

  public void activate()
  {
    add(viewPanel);
    setFocusWidget(viewPanel.getFocusWidget());
    if (viewPanel instanceof AccordionViewPanel)
    {
      System.out.println("WindowWrapper.activate: invoking onExpand");
      ((AccordionViewPanel)viewPanel).onExpand();
    }
  }

  public void deactivate()
  {
    // This is the only reliable way to prevent expensive grid redrawing during morph out
    remove(viewPanel);
  }

  public Rectangle getMorphInBounds()
  {
    return restoreBounds;
  }

  public ViewPanel getViewPanel()
  {
    return viewPanel;
  }

  public void morphOut()
  {
    viewManager.morphOut(this);
  }

  public Rectangle saveMorphInBounds()
  {
    restoreBounds = getBounds(false);
    return restoreBounds;
  }

  public class MinimizeListener extends SelectionListener<IconButtonEvent>
  {
    @Override
    public void componentSelected(IconButtonEvent ce)
    {
      morphOut();
    }
  }
}
