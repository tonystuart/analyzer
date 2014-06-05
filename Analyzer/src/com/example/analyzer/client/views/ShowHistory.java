// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.views;

import java.util.LinkedList;
import java.util.List;

import com.example.analyzer.client.Analyzer;
import com.example.analyzer.client.Resources;
import com.example.analyzer.client.TableTypeIconProvider;
import com.example.analyzer.client.widgets.MergeTreePanel;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.shared.HistoryContent;
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.google.gwt.user.client.Element;

public class ShowHistory extends MergeTreePanel<HistoryContent>
{
  private static ShowHistory instance;

  public static ShowHistory getInstance()
  {
    return ShowHistory.instance;
  }

  private Button clearAllButton;
  private Button clearButton;
  private Button displayButton;
  private Label tableCountLabel;

  public ShowHistory(String heading)
  {
    super(heading, Analyzer.getInstance().getHistoryMergeTreeStore());
    setInstance(this);
    setIcon(Resources.CHART_ORGANIZATION);
  }

  private void clearAllHistory()
  {
    mergeTreeStore.removeAll();
    Analyzer.getInstance().clearAllHistory();
  }

  private void clearHistory()
  {
    LinkedList<Integer> userTableIds = new LinkedList<Integer>();
    for (ModelData selectedItem : treePanel.getSelectionModel().getSelectedItems())
    {
      getUserTableIds(userTableIds, selectedItem);
    }
    Analyzer.getInstance().clearHistory(userTableIds);
  }

  private void displayTable()
  {
    for (ModelData selectedItem : treePanel.getSelectionModel().getSelectedItems())
    {
      int userTableId = selectedItem.get(Keys.USER_TABLE_ID);
      Analyzer.getInstance().selectTable(userTableId);
    }
  }

  public void displayTableData(int userTableId)
  {
    selectItem(Keys.USER_TABLE_ID, userTableId);
  }

  private void getUserTableIds(LinkedList<Integer> userTableIds, ModelData parent)
  {
    int userTableId = parent.get(Keys.USER_TABLE_ID);
    userTableIds.add(userTableId);

    int childCount = mergeTreeStore.getChildCount(parent);
    for (int childOffset = 0; childOffset < childCount; childOffset++)
    {
      ModelData child = mergeTreeStore.getChild(parent, childOffset);
      getUserTableIds(userTableIds, child);
    }

    // Remove from store on return to prevent user from reselecting table
    mergeTreeStore.remove(parent);
  }

  @Override
  protected void onLeafDoubleClick()
  {
    displayTable();
  }

  @Override
  protected void onParentDoubleClick()
  {
    displayTable();
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    treePanel.setIconProvider(TableTypeIconProvider.getInstance());
    //treePanel.setStyleAttribute("border-bottom", "1px solid DDDDDD");

    add(tableCountLabel = new Label(), new Constraint("w=-1,t=5,l=5,V=m"));
    add(clearButton = new Button("Clear", new ClearButtonListener()), new Constraint("s,t=5,b=5,l=5"));
    add(clearAllButton = new Button("Clear All", new ClearAllButtonListener()), new Constraint("s,t=5,b=5,l=5"));
    add(displayButton = new Button("Display", new DisplayButtonListener()), new Constraint("s,t=5,b=5,l=5,r=5"));

    displayButton.setToolTip("Display selected history item, reopening it if necessary");
    clearButton.setToolTip("Clear selected history item and close any associated tables");
    clearAllButton.setToolTip("Clear all history items and close any open tables");
  }

  @Override
  public void onSelectionChanged(ModelData selectedItem)
  {
    updateFormState();
  }

  private void setInstance(ShowHistory showHistory)
  {
    ShowHistory.instance = showHistory;
  }

  private void updateFormState()
  {
    boolean hasItems = mergeTreeStore.getChildCount() > 0;
    clearAllButton.setEnabled(hasItems);

    List<ModelData> selectedItems = treePanel.getSelectionModel().getSelectedItems();
    int selectedItemCount = selectedItems.size();
    boolean isSelected = selectedItemCount > 0;
    clearButton.setEnabled(isSelected);
    displayButton.setEnabled(isSelected);

    tableCountLabel.setText(selectedItemCount + " of " + mergeTreeStore.getAllItems().size() + " Tables");
  }

  public void updateHistory()
  {
    treePanel.expandAll();
    updateFormState();
  }

  private final class ClearAllButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      clearAllHistory();
    }
  }

  private final class ClearButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      clearHistory();
    }
  }

  private final class DisplayButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      displayTable();
    }
  }

}
