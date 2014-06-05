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
import com.example.analyzer.client.widgets.ComboBoxCellEditor;
import com.example.analyzer.client.widgets.TableTreePanel;
import com.example.analyzer.client.widgets.ViewPanel;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.shared.DbColumnPair;
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.store.StoreEvent;
import com.extjs.gxt.ui.client.store.StoreListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.google.gwt.user.client.Element;

public class JoinTablesHorizontally extends ViewPanel
{
  private static JoinTablesHorizontally instance;
  private static final String LEFT = "f";
  private static final String RIGHT = "t";

  public static JoinTablesHorizontally getInstance()
  {
    return JoinTablesHorizontally.instance;
  }

  private Button addColumnButton;
  private Button clearButton;
  private EditorGrid<ModelData> columnGrid;
  private ListStore<ModelData> columnGridListStore;
  private Button joinButton;
  private ComboBoxCellEditor<ModelData> leftColumnComboBox;
  private TableTreePanel leftTreePanel;
  private ComboBoxCellEditor<ModelData> rightColumnComboBox;
  private TableTreePanel rightTreePanel;

  public JoinTablesHorizontally(String heading)
  {
    super(heading);
    setInstance(this);
    setIcon(Resources.APPLICATION_TILE_HORIZONTAL);
  }

  private void addRowForColumnPair()
  {
    BaseModel defaultRow = new BaseModel();
    defaultRow.set(LEFT, null);
    defaultRow.set(RIGHT, null);
    int insertOffset = columnGridListStore.getCount();
    columnGridListStore.insert(defaultRow, insertOffset);
  }

  protected void clearColumnGridListStore()
  {
    columnGridListStore.removeAll();
    updateFormState();
  }

  public void configureLeftJoinColumn(int userTableId, String columnName)
  {
    setJoinCondition(LEFT, userTableId, columnName);
  }

  public void configureRightJoinColumn(int userTableId, String columnName)
  {
    setJoinCondition(RIGHT, userTableId, columnName);
  }

  public void displayCellData(int userTableId, int columnOffset, String columnName, String value)
  {
    // Only update table data if user is not building a join condition
    if (updateColumnName(LEFT, columnName) || updateColumnName(RIGHT, columnName))
    {
      updateFormState();
    }
  }

  public void displayTableData(int userTableId)
  {
    // Only update table data if user is not building a join condition
    int count = columnGridListStore.getCount();
    if (count == 0 || (count == 1 && columnGridListStore.getAt(0).get(LEFT) == null))
    {
      leftTreePanel.selectTableInHistory(userTableId);
    }
    if (count == 0 || (count == 1 && columnGridListStore.getAt(0).get(RIGHT) == null))
    {
      rightTreePanel.selectTableInHistory(userTableId);
    }
  }

  public void joinTablesHorizontally()
  {
    ModelData leftTable = leftTreePanel.getValue();
    ModelData rightTable = rightTreePanel.getValue();

    int leftUserTableId = leftTable.get(Keys.USER_TABLE_ID);
    int rightUserTableId = rightTable.get(Keys.USER_TABLE_ID);

    LinkedList<DbColumnPair> columnPairs = new LinkedList<DbColumnPair>();
    int columnPairCount = columnGridListStore.getCount();
    for (int columnPairOffset = 0; columnPairOffset < columnPairCount; columnPairOffset++)
    {
      ModelData columnPairModelData = columnGridListStore.getAt(columnPairOffset);
      String leftColumnName = columnPairModelData.get(LEFT);
      String rightColumnName = columnPairModelData.get(RIGHT);
      DbColumnPair columnPair = new DbColumnPair(leftColumnName, rightColumnName);
      columnPairs.add(columnPair);
    }

    Analyzer.getInstance().joinTablesHorizontally(leftUserTableId, rightUserTableId, columnPairs);
    clearColumnGridListStore();
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    columnGridListStore = new ListStore<ModelData>();
    columnGridListStore.setMonitorChanges(true);
    columnGridListStore.addStoreListener(new ColumnGridStoreListener());

    add(new Html("Select <b>Left</b> Table:"), new Constraint("w=.5,t=5,l=5"));
    add(new Html("Select <b>Right</b> Table:"), new Constraint("s,w=.5,t=5,l=5"));

    leftTreePanel = new TableTreePanel();
    leftTreePanel.setIconProvider(TableTypeIconProvider.getInstance());
    add(leftTreePanel, new Constraint("w=.5,h=-.70,l=5"));

    rightTreePanel = new TableTreePanel();
    rightTreePanel.setIconProvider(TableTypeIconProvider.getInstance());
    add(rightTreePanel, new Constraint("s,w=.5,h=-.70,l=5,r=5"));

    List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();

    ColumnConfig leftColumnConfig = new ColumnConfig(LEFT, "Left Table Column", 50);
    leftColumnComboBox = new ComboBoxCellEditor<ModelData>();
    leftColumnConfig.setEditor(leftColumnComboBox.getCellEditor());
    leftColumnConfig.setRenderer(Analyzer.CLICK_HERE_GRID_CELL_RENDERER);
    columnConfigs.add(leftColumnConfig);

    ColumnConfig rightColumnConfig = new ColumnConfig(RIGHT, "Right Table Column", 50);
    rightColumnComboBox = new ComboBoxCellEditor<ModelData>();
    rightColumnConfig.setEditor(rightColumnComboBox.getCellEditor());
    rightColumnConfig.setRenderer(Analyzer.CLICK_HERE_GRID_CELL_RENDERER);
    columnConfigs.add(rightColumnConfig);

    ColumnModel columnModel = new ColumnModel(columnConfigs);

    columnGrid = new EditorGrid<ModelData>(columnGridListStore, columnModel);
    columnGrid.getView().setAutoFill(true);
    columnGrid.setBorders(true);
    add(new Label("Join Rows where Columns Contain Matching Content:"), new Constraint("w=1,t=5,l=5"));
    add(columnGrid, new Constraint("w=1,h=-.30,t=1,l=5,r=5"));

    add(new CheckBox(), new Constraint("t=5,l=5,V=m"));
    add(new Label("Fuzzy Join"), new Constraint("s,w=-1,t=5,l=2,H=l,V=m"));

    addColumnButton = new Button("Add", new AddColumnButtonListener());
    addColumnButton.setToolTip("Add a row containing a pair of columns to match");
    add(addColumnButton, new Constraint("s,t=5,b=5,l=5"));

    clearButton = new Button("Clear", new ClearButtonListener());
    clearButton.setToolTip("Clear form and start over");
    add(clearButton, new Constraint("s,t=5,b=5,l=5"));

    joinButton = new Button("Join", new JoinButtonListener());
    joinButton.setToolTip("Join tables horizontally and display results");
    add(joinButton, new Constraint("s,t=5,b=5,l=5,r=5"));

    updateFormState();

    leftTreePanel.getSelectionModel().addSelectionChangedListener(new TableSelectionChangedListener(leftColumnComboBox, LEFT));
    rightTreePanel.getSelectionModel().addSelectionChangedListener(new TableSelectionChangedListener(rightColumnComboBox, RIGHT));
  }

