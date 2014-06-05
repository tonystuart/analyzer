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
import com.example.analyzer.shared.DbDirection;
import com.example.analyzer.shared.DbSortColumn;
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnData;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridCellRenderer;
import com.google.gwt.user.client.Element;

public class SortTable extends AccordionViewPanel
{
  private static SortTable instance;

  public static SortTable getInstance()
  {
    return SortTable.instance;
  }
  
  private Button clearButton;
  private Label columnCountLabel;
  private ColumnModel columnModel;
  private Grid<ModelData> grid;
  private Button sortButton;
  private LinkedList<ModelData> sortList = new LinkedList<ModelData>();

  public SortTable(String heading)
  {
    super(heading);
    setInstance(this);
    setIcon(Resources.TABLE_SORT);
  }

  private void clear()
  {
    int itemCount = tableListStore.getCount();
    for (int itemOffset = 0; itemOffset < itemCount; itemOffset++)
    {
      ModelData modelData = tableListStore.getAt(itemOffset);
      modelData.set(Keys.ASCENDING, null);
      modelData.set(Keys.DESCENDING, null);
      modelData.set(Keys.SORT_SEQUENCE, null);
    }
    sortList.clear();
    updateFormState();
  }

  private final boolean isSet(Object object)
  {
    return object != null && ((Boolean)object).booleanValue();
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    GridCellRenderer<ModelData> buttonRenderer = new LightweightRadioRenderer();

    List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();

    ColumnConfig nameColumnConfig = new ColumnConfig(Keys.NAME, "Column Name", 150);
    columnConfigs.add(nameColumnConfig);

    ColumnConfig ascendingColumnConfig = new ColumnConfig(Keys.ASCENDING, "Ascending", 70);
    ascendingColumnConfig.setRenderer(buttonRenderer);
    ascendingColumnConfig.setSortable(false);
    ascendingColumnConfig.setResizable(false);
    ascendingColumnConfig.setAlignment(HorizontalAlignment.CENTER);
    columnConfigs.add(ascendingColumnConfig);

    ColumnConfig descendingColumnConfig = new ColumnConfig(Keys.DESCENDING, "Descending", 70);
    descendingColumnConfig.setRenderer(buttonRenderer);
    descendingColumnConfig.setSortable(false);
    descendingColumnConfig.setResizable(false);
    descendingColumnConfig.setAlignment(HorizontalAlignment.CENTER);
    columnConfigs.add(descendingColumnConfig);

    ColumnConfig orderColumnConfig = new ColumnConfig(Keys.SORT_SEQUENCE, "Order", 45);
    orderColumnConfig.setAlignment(HorizontalAlignment.CENTER);
    orderColumnConfig.setSortable(false);
    orderColumnConfig.setResizable(false);
    columnConfigs.add(orderColumnConfig);

    columnModel = new ColumnModel(columnConfigs);
    grid = new Grid<ModelData>(tableListStore, columnModel);
    grid.setAutoExpandColumn(Keys.NAME);
    grid.setBorders(true);

    add(grid, new Constraint("w=1,h=-1,t=5,l=5,r=5"));
    add(columnCountLabel = new Label(), new Constraint("w=-1,t=5,l=5,V=m"));
    add(clearButton = new Button("Clear", new ClearButtonListener()), new Constraint("s,t=5,b=5"));
    add(sortButton = new Button("Sort", new SortButtonListener()), new Constraint("s,t=5,r=5,b=5,l=5"));

    clearButton.setToolTip("Clear sort fields and start over");
    sortButton.setToolTip("Sort table and display results");

    grid.getSelectionModel().addListener(Events.BeforeSelect, new SelectionPreventionGridListener());
    grid.addListener(Events.CellClick, new SortDirectionGridListener());

    updateFormState();
  }

  private void setInstance(SortTable sortTable)
  {
    SortTable.instance = sortTable;
  }

  // NB: A column in the TablePanel is a row in the SortTable.

