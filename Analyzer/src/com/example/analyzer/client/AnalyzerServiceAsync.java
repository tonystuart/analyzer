// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client;

import com.example.analyzer.shared.ConnectionContent;
import com.example.analyzer.shared.DbColumnPair;
import com.example.analyzer.shared.DbCompareOperation;
import com.example.analyzer.shared.DbSortColumn;
import com.example.analyzer.shared.DbVerticalJoinType;
import com.example.analyzer.shared.DwSummaryColumn;
import com.example.analyzer.shared.HistoryContent;
import com.example.analyzer.shared.PageResult;
import com.example.analyzer.shared.SerializableClasses;
import com.example.analyzer.shared.StateContent;
import com.example.analyzer.shared.TableContent;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.google.gwt.user.client.rpc.AsyncCallback;

public interface AnalyzerServiceAsync
{
  public void addCalculatedColumn(int userTableId, String columnName, String formula, AsyncCallback<TableContent> tableCallback);

  public void addExpandedColumns(int userTableId, String columnName, String expanderName, Iterable<String> fieldNames, AsyncCallback<TableContent> tableCallback);

  public void addSerializableClassesToWhiteList(SerializableClasses serializableClasses, AsyncCallback<SerializableClasses> callback);

  public void changeColumnNames(int userTableId, Iterable<String> newColumnNames, AsyncCallback<TableContent> tableCallback);

  public void clearAllHistory(AsyncCallback<HistoryContent> historyCallback);

  public void clearHistory(Iterable<Integer> userTableIds, AsyncCallback<HistoryContent> historyCallback);

  public void createConnection(String connectionName, String url, String userId, String password, AsyncCallback<ConnectionContent> callback);

  public void getPage(int userTableId, PagingLoadConfig loadConfig, AsyncCallback<PageResult> callback);

  public void getState(AsyncCallback<StateContent> callback);

  public void getTable(int userTableId, AsyncCallback<TableContent> tableCallback);

  public void joinTablesHorizontally(int fromUserTableId, int toUserTableId, Iterable<DbColumnPair> columnPairs, AsyncCallback<TableContent> tableCallback);

  public void joinTablesVertically(int topUserTableId, int bottomUserTableId, DbVerticalJoinType verticalJoinType, AsyncCallback<TableContent> tableCallback);

  public void openTable(String connectionName, String schemaName, String tableName, AsyncCallback<TableContent> callback);

  public void selectColumns(int userTableId, Iterable<String> columnNames, AsyncCallback<TableContent> tableCallback);

  public void selectColumns(int userTableId, Iterable<String> selectColumns, Iterable<DbSortColumn> sortColumns, AsyncCallback<TableContent> tableCallback);

  public void selectRows(int userTableId, String columnName, DbCompareOperation compareOperation, String value, AsyncCallback<TableContent> tableCallback);

  public void sortTable(int userTableId, Iterable<DbSortColumn> sortColumns, AsyncCallback<TableContent> tableCallback);

  public void summarizeColumns(int userTableId, Iterable<String> groupingColumnNames, Iterable<DwSummaryColumn> summaryColumns, AsyncCallback<TableContent> tableCallback);

}
