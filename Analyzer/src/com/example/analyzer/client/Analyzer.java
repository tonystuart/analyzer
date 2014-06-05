// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.example.analyzer.client.dialogs.CreateConnection;
import com.example.analyzer.client.views.AddCalculatedColumns;
import com.example.analyzer.client.views.AddExpandedColumns;
import com.example.analyzer.client.views.ChangeColumnNames;
import com.example.analyzer.client.views.JoinTablesHorizontally;
import com.example.analyzer.client.views.JoinTablesVertically;
import com.example.analyzer.client.views.SelectColumns;
import com.example.analyzer.client.views.SelectRows;
import com.example.analyzer.client.views.ShowDatabaseConnections;
import com.example.analyzer.client.views.ShowHistory;
import com.example.analyzer.client.views.SortTable;
import com.example.analyzer.client.views.SummarizeColumns;
import com.example.analyzer.client.widgets.ManagedAccordionPanel;
import com.example.analyzer.client.widgets.ManagedTabPanel;
import com.example.analyzer.client.widgets.NullGridCellRenderer;
import com.example.analyzer.client.widgets.ViewPanel;
import com.example.analyzer.jdbc.shared.ColumnTypes;
import com.example.analyzer.shared.ColumnDescriptor;
import com.example.analyzer.shared.ConnectionContent;
import com.example.analyzer.shared.DbColumnPair;
import com.example.analyzer.shared.DbCompareOperation;
import com.example.analyzer.shared.DbSortColumn;
import com.example.analyzer.shared.DbVerticalJoinType;
import com.example.analyzer.shared.DwSummaryColumn;
import com.example.analyzer.shared.HistoryContent;
import com.example.analyzer.shared.Keys;
import com.example.analyzer.shared.PageResult;
import com.example.analyzer.shared.QueryContent;
import com.example.analyzer.shared.StateContent;
import com.example.analyzer.shared.TableContent;
import com.example.analyzer.shared.UserTableDescriptor;
import com.example.analyzer.shared.ViewContent;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.widget.Viewport;
import com.extjs.gxt.ui.client.widget.form.ComboBox;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.RootPanel;

public class Analyzer implements EntryPoint
{
  public static final String CLICK_HERE = "Click Here";
  public static final NullGridCellRenderer CLICK_HERE_GRID_CELL_RENDERER = new NullGridCellRenderer(CLICK_HERE);
  public static final int DEFAULT_POPUP_HEIGHT = 400;
  public static final int DEFAULT_POPUP_WIDTH = 400;
  public static final int EMPTY_TABLE = -1;
  private static Analyzer instance;

  public static Analyzer getInstance()
  {
    return Analyzer.instance;
  }

  private MergeTreeStore connectionMergeTreeStore = new MergeTreeStore(Keys.NAME);
  private AnalyzerServiceAsync analyzerService = GWT.create(AnalyzerService.class);
  private MergeTreeStore historyMergeTreeStore = new MergeTreeStore(Keys.NAME);
  private MergeTreeStore queryMergeTreeStore = new MergeTreeStore(Keys.NAME);
  private TableCallback tableCallback = new TableCallback();
  private HashMap<Integer, PropertyListStore<ModelData>> tableListStores = new HashMap<Integer, PropertyListStore<ModelData>>();
  private MergeTreeStore viewMergeTreeStore = new MergeTreeStore(Keys.NAME);

  public Analyzer()
  {

  }

  public void addCalculatedColumn(int userTableId, String columnName, String formula)
  {
    setBusyStatus("Calculating...");
    analyzerService.addCalculatedColumn(userTableId, columnName, formula, tableCallback);
  }

  public void addExpandedColumns(int userTableId, String columnName, String expanderName, LinkedList<String> fieldNames)
  {
    setBusyStatus("Expanding...");
    analyzerService.addExpandedColumns(userTableId, columnName, expanderName, fieldNames, tableCallback);
  }

  private void cacheTableData(TableContent tableContent)
  {
    UserTableDescriptor userTableDescriptor = tableContent.getUserTableDescriptor();
    int userTableId = userTableDescriptor.getUserTableId();
    if (!tableListStores.containsKey(userTableId))
    {
      // NB: tableListStores already contains this data (+ properties) if this is a retrieve via history
      PropertyListStore<ModelData> tableListStore = new PropertyListStore<ModelData>();
      tableListStore.setMonitorChanges(true); // prior to adding data so listener can be added to data
      for (ColumnDescriptor columnDescriptor : userTableDescriptor.getColumnDescriptors())
      {
        int columnType = columnDescriptor.getColumnType();
        String columnTypeName = ColumnTypes.getTypeName(columnType);
        String columnName = columnDescriptor.getColumnName();
        ModelData modelData = new BaseModel();
        modelData.set(Keys.TYPE, columnTypeName);
        modelData.set(Keys.NAME, columnName);
        tableListStore.add(modelData);
      }
      tableListStores.put(userTableId, tableListStore);
    }
  }

