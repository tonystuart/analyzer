// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server;

import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import com.example.analyzer.jdbc.server.ColumnTypeMap;
import com.example.analyzer.jdbc.server.JdbcConnection;
import com.example.analyzer.server.database.AnalysisEngine;
import com.example.analyzer.server.database.DbAverageSummarizer;
import com.example.analyzer.server.database.DbColumnGroup;
import com.example.analyzer.server.database.DbCountDistinctSummarizer;
import com.example.analyzer.server.database.DbCountSummarizer;
import com.example.analyzer.server.database.DbDatabase;
import com.example.analyzer.server.database.DbSummarizer;
import com.example.analyzer.server.database.DbTable;
import com.example.analyzer.server.database.DbTotalSummarizer;
import com.example.analyzer.server.expanders.DbColumnExpander;
import com.example.analyzer.server.expanders.DbColumnExpanderContext;
import com.example.analyzer.server.expanders.date.DbDateColumnExpander;
import com.example.analyzer.server.expanders.uszipcode.DbUsZipCodeColumnExpander;
import com.example.analyzer.shared.ArrayModelData;
import com.example.analyzer.shared.ColumnDescriptor;
import com.example.analyzer.shared.ConnectionContent;
import com.example.analyzer.shared.DbColumnExpanderProperties;
import com.example.analyzer.shared.DbColumnPair;
import com.example.analyzer.shared.DbCompareOperation;
import com.example.analyzer.shared.DbSortColumn;
import com.example.analyzer.shared.DbVerticalJoinType;
import com.example.analyzer.shared.DwSummaryColumn;
import com.example.analyzer.shared.DwSummaryOperation;
import com.example.analyzer.shared.HistoryContent;
import com.example.analyzer.shared.Keys;
import com.example.analyzer.shared.PageResult;
import com.example.analyzer.shared.QueryContent;
import com.example.analyzer.shared.StateContent;
import com.example.analyzer.shared.TableContent;
import com.example.analyzer.shared.TableType;
import com.example.analyzer.shared.UserTableDescriptor;
import com.example.analyzer.shared.ViewContent;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.TreeModel;

public final class DataWarehouse
{
  private static DataWarehouse searchEngineSingleton;

  // private Database database;

  public synchronized static DataWarehouse getInstance()
  {
    if (searchEngineSingleton == null)
    {
      searchEngineSingleton = new DataWarehouse();
    }
    return searchEngineSingleton;
  }

  private AnalysisEngine analysisEngine = new AnalysisEngine();
  private LinkedList<DbColumnExpanderProperties> columnExpanderProperties = new LinkedList<DbColumnExpanderProperties>();
  private LinkedList<DbColumnExpander> columnExpanders = new LinkedList<DbColumnExpander>();
  private TreeMap<String, JdbcConnection> databaseCache = new TreeMap<String, JdbcConnection>();
  private DbDatabase databaseReader = new DbDatabase();
  private HashMap<TableCacheKey, DbTable> tableCache = new HashMap<TableCacheKey, DbTable>();
  private UserData userData = new UserData();

  private DataWarehouse()
  {
    initializeColumnExpanders();
  }

  public TableContent addCalculatedColumn(int userTableId, String columnName, String formula)
  {
    DbTable oldTable = lookupUserTable(userTableId);
    DbTable newTable = analysisEngine.addCalculatedColumn(oldTable, columnName, formula);
    TableContent tableContent = userData.createTableContent(userTableId, newTable, "Add Calculated Columns", DwToolTips.getTableToolTip(formula, newTable), TableType.ADD_CALCULATED_COLUMNS);
    return tableContent;
  }

  public TableContent addExpandedColumns(int userTableId, String columnName, String expanderName, Iterable<String> fieldNames)
  {
    LinkedList<DbColumnExpanderContext> dbColumnExpanderContexts = new LinkedList<DbColumnExpanderContext>();

    for (DbColumnExpander columnExpander : columnExpanders)
    {
      if (columnExpander.getColumnExpanderProperties().getExpanderName().equals(expanderName))
      {
        DbColumnExpanderContext columnExpanderContext = columnExpander.configure(columnName, fieldNames);
        dbColumnExpanderContexts.add(columnExpanderContext);
      }
    }

    DbTable oldTable = lookupUserTable(userTableId);
    DbTable newTable = analysisEngine.expand(oldTable, dbColumnExpanderContexts);
    TableContent tableContent = userData.createTableContent(userTableId, newTable, "Expand Columns", DwToolTips.getTableToolTip(newTable), TableType.ADD_EXPANDED_COLUMNS);
    return tableContent;
  }

