// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.database;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;

import com.example.analyzer.jdbc.server.JdbcConnection;

public class DbDatabase implements DbDatabaseColumnNameHandler
{
  private HashMap<String, JdbcConnection> jdbcConnections = new HashMap<String, JdbcConnection>();

  @Override
  public String getDatabaseColumnName(String databaseColumnName)
  {
    return databaseColumnName;
  }

  public DbTable read(String databaseUrl, String schemaName, String tableName)
  {
    return read(databaseUrl, schemaName, tableName, this);
  }

  public DbTable read(JdbcConnection jdbcConnection, String schemaName, String tableName)
  {
    return read(jdbcConnection, schemaName, tableName, this);
  }

  public DbTable read(String databaseUrl, String schemaName, String tableName, DbDatabaseColumnNameHandler columnNameHandler)
  {
    return read(getDatabase(databaseUrl), schemaName, tableName, columnNameHandler);
  }

  public DbTable read(JdbcConnection jdbcConnection, String schemaName, String tableName, DbDatabaseColumnNameHandler columnNameHandler)
  {
    PreparedStatement preparedStatement = null;
    try
    {
      StringBuilder s = new StringBuilder();
      s.append("select *\n");
      s.append("from ");
      s.append(schemaName);
      s.append(".");
      s.append(tableName);
      String sql = s.toString();
      preparedStatement = jdbcConnection.createPreparedStatement(sql);
      ResultSet resultSet = preparedStatement.getResultSet();
      ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
      int columnCount = resultSetMetaData.getColumnCount();
      DbTable table = new DbTable();
      for (int columnNumber = 1; columnNumber <= columnCount; columnNumber++)
      {
        String databaseColumnName = resultSetMetaData.getColumnName(columnNumber);
        String columnName = columnNameHandler.getDatabaseColumnName(databaseColumnName);
        table.addColumnName(columnName);
        int columnType = resultSetMetaData.getColumnType(columnNumber);
        table.addColumnType(columnType);
      }
      int rowOffset = 0;
      while (resultSet.next())
      {
        for (int columnOffset = 0, columnNumber = 1; columnOffset < columnCount; columnOffset++, columnNumber++)
        {
          Object columnValue = resultSet.getObject(columnNumber);
          table.set(rowOffset, columnOffset, columnValue);
        }
        rowOffset++;
      }
      return table;
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
    finally
    {
      try
      {
        if (preparedStatement != null)
        {
          preparedStatement.close();
        }
      }
      catch (SQLException e)
      {
      }
    }
  }

  public JdbcConnection getDatabase(String databaseUrl)
  {
    JdbcConnection jdbcConnection = jdbcConnections.get(databaseUrl);
    if (jdbcConnection == null)
    {
      jdbcConnection = new JdbcConnection(databaseUrl);
    }
    return jdbcConnection;
  }

}
