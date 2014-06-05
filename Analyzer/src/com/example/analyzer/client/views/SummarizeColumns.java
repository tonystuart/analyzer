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
import com.example.analyzer.client.widgets.EnumCellEditorComboBox;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.shared.DwSummaryColumn;
import com.example.analyzer.shared.DwSummaryOperation;
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.GridSelectionModel;
import com.google.gwt.user.client.Element;

public class SummarizeColumns extends AccordionViewPanel
{
  private static SummarizeColumns instance;

  public static SummarizeColumns getInstance()
  {
    return SummarizeColumns.instance;
  }

  private Button clearButton;
  private EditorGrid<ModelData> groupingGrid;
  private Button summarizeButton;
  private EditorGrid<ModelData> summaryGrid;
  private EnumCellEditorComboBox<DwSummaryOperation> summaryOperationComboBox;

  public SummarizeColumns(String heading)
  {
    super(heading);
    setInstance(this);
    setIcon(Resources.REPORT);
  }

  private void clear()
  {
    groupingGrid.getStore().removeAll();
    summaryGrid.getStore().removeAll();
    updateFormState();
  }

  public void configureGroupingColumn(int userTableId, String columnName)
  {
    GridSelectionModel<ModelData> selectionModel = groupingGrid.getSelectionModel();
    ModelData modelData = tableListStore.findModel(Keys.NAME, columnName);
    if (modelData != null)
    {
      selectionModel.select(modelData, true);
    }
    updateFormState();
  }

  public void configureSummaryOperation(int userTableId, String columnName, String summaryOperation)
  {
    ModelData modelData = tableListStore.findModel(Keys.NAME, columnName);
    if (modelData != null)
    {
      modelData.set(Keys.SUMMARIZE_COLUMNS_SUMMARY_OPERATION, summaryOperation);
    }
    updateFormState();
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    add(new Html("Select optional <b>Grouping Columns</b>:"), new Constraint("w=1,t=5,l=5,r=5"));

    List<ColumnConfig> groupingColumnConfigs = new LinkedList<ColumnConfig>();

    CheckBoxSelectionModel<ModelData> checkBoxSelectionModel = new CheckBoxSelectionModel<ModelData>();
    groupingColumnConfigs.add(checkBoxSelectionModel.getColumn());
    ColumnConfig columnNameConfig = new ColumnConfig(Keys.NAME, "Column Name", 125);
    groupingColumnConfigs.add(columnNameConfig);
    ColumnConfig typeColumnConfig = new ColumnConfig(Keys.TYPE, "Column Type", 125);
    groupingColumnConfigs.add(typeColumnConfig);
    ColumnModel groupingColumnModel = new ColumnModel(groupingColumnConfigs);

    groupingGrid = new EditorGrid<ModelData>(tableListStore, groupingColumnModel);
    groupingGrid.setAutoExpandColumn(Keys.NAME);
    groupingGrid.setBorders(true);
    groupingGrid.setSelectionModel(checkBoxSelectionModel);
    groupingGrid.addPlugin(checkBoxSelectionModel);
    add(groupingGrid, new Constraint("w=1,t=1,h=-0.5,l=5,r=5"));

    add(new Html("Click on item in <b>Summary Operations</b> column to change it:"), new Constraint("w=1,t=5,l=5,r=5"));

    List<ColumnConfig> summaryColumnConfigs = new LinkedList<ColumnConfig>();

    columnNameConfig = new ColumnConfig(Keys.NAME, "Column Name", 100);
    summaryColumnConfigs.add(columnNameConfig);

    ColumnConfig summaryOperation = new ColumnConfig(Keys.SUMMARIZE_COLUMNS_SUMMARY_OPERATION, "Summary Operation", 150);
    summaryOperationComboBox = new EnumCellEditorComboBox<DwSummaryOperation>(DwSummaryOperation.values());
    summaryOperation.setEditor(summaryOperationComboBox.getCellEditor());
    summaryOperation.setRenderer(Analyzer.CLICK_HERE_GRID_CELL_RENDERER);
    summaryColumnConfigs.add(summaryOperation);

    ColumnModel summaryColumnModel = new ColumnModel(summaryColumnConfigs);

    summaryGrid = new EditorGrid<ModelData>(tableListStore, summaryColumnModel);
    summaryGrid.setBorders(true);
    summaryGrid.setAutoExpandColumn(Keys.NAME);
    add(summaryGrid, new Constraint("w=1,t=1,h=-0.5,l=5,r=5"));

    clearButton = new Button("Clear", new ClearButtonListener());
    clearButton.setToolTip("Clear form and start over");
    add(clearButton, new Constraint("w=-1,t=5,b=5,H=r"));

    summarizeButton = new Button("Summarize", new SummarizeButtonListener());
    summarizeButton.setToolTip("Summarize columns and display results");
    add(summarizeButton, new Constraint("s,t=5,l=5,b=5,r=5"));

    updateFormState();
  }

