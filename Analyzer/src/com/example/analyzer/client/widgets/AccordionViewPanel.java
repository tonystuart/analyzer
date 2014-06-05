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
import com.example.analyzer.client.PropertyListStore;
import com.extjs.gxt.ui.client.data.ModelData;

// Note the subtle distinction between a configured user table id
// and a previous user table id, i.e. a change in the user table id
// only results in a reconfiguration if the view is visible or becomes
// visible. If the user switches to a new table and then back before
// the view becomes visible, or goes through a long sequence of switches
// without the view being visible, there is no reconfiguration.

public abstract class AccordionViewPanel extends ViewPanel
{
  protected PropertyListStore<ModelData> configuredTableListStore;
  protected int configuredUserTableId;
  protected PropertyListStore<ModelData> tableListStore;
  protected int userTableId;

  public AccordionViewPanel(String heading)
  {
    super(heading);
    configuredUserTableId = userTableId = Analyzer.EMPTY_TABLE;
    configuredTableListStore = tableListStore = Analyzer.getInstance().getTableListStore(userTableId);
  }

  private void checkTableSwitch()
  {
    if (configuredUserTableId != userTableId)
    {
      tableListStore = Analyzer.getInstance().getTableListStore(userTableId);

      reconfigure();
      updateFormState();

      configuredUserTableId = userTableId;
      configuredTableListStore = tableListStore;
    }
  }

  public void displayTableData(int userTableId)
  {
    this.userTableId = userTableId;
    if (isVisibleWithGxtWorkaround())
    {
      checkTableSwitch();
    }
  }

  public void onExpand()
  {
    System.out.println("AccordionViewPanel.onExpand");
    checkTableSwitch();
  }

  protected abstract void updateFormState();

  protected abstract void reconfigure();

}