  public TableContent changeColumnNames(int userTableId, Iterable<String> columnNames)
  {
    DbTable oldTable = lookupUserTable(userTableId);
    DbTable newTable = analysisEngine.changeColumnNames(oldTable, columnNames);
    TableContent tableContent = userData.createTableContent(userTableId, newTable, "Change Column Names", DwToolTips.getTableToolTip(newTable), TableType.CHANGE_COLUMN_NAMES);
    return tableContent;
  }

  public HistoryContent clearAllHistory()
  {
    return userData.clearAllHistory();
  }

  public HistoryContent clearHistory(Iterable<Integer> userTableIds)
  {
    return userData.clearHistory(userTableIds);
  }

  public ConnectionContent createConnection(String connectionName, String url, String userId, String password)
  {
    if (isEmpty(connectionName))
    {
      throw new RuntimeException("Missing user defined connection name");
    }

    if (isEmpty(url))
    {
      throw new RuntimeException("Missing database JDBC URL");
    }

    if (userData.isConnection(connectionName))
    {
      throw new RuntimeException("Connection " + connectionName + " is already defined");
    }

    JdbcConnection jdbcConnection = new JdbcConnection(url, userId, password);
    databaseCache.put(url, jdbcConnection);
    userData.addUrl(connectionName, url);
    BaseTreeModel connectionTree = getConnectionTree();
    ConnectionContent connectionContent = new ConnectionContent(connectionName, connectionTree);
    return connectionContent;
  }

  public BaseTreeModel createDummyData(String prefix)
  {
    BaseTreeModel root = new BaseTreeModel();
    for (int i = 0; i < 5; i++)
    {
      BaseTreeModel child = new BaseTreeModel(root);
      child.set(Keys.NAME, prefix + " " + i);
    }
    return root;
  }

  public LinkedList<DbColumnExpanderProperties> getColumnExpanderProperties()
  {
    return columnExpanderProperties;
  }

  @SuppressWarnings("unused")
  private List<String> getColumnNames(int userTableId)
  {
    LinkedList<String> columnNames = new LinkedList<String>();
    UserTableCacheItem userTableCacheItem = userData.getTableCacheItem(userTableId);
    UserTableDescriptor userTableDescriptor = userTableCacheItem.getUserTableDescriptor();
    for (ColumnDescriptor columnDescriptor : userTableDescriptor.getColumnDescriptors())
    {
      String columnName = columnDescriptor.getColumnName();
      columnNames.add(columnName);
    }
    return columnNames;
  }

  public BaseTreeModel getConnectionTree()
  {
    BaseTreeModel databaseTreeModel = new BaseTreeModel();

    for (Entry<String, String> entry : userData.urlCache.entrySet())
    {
      String name = entry.getKey();
      String url = entry.getValue();
      JdbcConnection jdbcConnection = databaseCache.get(url);
      BaseTreeModel databaseConnectionTreeModel = new BaseTreeModel(databaseTreeModel);
      databaseConnectionTreeModel.set(Keys.NAME, name);
      getSchemas(jdbcConnection, databaseConnectionTreeModel);
    }

    return databaseTreeModel;
  }

  public PageResult getPage(int userTableId, PagingLoadConfig pagingLoadConfig)
  {
    DbTable table = lookupUserTable(userTableId);
    int limit = pagingLoadConfig.getLimit();
    int offset = pagingLoadConfig.getOffset();
    int rowCount = table.getRowCount();
    int columnCount = table.getColumnCount();
    List<ModelData> dataList = new LinkedList<ModelData>();
    for (int i = 0, rowOffset = offset; i < limit && rowOffset < rowCount; i++, rowOffset++)
    {
      String[] data = new String[columnCount];
      for (int columnOffset = 0; columnOffset < columnCount; columnOffset++)
      {
        data[columnOffset] = table.coalesce(rowOffset, columnOffset, "").toString();
      }
      ArrayModelData rowData = new ArrayModelData(data);
      dataList.add(rowData);
    }
    PageResult results = new PageResult();
    results.setData(dataList);
    results.setOffset(offset);
    results.setTotalLength(rowCount);
    return results;
  }