  @Override
  protected void reconfigure()
  {
    List<ModelData> selectedItems = groupingGrid.getSelectionModel().getSelectedItems();
    configuredTableListStore.getProperties().set(Keys.SUMMARIZE_COLUMNS_GROUPING_SELECTION, selectedItems);

    groupingGrid.reconfigure(tableListStore, groupingGrid.getColumnModel());

    selectedItems = tableListStore.getProperties().get(Keys.SUMMARIZE_COLUMNS_GROUPING_SELECTION);
    if (selectedItems != null)
    {
      groupingGrid.getSelectionModel().setSelection(selectedItems);
    }

    summaryGrid.reconfigure(tableListStore, summaryGrid.getColumnModel());
  }

  private void setInstance(SummarizeColumns summarizeColumns)
  {
    SummarizeColumns.instance = summarizeColumns;
  }

  private void summarizeColumns()
  {
    List<String> groupingColumnNames = new LinkedList<String>();
    ListStore<ModelData> groupingListStore = groupingGrid.getStore();
    int groupingCount = groupingListStore.getCount();
    for (int groupingOffset = 0; groupingOffset < groupingCount; groupingOffset++)
    {
      ModelData groupingModelData = groupingListStore.getAt(groupingOffset);
      String groupingColumnName = groupingModelData.get(Keys.NAME);
      if (groupingColumnName != null)
      {
        groupingColumnNames.add(groupingColumnName);
      }
    }
    List<DwSummaryColumn> summaryColumns = new LinkedList<DwSummaryColumn>();
    ListStore<ModelData> summaryListStore = summaryGrid.getStore();
    int summaryCount = summaryListStore.getCount();
    for (int summaryOffset = 0; summaryOffset < summaryCount; summaryOffset++)
    {
      ModelData summaryModelData = summaryListStore.getAt(summaryOffset);
      String columnName = summaryModelData.get(Keys.NAME);
      DwSummaryOperation summaryOperation = summaryModelData.get(Keys.SUMMARIZE_COLUMNS_SUMMARY_OPERATION);
      DwSummaryColumn summaryColumn = new DwSummaryColumn(columnName, summaryOperation);
      summaryColumns.add(summaryColumn);
    }
    Analyzer.getInstance().summarizeColumns(userTableId, groupingColumnNames, summaryColumns);
  }

  @Override
  protected void updateFormState()
  {
    int groupingCount = 0;
    int nameCount = 0;
    int operationCount = 0;
    ListStore<ModelData> groupingListStore = groupingGrid.getStore();
    int listStoreCount = groupingListStore.getCount();
    for (int modelOffset = 0; modelOffset < listStoreCount; modelOffset++)
    {
      ModelData modelData = groupingListStore.getAt(modelOffset);
      if (modelData.get(Keys.NAME) != null)
      {
        groupingCount++;
      }
    }
    ListStore<ModelData> summaryListStore = summaryGrid.getStore();
    listStoreCount = summaryListStore.getCount();
    for (int modelOffset = 0; modelOffset < listStoreCount; modelOffset++)
    {
      ModelData modelData = summaryListStore.getAt(modelOffset);
      if (modelData.get(Keys.NAME) != null)
      {
        nameCount++;
      }
      if (modelData.get(Keys.SUMMARIZE_COLUMNS_SUMMARY_OPERATION) != null)
      {
        operationCount++;
      }
    }
    summarizeButton.setEnabled(nameCount > 0 && operationCount == nameCount);
    clearButton.setEnabled(groupingCount > 0 || nameCount > 0 || operationCount > 0);
  }

  private final class ClearButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      clear();
    }
  }

  private final class SummarizeButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      summarizeColumns();
    }
  }

}
