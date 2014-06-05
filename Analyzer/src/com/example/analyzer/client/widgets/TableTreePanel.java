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
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelData;

public class TableTreePanel extends ToolTipTreePanel<ModelData>
{
  public TableTreePanel()
  {
    super(Analyzer.getInstance().getHistoryMergeTreeStore(), Keys.TOOL_TIP);
    setBorders(true);
    setStyleAttribute("background-color", "white");
    setDisplayProperty(Keys.NAME);
    getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
  }

  public ModelData getValue()
  {
    return getSelectionModel().getSelectedItem();
  }

  /**
   * Select a table in the history tree and populate the column name drop-down
   * box.
   */
  public void selectTableInHistory(int userTableId)
  {
    ModelData modelData = Analyzer.getInstance().getHistoryMergeTreeStore().findModel(Keys.USER_TABLE_ID, userTableId);
    if (modelData != null)
    {
      selectTableInHistory(modelData);
    }
  }

  /**
   * Select a table in the history tree and populate the column name drop-down
   * box.
   */
  public void selectTableInHistory(ModelData modelData)
  {
    getSelectionModel().select(modelData, false);
    scrollIntoView(modelData);
  }
}