  public void changeColumnNames(int userTableId, LinkedList<String> newColumnNames)
  {
    setBusyStatus("Changing...");
    analyzerService.changeColumnNames(userTableId, newColumnNames, tableCallback);
  }

  public void clearAllHistory()
  {
    ManagedTabPanel.getInstance().closeAll();
    initializeTableListStores();
    displayTableData(EMPTY_TABLE);
    setBusyStatus("Clearing History...");
    analyzerService.clearAllHistory(new HistoryCallback());
  }

  public void clearHistory(Iterable<Integer> userTableIds)
  {
    closeTables(userTableIds);
    setBusyStatus("Clearing History...");
    analyzerService.clearHistory(userTableIds, new HistoryCallback());
  }

  public void clearStatus()
  {
    ManagedAccordionPanel.getInstance().clearStatus();
  }

  private void closeTables(Iterable<Integer> userTableIds)
  {
    ManagedTabPanel.getInstance().closeTables(userTableIds);
    for (int userTableId : userTableIds)
    {
      // TODO: Consider whether we need to remove any/all StoreListeners. Do we add any?
      tableListStores.remove(userTableId);
    }
    int selectedUserTableId = ManagedTabPanel.getInstance().getSelectedUserTableId();
    displayTableData(selectedUserTableId);
  }

  public void configureCompareOperation(int userTableId, int columnOffset, String compareOperation, String value)
  {
    ManagedAccordionPanel.getInstance().selectViewPanel(SelectRows.getInstance());
    SelectRows.getInstance().configureCompareOperation(userTableId, columnOffset, compareOperation, value);
  }

  public void configureGroupingColumn(int userTableId, String columnName)
  {
    ManagedAccordionPanel.getInstance().selectViewPanel(SummarizeColumns.getInstance());
    SummarizeColumns.getInstance().configureGroupingColumn(userTableId, columnName);
  }

  public void configureLeftJoinColumn(int userTableId, String columnName)
  {
    ManagedAccordionPanel.getInstance().selectViewPanel(JoinTablesHorizontally.getInstance());
    JoinTablesHorizontally.getInstance().configureLeftJoinColumn(userTableId, columnName);
  }

  public void configureRightJoinColumn(int userTableId, String columnName)
  {
    ManagedAccordionPanel.getInstance().selectViewPanel(JoinTablesHorizontally.getInstance());
    JoinTablesHorizontally.getInstance().configureRightJoinColumn(userTableId, columnName);
  }

  public void configureSelectedColumns(ColumnModel columnModel)
  {
    ManagedAccordionPanel.getInstance().selectViewPanel(SelectColumns.getInstance());
    SelectColumns.getInstance().configureSelectedColumns(columnModel);
  }

  public void configureSortColumn(int columnOffset, SortDir sortDir)
  {
    ManagedAccordionPanel.getInstance().selectViewPanel(SortTable.getInstance());
    SortTable.getInstance().configureSortColumn(columnOffset, sortDir);
  }

  public void configureSummaryOperation(int userTableId, String columnName, String summaryOperation)
  {
    ManagedAccordionPanel.getInstance().selectViewPanel(SummarizeColumns.getInstance());
    SummarizeColumns.getInstance().configureSummaryOperation(userTableId, columnName, summaryOperation);
  }

  public void createConnection(String connectionName, String url, String userId, String password)
  {
    setBusyStatus("Connecting to " + url + "...");
    analyzerService.createConnection(connectionName, url, userId, password, new ConnectionCallback());
  }

  public void displayTableData(int userTableId)
  {
    AddCalculatedColumns.getInstance().displayTableData(userTableId);
    AddExpandedColumns.getInstance().displayTableData(userTableId);
    ChangeColumnNames.getInstance().displayTableData(userTableId);
    JoinTablesHorizontally.getInstance().displayTableData(userTableId);
    ShowHistory.getInstance().displayTableData(userTableId);
    SelectColumns.getInstance().displayTableData(userTableId);
    SelectRows.getInstance().displayTableData(userTableId);
    SortTable.getInstance().displayTableData(userTableId);
    SummarizeColumns.getInstance().displayTableData(userTableId);
  }

  public MergeTreeStore getConnectionMergeTreeStore()
  {
    return connectionMergeTreeStore;
  }

  public MergeTreeStore getHistoryMergeTreeStore()
  {
    return historyMergeTreeStore;
  }