  public void propagateTableSelectionToColumnGrid(ModelData modelData, ComboBoxCellEditor<ModelData> columnComboBox, String type)
  {
    if (modelData == null)
    {
      clearColumnGridListStore();
    }
    else
    {
      int userTableId = modelData.get(Keys.USER_TABLE_ID);
      Analyzer.getInstance().initializeComboBoxListStore(columnComboBox, userTableId);
      int count = columnGridListStore.getCount();
      if (count == 0)
      {
        addRowForColumnPair();
      }
      else
      {
        // Clear column values for previous table
        for (int offset = 0; offset < count; offset++)
        {
          columnGridListStore.getAt(offset).set(type, null);
        }
      }
    }
  }

  protected void selectColumnInComboBox(String type, String columnName)
  {
    if (columnGridListStore.getCount() == 0)
    {
      BaseModel defaultRow = new BaseModel();
      defaultRow.set(type, columnName);
      columnGridListStore.add(defaultRow);
    }
    else
    {
      int lastOffset = columnGridListStore.getCount() - 1;
      columnGridListStore.getAt(lastOffset).set(type, columnName);
    }
  }

  private void setInstance(JoinTablesHorizontally joinTablesHorizontally)
  {
    JoinTablesHorizontally.instance = joinTablesHorizontally;
  }

  private void setJoinCondition(String type, int userTableId, String columnName)
  {
    if (type == LEFT)
    {
      leftTreePanel.selectTableInHistory(userTableId);
      //leftColumnComboBox.setSimpleValue(columnName);
    }
    else
    {
      rightTreePanel.selectTableInHistory(userTableId);
      //rightColumnComboBox.setSimpleValue(columnName);
    }
    selectColumnInComboBox(type, columnName);

  }

  public boolean updateColumnName(String type, String columnName)
  {
    int count = columnGridListStore.getCount();
    if (count == 0)
    {
      addRowForColumnPair();
    }
    else if (count > 1 || columnGridListStore.getAt(0).get(type) != null)
    {
      return false;
    }
    columnGridListStore.getAt(0).set(type, columnName);
    return true;
  }

  public void updateFormState()
  {
    int readyCount = 0;
    int notReadyCount = 0;
    int modelCount = columnGridListStore.getCount();
    for (int modelOffset = 0; modelOffset < modelCount; modelOffset++)
    {
      ModelData modelData = columnGridListStore.getAt(modelOffset);
      if (modelData.get(LEFT) != null && modelData.get(RIGHT) != null)
      {
        readyCount++;
      }
      else
      {
        notReadyCount--;
      }
    }
    joinButton.setEnabled(readyCount > 0 && notReadyCount == 0);
    clearButton.setEnabled(modelCount > 0);
  }

  public void updateHistory()
  {
    leftTreePanel.expandAll();
    rightTreePanel.expandAll();
    updateFormState();
  }

  private final class AddColumnButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      addRowForColumnPair();
    }
  }

  private final class ClearButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      clearColumnGridListStore();
    }
  }

  private final class ColumnGridStoreListener extends StoreListener<ModelData>
  {
    @Override
    public void storeUpdate(StoreEvent<ModelData> se)
    {
      super.storeUpdate(se);
      columnGridListStore.commitChanges();
      updateFormState();
    }
  }

  private final class JoinButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      joinTablesHorizontally();
    }
  }

  private final class TableSelectionChangedListener extends SelectionChangedListener<ModelData>
  {
    private ComboBoxCellEditor<ModelData> columnComboBox;
    private String type;

    public TableSelectionChangedListener(ComboBoxCellEditor<ModelData> columnComboBox, String type)
    {
      this.columnComboBox = columnComboBox;
      this.type = type;
    }

    @Override
    public void selectionChanged(SelectionChangedEvent<ModelData> se)
    {
      propagateTableSelectionToColumnGrid(se.getSelectedItem(), columnComboBox, type);
    }
  }

}
