// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server;

import com.example.analyzer.client.AnalyzerService;
import com.example.analyzer.client.Utilities;
import com.example.analyzer.shared.ConnectionContent;
import com.example.analyzer.shared.DbColumnPair;
import com.example.analyzer.shared.DbCompareOperation;
import com.example.analyzer.shared.DbSortColumn;
import com.example.analyzer.shared.DbVerticalJoinType;
import com.example.analyzer.shared.DwSummaryColumn;
import com.example.analyzer.shared.HistoryContent;
import com.example.analyzer.shared.PageResult;
import com.example.analyzer.shared.SerializableClasses;
import com.example.analyzer.shared.ServerException;
import com.example.analyzer.shared.StateContent;
import com.example.analyzer.shared.TableContent;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

// TODO: To provide the most flexible interface, change Iterable to Iterable where possible.

@SuppressWarnings("serial")
public class AnalyzerServiceImpl extends RemoteServiceServlet implements AnalyzerService
{
  private DataWarehouse dataWarehouse = DataWarehouse.getInstance();

  @Override
  public TableContent addCalculatedColumn(int userTableId, String columnName, String formula) throws ServerException
  {
    try
    {
      return dataWarehouse.addCalculatedColumn(userTableId, columnName, formula);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public TableContent addExpandedColumns(int userTableId, String columnName, String expanderName, Iterable<String> fieldNames) throws ServerException
  {
    try
    {
      return dataWarehouse.addExpandedColumns(userTableId, columnName, expanderName, fieldNames);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public SerializableClasses addSerializableClassesToWhiteList(SerializableClasses serializableClasses) throws ServerException
  {
    try
    {
      return serializableClasses;
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public TableContent changeColumnNames(int userTableId, Iterable<String> newColumnNames) throws ServerException
  {
    try
    {
      return dataWarehouse.changeColumnNames(userTableId, newColumnNames);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public HistoryContent clearAllHistory() throws ServerException
  {
    try
    {
      return dataWarehouse.clearAllHistory();
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public HistoryContent clearHistory(Iterable<Integer> userTableIds) throws ServerException
  {
    try
    {
      return dataWarehouse.clearHistory(userTableIds);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public ConnectionContent createConnection(String connectionName, String url, String userId, String password) throws ServerException
  {
    try
    {
      return dataWarehouse.createConnection(connectionName, url, userId, password);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  protected void doUnexpectedFailure(Throwable e)
  {
    System.err.println("******************* doUnexpectedFailure *******************");
    e.printStackTrace();
    super.doUnexpectedFailure(e);
  }

  @Override
  public PageResult getPage(int userTableId, PagingLoadConfig pagingLoadConfig) throws ServerException
  {
    try
    {
      return dataWarehouse.getPage(userTableId, pagingLoadConfig);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public StateContent getState() throws ServerException
  {
    try
    {
      return dataWarehouse.getState();
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public TableContent getTable(int userTableId) throws ServerException
  {
    try
    {
      return dataWarehouse.getTable(userTableId);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public TableContent joinTablesHorizontally(int fromUserTableId, int toUserTableId, Iterable<DbColumnPair> columnPairs) throws ServerException
  {
    try
    {
      return dataWarehouse.joinTablesHorizontally(fromUserTableId, toUserTableId, columnPairs);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public TableContent joinTablesVertically(int topUserTableId, int bottomUserTableId, DbVerticalJoinType verticalJoinType) throws ServerException
  {
    try
    {
      return dataWarehouse.joinTablesVertically(topUserTableId, bottomUserTableId, verticalJoinType);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public TableContent openTable(String connectionName, String schemaName, String tableName) throws ServerException
  {
    try
    {
      return dataWarehouse.openTable(connectionName, schemaName, tableName);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public TableContent selectColumns(int userTableId, Iterable<String> columnNames) throws ServerException
  {
    try
    {
      return dataWarehouse.selectColumns(userTableId, columnNames);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public TableContent selectColumns(int userTableId, Iterable<String> columnNames, Iterable<DbSortColumn> sortColumns) throws ServerException
  {
    try
    {
      return dataWarehouse.selectColumns(userTableId, columnNames, sortColumns);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public TableContent selectRows(int userTableId, String columnName, DbCompareOperation compareOperation, String value) throws ServerException
  {
    try
    {
      return dataWarehouse.selectRows(userTableId, columnName, compareOperation, value);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public TableContent sortTable(int userTableId, Iterable<DbSortColumn> sortColumns) throws ServerException
  {
    try
    {
      return dataWarehouse.sortTable(userTableId, sortColumns);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

  @Override
  public TableContent summarizeColumns(int userTableId, Iterable<String> groupingColumnNames, Iterable<DwSummaryColumn> summaryColumns) throws ServerException
  {
    try
    {
      return dataWarehouse.summarizeColumns(userTableId, groupingColumnNames, summaryColumns);
    }
    catch (RuntimeException e)
    {
      throw new ServerException(Utilities.getStackTrace(e));
    }
  }

}