  public void configureSortColumn(int columnOffset, SortDir sortDir)
  {
    ModelData modelData = tableListStore.getAt(columnOffset);

    if (sortDir == null)
    {
      sortDir = isSet(modelData.get(Keys.ASCENDING)) ? SortDir.DESC : SortDir.ASC;
    }

    if (sortDir == SortDir.ASC)
    {
      setSortDirection(modelData, Keys.ASCENDING, Keys.DESCENDING);
    }
    else
    {
      setSortDirection(modelData, Keys.DESCENDING, Keys.ASCENDING);
    }

    System.out.println("SortTable.setSortColumn: ensureVisible(" + columnOffset + ")");
    grid.getView().ensureVisible(columnOffset, 0, false);
    updateFormState();
  }

  private void setSortDirection(ModelData modelData, String newDirection, String oldDirection)
  {
    if (isSet(modelData.get(oldDirection)))
    {
      modelData.set(oldDirection, null);
    }
    if (!isSet(modelData.get(newDirection)))
    {
      modelData.set(newDirection, true);
    }
    if (modelData.get(Keys.SORT_SEQUENCE) == null)
    {
      sortList.add(modelData);
      modelData.set(Keys.SORT_SEQUENCE, sortList.size());
    }
  }

  private void sortTable()
  {
    LinkedList<DbSortColumn> sortColumns = new LinkedList<DbSortColumn>();
    for (ModelData modelData : sortList)
    {
      String columnName = modelData.get(Keys.NAME);
      if (isSet(modelData.get(Keys.ASCENDING)))
      {
        DbSortColumn sortColumn = new DbSortColumn(columnName, DbDirection.ASCENDING);
        sortColumns.add(sortColumn);
      }
      else if (isSet(modelData.get(Keys.DESCENDING)))
      {
        DbSortColumn sortColumn = new DbSortColumn(columnName, DbDirection.DESCENDING);
        sortColumns.add(sortColumn);
      }
    }
    Analyzer.getInstance().sortTable(userTableId, sortColumns);
  }

  protected void updateFormState()
  {
    int sortFieldCount = sortList.size();
    boolean isSortable = sortFieldCount > 0;
    clearButton.setEnabled(isSortable);
    sortButton.setEnabled(isSortable);
    columnCountLabel.setText(sortFieldCount + " of " + tableListStore.getCount() + " Columns");
  }

  protected void reconfigure()
  {
    System.out.println("SortTable.updateTableState");
    ModelData tableListStoreProperties = tableListStore.getProperties();
    sortList = tableListStoreProperties.get(Keys.SORT_LIST);
    if (sortList == null)
    {
      sortList = new LinkedList<ModelData>();
      tableListStoreProperties.set(Keys.SORT_LIST, sortList);
    }
    grid.reconfigure(tableListStore, columnModel);
  }

  private final class ClearButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      clear();
    }
  }

  private final class LightweightRadioRenderer implements GridCellRenderer<ModelData>
  {
    public Object render(final ModelData model, final String property, ColumnData config, final int rowIndex, final int colIndex, ListStore<ModelData> store, Grid<ModelData> grid)
    {
      String value = isSet(model.get(property)) ? "<img src='tick.png' alt='x'/>" : "<img src='cross.png' alt='o'/>";
      return value;
    }
  }

  private final class SelectionPreventionGridListener implements Listener<BaseEvent>
  {
    @Override
    public void handleEvent(BaseEvent be)
    {
      be.setCancelled(true);
    }
  }

  private final class SortButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      sortTable();
    }
  }

  private final class SortDirectionGridListener implements Listener<GridEvent<ModelData>>
  {
    @Override
    public void handleEvent(GridEvent<ModelData> be)
    {
      int columnOffset = be.getColIndex();
      if (columnOffset == 1)
      {
        setSortDirection(be.getModel(), Keys.ASCENDING, Keys.DESCENDING);
      }
      else if (columnOffset == 2)
      {
        setSortDirection(be.getModel(), Keys.DESCENDING, Keys.ASCENDING);
      }
      updateFormState();
    }

  }

}