  public void getInitialState()
  {
    analyzerService.getState(new InitialStateCallback());
  }

  public void getPage(int userTableId, PagingLoadConfig loadConfig, AsyncCallback<PageResult> callback)
  {
    setBusyStatus("Requesting Page...");
    analyzerService.getPage(userTableId, loadConfig, new PageCallback(callback));
  }

  public MergeTreeStore getQueryMergeTreeStore()
  {
    return queryMergeTreeStore;
  }

  public PropertyListStore<ModelData> getTableListStore(int userTableId)
  {
    PropertyListStore<ModelData> tableListStore = tableListStores.get(userTableId);
    return tableListStore;
  }

  public MergeTreeStore getViewMergeTreeStore()
  {
    return viewMergeTreeStore;
  }

  public void initializeComboBoxListStore(ComboBox<ModelData> comboBox, int userTableId)
  {
    PropertyListStore<ModelData> propertyListStore = getTableListStore(userTableId);
    if (propertyListStore == null)
    {
      setBusyStatus("Requesting Column Names...");
      analyzerService.getTable(userTableId, new ComboBoxTableCallback(comboBox));
    }
    else
    {
      comboBox.setStore(propertyListStore);
    }
  }

  protected void initializeTableListStores()
  {
    tableListStores.clear();
    tableListStores.put(EMPTY_TABLE, new PropertyListStore<ModelData>());
  }

  public void joinTablesHorizontally(int fromUserTableId, int toUserTableId, LinkedList<DbColumnPair> columnPairs)
  {
    setBusyStatus("Joining Tables...");
    analyzerService.joinTablesHorizontally(fromUserTableId, toUserTableId, columnPairs, tableCallback);
  }

  public void joinTablesVertically(int topUserTableId, int bottomUserTableId, DbVerticalJoinType verticalJoinType)
  {
    setBusyStatus("Joining Tables...");
    analyzerService.joinTablesVertically(topUserTableId, bottomUserTableId, verticalJoinType, tableCallback);
  }

  public void onModuleLoad()
  {
    setInstance(this);
    GWT.setUncaughtExceptionHandler(new DefaultUncaughtExceptionHandler());

    initializeTableListStores();

    Viewport viewport = new Viewport();
    viewport.setLayout(new FitLayout());
    viewport.add(new MainPanel());

    RootPanel.get().add(viewport);
    getInitialState();
  }

  public void openTable(final String connectionName, final String schemaName, final String tableName)
  {
    setBusyStatus("Opening " + tableName + "...");
    analyzerService.openTable(connectionName, schemaName, tableName, tableCallback);
  }

  public void openTable(TableContent tableContent)
  {
    cacheTableData(tableContent);
    ManagedTabPanel.getInstance().openTable(tableContent);
    // NB: TableManager selects the new tab which results in a callback via Analyzer.displayTableData(userTableId)
  }

  public void refreshState()
  {
    setBusyStatus("Refreshing...");
    analyzerService.getState(new RefreshStateCallback());
  }

  public void restore(String heading)
  {
    ManagedAccordionPanel.getInstance().morphIn(heading);
  }

  public void retrieveTable(int userTableId)
  {
    setBusyStatus("Retrieving Table...");
    analyzerService.getTable(userTableId, tableCallback);
  }

  public void selectColumns(int userTableId, List<String> columnNames)
  {
    setBusyStatus("Selecting Columns...");
    analyzerService.selectColumns(userTableId, columnNames, tableCallback);
  }

  public void selectColumns(int userTableId, List<String> selectColumns, List<DbSortColumn> sortColumns)
  {
    setBusyStatus("Selecting Columns...");
    analyzerService.selectColumns(userTableId, selectColumns, sortColumns, tableCallback);
  }

  public void selectRows(int userTableId, String columnName, DbCompareOperation compareOperation, String value)
  {
    setBusyStatus("Selecting Rows...");
    analyzerService.selectRows(userTableId, columnName, compareOperation, value, tableCallback);
  }

  public void selectTable(int userTableId)
  {
    // NB: TableManager selects the new tab which results in a callback via Analyzer.displayTableData(userTableId)
    if (!ManagedTabPanel.getInstance().showTable(userTableId))
    {
      retrieveTable(userTableId);
    }
  }

  public void selectViewPanel(ViewPanel viewPanel)
  {
    ManagedAccordionPanel.getInstance().selectViewPanel(viewPanel);
  }

  public void setBusyStatus(String message)
  {
    ManagedAccordionPanel.getInstance().setBusyStatus(message);
  }

  private void setInstance(Analyzer analyzer)
  {
    Analyzer.instance = analyzer;
  }

