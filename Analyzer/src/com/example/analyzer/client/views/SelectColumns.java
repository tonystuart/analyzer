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
import com.example.analyzer.client.widgets.AccordionViewPanel;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.Store;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.google.gwt.user.client.Element;

public class SelectColumns extends AccordionViewPanel
{
  private static SelectColumns instance;

  public static SelectColumns getInstance()
  {
    return SelectColumns.instance;
  }

  private Button clearButton;
  private Label columnCountLabel;
  private Button downButton;
  private Grid<ModelData> grid;
  private Button selectButton;
  private Button upButton;

  public SelectColumns(String heading)
  {
    super(heading);
    setInstance(this);
    setIcon(Resources.APPLICATION_GET);
  }

  private void clear()
  {
    clearGridSortColumn();
    grid.getSelectionModel().deselectAll();
    grid.getView().getHeader().refresh();
    updateFormState();
  }

  private void clearGridSortColumn()
  {
    tableListStore.setStoreSorter(null);
    tableListStore.setSortField(null);
    tableListStore.setSortDir(SortDir.NONE);
  }

  private void down()
  {
    int count = tableListStore.getCount();
    GridSelectionModel<ModelData> selectionModel = grid.getSelectionModel();
    for (int i = count - 1, j = count - 2; j >= 0; i--, j--)
    {
      ModelData item = tableListStore.getAt(i);
      ModelData nextItem = tableListStore.getAt(j);
      if (!selectionModel.isSelected(item) && selectionModel.isSelected(nextItem))
      {
        // Clever method of pushing selected items down by bubbling unselected items
        // up... doesn't change the select list and generate extra events.
        tableListStore.remove(item);
        tableListStore.insert(item, j);
      }
    }
    updateFormState();
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
    CheckBoxSelectionModel<ModelData> checkBoxSelectionModel = new CheckBoxSelectionModel<ModelData>();
    columnConfigs.add(checkBoxSelectionModel.getColumn());
    ColumnConfig nameColumnConfig = new ColumnConfig(Keys.NAME, "Column Name", 125);
    columnConfigs.add(nameColumnConfig);
    ColumnConfig typeColumnConfig = new ColumnConfig(Keys.TYPE, "Column Type", 125);
    columnConfigs.add(typeColumnConfig);
    ColumnModel columnModel = new ColumnModel(columnConfigs);
    
    grid = new Grid<ModelData>(tableListStore, columnModel);
    grid.setAutoExpandColumn(Keys.NAME);
    grid.setBorders(true);
    grid.setSelectionModel(checkBoxSelectionModel);
    grid.addPlugin(checkBoxSelectionModel);
    add(grid, new Constraint("w=1,h=-1,t=5,l=5,r=5"));
    
    add(columnCountLabel = new Label(), new Constraint("w=-1,t=5,l=5,V=m"));

    upButton = new Button("Up", new UpButtonListener());
    upButton.setToolTip("Move selected item(s) up");
    add(upButton, new Constraint("s,t=5,b=5"));

    downButton = new Button("Down", new DownButtonListener());
    downButton.setToolTip("Move selected item(s) down");
    add(downButton, new Constraint("s,t=5,r=5,b=5,l=5"));

    clearButton = new Button("Clear", new ClearButtonListener());
    clearButton.setToolTip("Clear selected columns and start over");
    add(clearButton, new Constraint("s,t=5,b=5"));

    selectButton = new Button("Select", new SelectButtonListener());
    selectButton.setToolTip("Select columns and display results");
    add(selectButton, new Constraint("s,t=5,r=5,b=5,l=5"));

    grid.getSelectionModel().addListener(Events.SelectionChange, new SelectionChangeListener());
    tableListStore.addListener(Store.Sort, new SortListener());
    tableListStore.setMonitorChanges(true);

    updateFormState();
  }

  private void selectColumns()
  {
    int itemCount = tableListStore.getCount();
    LinkedList<String> columnNames = new LinkedList<String>();
    GridSelectionModel<ModelData> selectionModel = grid.getSelectionModel();
    for (int itemOffset = 0; itemOffset < itemCount; itemOffset++)
    {
      ModelData modelData = tableListStore.getAt(itemOffset);
      String columnName = modelData.get(Keys.NAME);
      if (selectionModel.isSelected(modelData))
      {
        columnNames.add(columnName);
      }
    }
    Analyzer.getInstance().selectColumns(userTableId, columnNames);
  }

