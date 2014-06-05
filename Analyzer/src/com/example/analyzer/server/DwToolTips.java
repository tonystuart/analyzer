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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import com.example.analyzer.jdbc.server.JdbcConnection;
import com.example.analyzer.server.database.DbTable;
import com.example.analyzer.shared.DbColumnPair;
import com.example.analyzer.shared.DbCompareOperation;
import com.example.analyzer.shared.DbSortColumn;

public class DwToolTips
{
  private static final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
  private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

  public static String getTableDetailToolTip(JdbcConnection jdbcConnection, DatabaseMetaData databaseMetaData, String schemaName, String tableName)
  {
    ResultSet columns = null;
    ResultSet rows = null;
    try
    {
      int columnCount = 0;
      LinkedList<String> columnNames = new LinkedList<String>();
      columns = databaseMetaData.getColumns(null, schemaName, tableName, null);
      while (columns.next())
      {
        String columnName = columns.getString(4);
        columnNames.add(columnName);
        // System.out.println("DwToolTips.getTableDetailToolTip: columnName="+columnName+", dataType="+columns.getInt(5)+", typeName="+columns.getString(6));
        columnCount++;
      }
      int rowCount = 0;
      PreparedStatement preparedStatement = jdbcConnection.createPreparedStatement("select count(*) from " + schemaName + "." + tableName);
      rows = preparedStatement.executeQuery();
      if (rows.next())
      {
        rowCount = rows.getInt(1);
      }
      String toolTip = getTableToolTip(null, rowCount, columnCount, columnNames);
      return toolTip;
    }
    catch (Exception e)
    {
      return null;
    }
    finally
    {
      jdbcConnection.close(columns);
      jdbcConnection.close(rows);
    }
  }

  public static String getJoinToolTip(DbTable newTable, Iterable<DbColumnPair> columnPairs)
  {
    StringBuilder s = new StringBuilder();
    for (DbColumnPair columnPair : columnPairs)
    {
      s.append(columnPair.getFromColumnName());
      s.append("<br/>&nbsp;=&nbsp;");
      s.append(columnPair.getToColumnName());
      s.append("<br/>");
    }
    String toolTip = getTableToolTip(s.toString(), newTable);
    return toolTip;
  }

  public static String getSortToolTip(DbTable newTable, Iterable<DbSortColumn> sortColumns)
  {
    StringBuilder s = new StringBuilder();
    for (DbSortColumn sortColumn : sortColumns)
    {
      s.append("SORT ");
      s.append(sortColumn.getDirection());
      s.append(" ");
      s.append(sortColumn.getColumnName());
      s.append("<br/>");
    }
    String toolTip = getTableToolTip(s.toString(), newTable);
    return toolTip;
  }

  public static String getSelectRowToolTip(DbTable newTable, String columnName, DbCompareOperation compareOperation, String value)
  {
    StringBuilder s = new StringBuilder();
    s.append(columnName);
    s.append("<br/>&nbsp;");
    s.append(compareOperation);
    s.append("&nbsp;");
    s.append(value);
    String toolTip = getTableToolTip(s.toString(), newTable);
    return toolTip;
  }

  public static String getTableToolTip(DbTable newTable)
  {
    return getTableToolTip(null, newTable);
  }

  protected static String getTableToolTip(String condition, DbTable newTable)
  {
    return getTableToolTip(condition, newTable.getRowCount(), newTable.getColumnCount(), newTable.getColumnNames());
  }

  public static String getTableToolTip(String condition, int rowCount, int columnCount, Iterable<String> columnNames)
  {
    Date instant = new Date();
    String time = timeFormat.format(instant);
    String date = dateFormat.format(instant);
    StringBuilder s = new StringBuilder();
    if (condition != null)
    {
      s.append(condition);
      s.append("<hr/>");
    }
    for (String columnName : columnNames)
    {
      s.append(columnName);
      s.append("<br/>");
    }
    s.append("<hr/>");
    s.append("ROWS: ");
    s.append(rowCount);
    s.append("<br/>");
    s.append("COLUMNS: ");
    s.append(columnCount);
    s.append("<br/>");
    s.append("TIME: ");
    s.append(time);
    s.append("<br/>");
    s.append("DATE: ");
    s.append(date);
    return s.toString();
  }

}