  public void sortTable(int userTableId, LinkedList<DbSortColumn> sortColumns)
  {
    setBusyStatus("Sorting Table...");
    analyzerService.sortTable(userTableId, sortColumns, tableCallback);
  }

  public void summarizeColumns(int userTableId, List<String> groupingColumnNames, List<DwSummaryColumn> summaryColumns)
  {
    analyzerService.summarizeColumns(userTableId, groupingColumnNames, summaryColumns, tableCallback);
  }

  protected void updateConnections(ConnectionContent connectionContent)
  {
    connectionMergeTreeStore.merge(connectionContent.getBaseTreeModel());
    ShowDatabaseConnections.getInstance().updateConnections(connectionContent.getConnectionName());
    CreateConnection.getInstance().updateConnections();
  }

  protected void updateHistory(HistoryContent historyContent)
  {
    historyMergeTreeStore.merge(historyContent.getBaseTreeModel());
    JoinTablesHorizontally.getInstance().updateHistory();
    JoinTablesVertically.getInstance().updateHistory();
    ShowHistory.getInstance().updateHistory();
  }

  protected void updateQueries(QueryContent queryContent)
  {
    queryMergeTreeStore.merge(queryContent.getBaseTreeModel());
  }

  protected void updateViews(ViewContent viewContent)
  {
    viewMergeTreeStore.merge(viewContent.getBaseTreeModel());
  }

  public abstract class ClearStatusCallback<T> extends FailureReportingAsyncCallback<T>
  {
    @Override
    public void onFailure(Throwable caught)
    {
      clearStatus();
      super.onFailure(caught);
    }

    @Override
    public void onSuccess(T result)
    {
      clearStatus();
      process(result);
    }

    protected abstract void process(T result);
  }

  private final class ComboBoxTableCallback extends ClearStatusCallback<TableContent>
  {
    private final ComboBox<ModelData> comboBox;

    private ComboBoxTableCallback(ComboBox<ModelData> comboBox)
    {
      this.comboBox = comboBox;
    }

    @Override
    protected void process(TableContent tableContent)
    {
      cacheTableData(tableContent);
      comboBox.setStore(getTableListStore(tableContent.getUserTableDescriptor().getUserTableId()));
    }
  }

  public class ConnectionCallback extends ClearStatusCallback<ConnectionContent>
  {
    @Override
    public void process(ConnectionContent connectionContent)
    {
      updateConnections(connectionContent);
    }
  }

  public class HistoryCallback extends ClearStatusCallback<HistoryContent>
  {
    @Override
    protected void process(HistoryContent historyContent)
    {
      updateHistory(historyContent);
    }

  }

  public class InitialStateCallback extends FailureReportingAsyncCallback<StateContent>
  {
    @Override
    public void onSuccess(StateContent stateContent)
    {
      updateConnections(stateContent.getConnectionContent());
      updateHistory(stateContent.getHistoryContent());
      updateQueries(stateContent.getQueryContent());
      updateViews(stateContent.getViewContent());
      AddExpandedColumns.getInstance().configure(stateContent.getExpandedColumnDescriptors());
      displayTableData(EMPTY_TABLE);

      ShowDatabaseConnections.getInstance().updateConnections(null);

      if (historyMergeTreeStore.getChildCount() > 0)
      {
        selectViewPanel(ShowHistory.getInstance());
      }
      else
      {
        selectViewPanel(ShowDatabaseConnections.getInstance());
        if (connectionMergeTreeStore.getChildCount() == 0)
        {
          ShowDatabaseConnections.getInstance().create();
        }
      }

      MainPanel.getInstance().showViewManager();
    }

  }

  public class PageCallback extends ClearStatusCallback<PageResult>
  {
    private AsyncCallback<PageResult> callback;

    public PageCallback(AsyncCallback<PageResult> callback)
    {
      this.callback = callback;
    }

    @Override
    public void onFailure(Throwable caught)
    {
      super.onFailure(caught);
      callback.onFailure(caught);
    }

    @Override
    protected void process(PageResult pageResult)
    {
      callback.onSuccess(pageResult);
    }
  }

  public class RefreshStateCallback extends ClearStatusCallback<StateContent>
  {
    @Override
    protected void process(StateContent stateContent)
    {
      updateConnections(stateContent.getConnectionContent());
      updateHistory(stateContent.getHistoryContent());
      updateQueries(stateContent.getQueryContent());
      updateViews(stateContent.getViewContent());
    }
  }

  public class TableCallback extends ClearStatusCallback<TableContent>
  {
    @Override
    protected void process(TableContent tableContent)
    {
      updateHistory(tableContent.getHistoryContent());
      openTable(tableContent);
    }
  }

}