  private void setInstance(SelectColumns selectColumns)
  {
    SelectColumns.instance = selectColumns;
  }

  public void configureSelectedColumns(ColumnModel columnModel)
  {
    int columnOffset = 0;
    GridSelectionModel<ModelData> selectionModel = grid.getSelectionModel();
    for (ColumnConfig columnConfig : columnModel.getColumns())
    {
      if (columnConfig.isHidden())
      {
        selectionModel.deselect(columnOffset);
      }
      else
      {
        selectionModel.select(columnOffset, true);
      }
      columnOffset++;
    }
    updateFormState();
  }

  private void up()
  {
    int count = tableListStore.getCount();
    GridSelectionModel<ModelData> selectionModel = grid.getSelectionModel();
    for (int i = 0, j = 1; j < count; i++, j++)
    {
      ModelData item = tableListStore.getAt(i);
      ModelData nextItem = tableListStore.getAt(j);
      if (!selectionModel.isSelected(item) && selectionModel.isSelected(nextItem))
      {
        // Clever method of bubbling selected items up by pushing unselected items
        // down... doesn't change the select list and generate extra events.
        tableListStore.remove(item);
        tableListStore.insert(item, j);
      }
    }
    updateFormState();
  }

  protected void updateFormState()
  {
    boolean isUpwardMobile;
    boolean isDownwardMobile;
    List<ModelData> selectedItems = grid.getSelectionModel().getSelectedItems();
    int size = selectedItems == null ? 0 : selectedItems.size();
    boolean isSelected = size > 0;
    if (isSelected)
    {
      int firstOffset = Integer.MAX_VALUE;
      int lastOffset = Integer.MIN_VALUE;
      for (ModelData selectedItem : selectedItems)
      {
        int offset = tableListStore.indexOf(selectedItem);
        if (offset < firstOffset)
        {
          firstOffset = offset;
        }
        if (offset > lastOffset)
        {
          lastOffset = offset;
        }
      }
      boolean isGap = ((lastOffset - firstOffset) + 1) > size; // +1 to convert offset to count
      isUpwardMobile = (firstOffset > 0) || isGap;
      isDownwardMobile = (lastOffset < tableListStore.getCount() - 1) || isGap;
    }
    else
    {
      isUpwardMobile = false;
      isDownwardMobile = false;
    }
    boolean isSorted = tableListStore.getStoreSorter() != null;
    upButton.setEnabled(isUpwardMobile && !isSorted);
    downButton.setEnabled(isDownwardMobile && !isSorted);
    clearButton.setEnabled(isSelected || isSorted);
    selectButton.setEnabled(isSelected);

    columnCountLabel.setText(selectedItems.size() + " of " + tableListStore.getCount() + " Columns");
  }

  protected void reconfigure()
  {
    List<ModelData> selectedItems = grid.getSelectionModel().getSelectedItems();
    configuredTableListStore.getProperties().set(Keys.SELECT_COLUMN_ITEMS, selectedItems);
    
    grid.reconfigure(tableListStore, grid.getColumnModel());
    
    selectedItems = tableListStore.getProperties().get(Keys.SELECT_COLUMN_ITEMS);
    if (selectedItems != null)
    {
      grid.getSelectionModel().setSelection(selectedItems);
    }
  }

  private final class ClearButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      clear();
    }
  }

  private final class DownButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      down();
    }
  }

  private final class SelectButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      selectColumns();
    }
  }

  private class SelectionChangeListener implements Listener<BaseEvent>
  {
    @Override
    public void handleEvent(BaseEvent be)
    {
      updateFormState();
    }
  }

  private final class SortListener implements Listener<BaseEvent>
  {
    @Override
    public void handleEvent(BaseEvent be)
    {
      // It's not possible to use up/down when a column is sorted by the
      // ListStore, so we automatically clear the sort column, following the sort.
      if (tableListStore.getStoreSorter() != null)
      {
        clearGridSortColumn();
      }

      // The sort may have moved a selected item into a position that makes
      // it no longer movable, so we must update the button state.
      updateFormState();
    }
  }

  private final class UpButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      up();
    }
  }

}
