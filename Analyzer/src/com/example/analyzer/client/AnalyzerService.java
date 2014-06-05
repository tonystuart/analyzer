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
import com.example.analyzer.shared.ServerException;
import com.example.analyzer.shared.StateContent;
import com.example.analyzer.shared.TableContent;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

@RemoteServiceRelativePath("analyzerService")
public interface AnalyzerService extends RemoteService
{
  public TableContent addCalculatedColumn(int userTableId, String columnName, String formula) throws ServerException;

  public TableContent addExpandedColumns(int userTableId, String columnName, String expanderName, Iterable<String> fieldNames) throws ServerException;

  public SerializableClasses addSerializableClassesToWhiteList(SerializableClasses serializableClasses) throws ServerException;

  public TableContent changeColumnNames(int userTableId, Iterable<String> newColumnNames) throws ServerException;

  public HistoryContent clearAllHistory() throws ServerException;

  public HistoryContent clearHistory(Iterable<Integer> userTableIds) throws ServerException;

  public ConnectionContent createConnection(String connectionName, String url, String userId, String password) throws ServerException;

  public PageResult getPage(int userTableId, PagingLoadConfig loadConfig) throws ServerException;

  public StateContent getState() throws ServerException;

  public TableContent getTable(int userTableId) throws ServerException;

  public TableContent joinTablesHorizontally(int fromUserTableId, int toUserTableId, Iterable<DbColumnPair> columnPairs) throws ServerException;

  public TableContent joinTablesVertically(int topUserTableId, int bottomUserTableId, DbVerticalJoinType verticalJoinType) throws ServerException;

  public TableContent openTable(String connectionName, String schemaName, String tableName) throws ServerException;

  public TableContent selectColumns(int userTableId, Iterable<String> columnNames) throws ServerException;

  public TableContent selectColumns(int userTableId, Iterable<String> columnNames, Iterable<DbSortColumn> sortColumns) throws ServerException;

  public TableContent selectRows(int userTableId, String columnName, DbCompareOperation compareOperation, String value) throws ServerException;

  public TableContent sortTable(int userTableId, Iterable<DbSortColumn> sortColumns) throws ServerException;

  public TableContent summarizeColumns(int userTableId, Iterable<String> groupingColumnNames, Iterable<DwSummaryColumn> summaryColumns) throws ServerException;

}
