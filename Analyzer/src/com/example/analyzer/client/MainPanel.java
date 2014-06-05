// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client;

import com.example.analyzer.client.widgets.ManagedAccordionPanel;
import com.example.analyzer.client.widgets.ManagedTabPanel;
import com.extjs.gxt.ui.client.Style.LayoutRegion;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BorderLayout;
import com.extjs.gxt.ui.client.widget.layout.BorderLayoutData;
import com.google.gwt.user.client.Element;

public class MainPanel extends LayoutContainer
{
  private ManagedAccordionPanel managedAccordionPanel;
  private ManagedTabPanel managedTabPanel;

  private static MainPanel instance;

  public static MainPanel getInstance()
  {
    return MainPanel.instance;
  }

  private void setInstance(MainPanel mainPanel)
  {
    MainPanel.instance = mainPanel;
  }

  public MainPanel()
  {
    setInstance(this);
  }

  protected void onRender(Element target, int index)
  {
    super.onRender(target, index);
    setLayout(new BorderLayout());

    managedAccordionPanel = new ManagedAccordionPanel();
    managedAccordionPanel.hide();
    BorderLayoutData westData = new BorderLayoutData(LayoutRegion.WEST, 300);
    westData.setSplit(true);
    westData.setCollapsible(true);
    westData.setMargins(new Margins(0, 5, 0, 0));
    add(managedAccordionPanel, westData);
    
    managedTabPanel = new ManagedTabPanel();
    BorderLayoutData centerData = new BorderLayoutData(LayoutRegion.CENTER);
    centerData.setMargins(new Margins(0));
    add(managedTabPanel, centerData);
  }

  public void showViewManager()
  {
    managedAccordionPanel.show();
    managedAccordionPanel.layout(true);
  }
}