  private void getSchemas(JdbcConnection jdbcConnection, BaseTreeModel parent)
  {
    try
    {
      DatabaseMetaData databaseMetaData = jdbcConnection.getConnection().getMetaData();
      ResultSet schemas = databaseMetaData.getSchemas();
      while (schemas.next())
      {
        String schemaName = schemas.getString(1);
        BaseTreeModel child = new BaseTreeModel();
        getTables(jdbcConnection, child, schemaName);
        if (child.getChildCount() > 0)
        {
          child.set(Keys.NAME, schemaName);
          parent.add(child);
        }
      }
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  public StateContent getState()
  {
    StateContent stateContent = new StateContent();
    stateContent.setConnectionContent(new ConnectionContent(getConnectionTree()));
    stateContent.setHistoryContent(userData.createHistoryContent());
    stateContent.setQueryContent(new QueryContent(createDummyData("Query")));
    stateContent.setViewContent(new ViewContent(createDummyData("View")));
    stateContent.setExpandedColumnDescriptors(getColumnExpanderProperties());
    return stateContent;
  }

  public TableContent getTable(int userTableId)
  {
    UserTableCacheItem userTableCacheItem = userData.getTableCacheItem(userTableId);
    UserTableDescriptor userTableDescriptor = userTableCacheItem.getUserTableDescriptor();
    TableContent tableContent = userData.createTableContent(userTableDescriptor);
    return tableContent;
  }

  private void getTables(JdbcConnection jdbcConnection, BaseTreeModel parent, String schemaName)
  {
    try
    {
      DatabaseMetaData databaseMetaData = jdbcConnection.getConnection().getMetaData();
      ResultSet tables = databaseMetaData.getTables(null, schemaName, null, new String[] {
          "TABLE",
          "VIEW"
      });
      while (tables.next())
      {
        String tableName = tables.getString(3);
        BaseTreeModel child = new BaseTreeModel(parent);
        child.set(Keys.NAME, tableName);
        String toolTip = DwToolTips.getTableDetailToolTip(jdbcConnection, databaseMetaData, schemaName, tableName);
        child.set(Keys.TOOL_TIP, toolTip);
      }
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }

  }

  private void initializeColumnExpanders()
  {
    // TODO: Add support for dynamically loading column expanders
    columnExpanders.add(new DbDateColumnExpander());
    columnExpanders.add(new DbUsZipCodeColumnExpander());
    
    for (DbColumnExpander columnExpander : columnExpanders)
    {
      columnExpanderProperties.add(columnExpander.getColumnExpanderProperties());
    }
  }

  public final boolean isEmpty(String name)
  {
    return name == null || name.isEmpty();
  }

  public TableContent joinTablesHorizontally(int leftUserTableId, int rightUserTableId, Iterable<DbColumnPair> columnPairs)
  {
    DbTable leftTable = lookupUserTable(leftUserTableId);
    DbTable rightTable = lookupUserTable(rightUserTableId);
    DbTable newTable = analysisEngine.joinTablesHorizontally(leftTable, rightTable, columnPairs);
    TableContent tableContent = userData.createTableContent(leftUserTableId, newTable, "Join " + leftUserTableId + " and " + rightUserTableId, DwToolTips.getJoinToolTip(newTable, columnPairs), TableType.JOIN_TABLES_HORIZONTALLY);
    return tableContent;
  }

  public TableContent joinTablesVertically(int topUserTableId, int bottomUserTableId, DbVerticalJoinType verticalJoinType)
  {
    DbTable topTable = lookupUserTable(topUserTableId);
    DbTable bottomTable = lookupUserTable(bottomUserTableId);
    DbTable newTable = analysisEngine.joinTablesVertically(topTable, bottomTable, verticalJoinType);
    TableContent tableContent = userData.createTableContent(topUserTableId, newTable, "Join " + topUserTableId + " and " + bottomUserTableId, DwToolTips.getTableToolTip(verticalJoinType.toString(), newTable), TableType.JOIN_TABLES_VERTICALLY);
    return tableContent;
  }

  private DbTable lookupUserTable(int userTableId)
  {
    UserTableCacheItem userTableCacheItem = userData.getTableCacheItem(userTableId);
    DbTable oldTable = userTableCacheItem.getTable();
    return oldTable;
  }

  public TableContent openTable(String connectionName, String schemaName, String tableName)
  {
    String url = userData.getUrl(connectionName);
    TableCacheKey tableCacheKey = new TableCacheKey(url, schemaName, tableName);
    DbTable table = tableCache.get(tableCacheKey);
    if (table == null)
    {
      JdbcConnection databaseConnection = databaseCache.get(url);
      table = databaseReader.read(databaseConnection, schemaName, tableName);
      tableCache.put(tableCacheKey, table);
    }

    TableContent tableContent = userData.createTableContent(table, "Open " + tableName, DwToolTips.getTableToolTip(connectionName, table), TableType.OPEN_TABLE);
    return tableContent;
  }

  public TableContent selectColumns(int parentTableId, Iterable<String> columnNames)
  {
    DbTable oldTable = lookupUserTable(parentTableId);
    DbTable newTable = analysisEngine.selectColumns(oldTable, columnNames);
    TableContent tableContent = userData.createTableContent(parentTableId, newTable, "Select Columns", DwToolTips.getTableToolTip(newTable), TableType.SELECT_COLUMNS);
    return tableContent;
  }

  public TableContent selectColumns(int parentTableId, Iterable<String> columnNames, Iterable<DbSortColumn> sortColumns)
  {
    DbTable oldTable = lookupUserTable(parentTableId);
    DbTable newTable = analysisEngine.selectColumns(oldTable, columnNames, sortColumns);
    TableContent tableContent = userData.createTableContent(parentTableId, newTable, "Select and Sort Columns", "Select<ul>" + DwToolTips.getSortToolTip(newTable, sortColumns), TableType.SELECT_COLUMNS);
    return tableContent;
  }

  public TableContent selectRows(int parentTableId, String columnName, DbCompareOperation compareOperation, String value)
  {
    DbTable oldTable = lookupUserTable(parentTableId);
    DbTable newTable = analysisEngine.selectRows(oldTable, columnName, compareOperation, value);
    TableContent tableContent = userData.createTableContent(parentTableId, newTable, "Select Rows", DwToolTips.getSelectRowToolTip(newTable, columnName, compareOperation, value), TableType.SELECT_ROWS);
    return tableContent;
  }

  public TableContent sortTable(int parentTableId, Iterable<DbSortColumn> sortColumns)
  {
    DbTable oldTable = lookupUserTable(parentTableId);
    DbTable newTable = analysisEngine.sort(oldTable, sortColumns);
    TableContent tableContent = userData.createTableContent(parentTableId, newTable, "Sort Table", DwToolTips.getSortToolTip(newTable, sortColumns), TableType.SORT_TABLE);
    return tableContent;
  }

  public TableContent summarizeColumns(int userTableId, Iterable<String> groupingColumnNames, Iterable<DwSummaryColumn> summaryColumns)
  {
    DbTable oldTable = lookupUserTable(userTableId);
    DbColumnGroup columnGroup = new DbColumnGroup(groupingColumnNames);
    LinkedList<DbSummarizer> dbSummarizers = new LinkedList<DbSummarizer>();
    for (DwSummaryColumn summaryColumn : summaryColumns)
    {
      DbSummarizer dbSummarizer;
      String columnName = summaryColumn.getColumnName();
      DwSummaryOperation summaryOperation = summaryColumn.getDwSummary();
      switch (summaryOperation)
      {
        case AVERAGE:
          dbSummarizer = new DbAverageSummarizer(columnName, "AVERAGE(" + columnName + ")");
          break;
        case COUNT:
          dbSummarizer = new DbCountSummarizer("COUNT(*)");
          break;
        case COUNT_DISTINCT:
          dbSummarizer = new DbCountDistinctSummarizer(columnName, "COUNT(" + columnName + ")");
          break;
        case TOTAL:
          dbSummarizer = new DbTotalSummarizer(columnName, "TOTAL(" + columnName + ")");
          break;
        default:
          throw new UnsupportedOperationException(summaryOperation.name());
      }
      dbSummarizers.add(dbSummarizer);
    }
    DbTable newTable = analysisEngine.summarize(oldTable, columnGroup, dbSummarizers);
    TableContent tableContent = userData.createTableContent(userTableId, newTable, "Summarize Columns", DwToolTips.getTableToolTip(newTable), TableType.SUMMARIZE_COLUMNS);
    return tableContent;
  }

  public class TableCacheKey
  {
    private String schemaName;
    private String tableName;
    private String url;

    public TableCacheKey(String url, String schemaName, String tableName)
    {
      this.url = url;
      this.schemaName = schemaName;
      this.tableName = tableName;
    }

    @Override
    public boolean equals(Object obj)
    {
      if (this == obj)
      {
        return true;
      }
      if (obj == null)
      {
        return false;
      }
      if (getClass() != obj.getClass())
      {
        return false;
      }
      TableCacheKey other = (TableCacheKey)obj;
      if (!getOuterType().equals(other.getOuterType()))
      {
        return false;
      }
      if (url == null)
      {
        if (other.url != null)
        {
          return false;
        }
      }
      else if (!url.equals(other.url))
      {
        return false;
      }
      if (schemaName == null)
      {
        if (other.schemaName != null)
        {
          return false;
        }
      }
      else if (!schemaName.equals(other.schemaName))
      {
        return false;
      }
      if (tableName == null)
      {
        if (other.tableName != null)
        {
          return false;
        }
      }
      else if (!tableName.equals(other.tableName))
      {
        return false;
      }
      return true;
    }

    private DataWarehouse getOuterType()
    {
      return DataWarehouse.this;
    }

    public String getSchemaName()
    {
      return schemaName;
    }

    public String getTableName()
    {
      return tableName;
    }

    public String getUrl()
    {
      return url;
    }

    @Override
    public int hashCode()
    {
      final int prime = 31;
      int result = 1;
      result = prime * result + getOuterType().hashCode();
      result = prime * result + ((url == null) ? 0 : url.hashCode());
      result = prime * result + ((schemaName == null) ? 0 : schemaName.hashCode());
      result = prime * result + ((tableName == null) ? 0 : tableName.hashCode());
      return result;
    }

  }

  public class UserData
  {
    private BaseTreeModel rootHistoryNode;
    private HashMap<Integer, UserTableCacheItem> tableCache = new HashMap<Integer, DataWarehouse.UserTableCacheItem>();
    private int tableCount;
    private HashMap<String, String> urlCache = new HashMap<String, String>();

    public UserData()
    {
      initializeHistory();
    }

    public void addUrl(String connectionName, String url)
    {
      urlCache.put(connectionName, url);
    }

    public HistoryContent clearAllHistory()
    {
      initializeHistory();
      HistoryContent historyContent = createHistoryContent();
      return historyContent;
    }

    public HistoryContent clearHistory(Iterable<Integer> userTableIds)
    {
      for (int userTableId : userTableIds)
      {
        TreeModel historyNode = findHistoryNode(rootHistoryNode, userTableId);
        // Note that userTableIds may include descendants that are removed when parent is removed
        if (historyNode != null)
        {
          historyNode.getParent().remove(historyNode);
        }
      }
      if (rootHistoryNode.getChildCount() == 0)
      {
        initializeHistory();
      }
      HistoryContent historyContent = createHistoryContent();
      return historyContent;
    }

    public HistoryContent createHistoryContent()
    {
      return new HistoryContent(getRootHistoryNode());
    }

    public TableContent createTableContent(DbTable table, String description, int userTableId)
    {
      UserTableCacheItem userTableCacheItem = createUserTableCacheItem(table, userTableId, description);
      tableCache.put(userTableId, userTableCacheItem);
      UserTableDescriptor userTableDescriptor = userTableCacheItem.getUserTableDescriptor();
      return createTableContent(userTableDescriptor);
    }

    public TableContent createTableContent(DbTable table, String description, String toolTip, TableType tableType)
    {
      int userTableId = getNextTableId();
      description = userTableId + "-" + description;
      extendHistory(userTableId, description, toolTip, tableType);
      return createTableContent(table, description, userTableId);
    }

    public TableContent createTableContent(int parentTableId, DbTable table, String description, String toolTip, TableType tableType)
    {
      int userTableId = getNextTableId();
      description = userTableId + "-" + description;
      extendHistory(parentTableId, userTableId, description, toolTip, tableType);
      return createTableContent(table, description, userTableId);
    }

    public TableContent createTableContent(UserTableDescriptor userTableDescriptor)
    {
      TableContent tableContent = new TableContent(userTableDescriptor, createHistoryContent());
      return tableContent;
    }

    public UserTableCacheItem createUserTableCacheItem(DbTable table, int userTableId, String description)
    {
      UserTableCacheItem userTableCacheItem;
      List<ColumnDescriptor> columnDescriptors = new LinkedList<ColumnDescriptor>();

      int columnCount = table.getColumnCount();
      for (int columnOffset = 0; columnOffset < columnCount; columnOffset++)
      {
        String columnName = table.getColumnName(columnOffset);
        int columnType = ColumnTypeMap.mapType(table.getColumnType(columnOffset));
        ColumnDescriptor columnDescriptor = new ColumnDescriptor(columnName, columnType);
        columnDescriptors.add(columnDescriptor);
      }

      UserTableDescriptor userTableDescriptor = new UserTableDescriptor(userTableId, columnDescriptors, description);
      userTableCacheItem = new UserTableCacheItem(table, userTableDescriptor);
      return userTableCacheItem;
    }

    public int extendHistory(int parentTableId, int userTableId, String description, String toolTip, TableType tableType)
    {
      TreeModel parentHistoryNode = findHistoryNode(rootHistoryNode, parentTableId);
      if (parentHistoryNode == null)
      {
        throw new RuntimeException("Cannot find history item for user table " + parentTableId);
      }
      return extendHistory(parentHistoryNode, userTableId, description, toolTip, tableType);
    }

    public int extendHistory(int userTableId, String description, String toolTip, TableType tableType)
    {
      return extendHistory(rootHistoryNode, userTableId, description, toolTip, tableType);
    }

    public int extendHistory(TreeModel parent, int userTableId, String description, String toolTip, TableType tableType)
    {
      BaseTreeModel newHistoryNode = new BaseTreeModel(parent);
      newHistoryNode.set(Keys.NAME, description);
      newHistoryNode.set(Keys.USER_TABLE_ID, userTableId);
      newHistoryNode.set(Keys.TOOL_TIP, toolTip);
      newHistoryNode.set(Keys.TABLE_TYPE, tableType);
      return userTableId;
    }

    private TreeModel findHistoryNode(TreeModel parent, int tableId)
    {
      int childCount = parent.getChildCount();
      for (int childOffset = 0; childOffset < childCount; childOffset++)
      {
        TreeModel child = (TreeModel)parent.getChild(childOffset);
        if ((Integer)child.get(Keys.USER_TABLE_ID) == tableId)
        {
          return child;
        }
        TreeModel target = findHistoryNode(child, tableId);
        if (target != null)
        {
          return target;
        }
      }
      return null;
    }

    public synchronized int getNextTableId()
    {
      return ++tableCount;
    }

    public BaseTreeModel getRootHistoryNode()
    {
      return rootHistoryNode;
    }

    public UserTableCacheItem getTableCacheItem(int userTableId)
    {
      UserTableCacheItem userTableCacheItem = tableCache.get(userTableId);
      if (userTableCacheItem == null)
      {
        throw new RuntimeException("Table " + userTableId + " is not defined"); // TODO: An undefined table means server restarted while client was connected. Client should display a message and refresh.
      }
      return userTableCacheItem;
    }

    public String getUrl(String connectionName)
    {
      String url = urlCache.get(connectionName);
      if (url == null)
      {
        throw new RuntimeException("Connection " + connectionName + " is not defined");
      }
      return url;
    }

    public void initializeHistory()
    {
      tableCount = 0;
      rootHistoryNode = new BaseTreeModel();
    }

    public boolean isConnection(String connectionName)
    {
      return urlCache.containsKey(connectionName);
    }

  }

  public class UserTableCacheItem
  {
    private DbTable table;
    private UserTableDescriptor userTableDescriptor;

    public UserTableCacheItem(DbTable table, UserTableDescriptor userTableDescriptor)
    {
      this.table = table;
      this.userTableDescriptor = userTableDescriptor;
    }

    public DbTable getTable()
    {
      return table;
    }

    public UserTableDescriptor getUserTableDescriptor()
    {
      return userTableDescriptor;
    }

  }

}
