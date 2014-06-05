// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.jdbc.server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class JdbcConnection
{
  static
  {
    loadAllJdbcDrivers();
  }

  private static final void loadAllJdbcDrivers()
  {
    // In alphabetical order... See http://wiki.netbeans.org/DatabasesAndDrivers
    loadJdbcDriver("com.ibm.db2.jcc.DB2Driver");
    loadJdbcDriver("com.microsoft.sqlserver.jdbc.SQLServerDriver");
    loadJdbcDriver("com.mysql.jdbc.Driver");
    loadJdbcDriver("oracle.jdbc.driver.OracleDriver");
    loadJdbcDriver("org.apache.derby.jdbc.ClientDriver");
  }

  private static final void loadJdbcDriver(String className)
  {
    try
    {
      System.out.print("Loading JDBC driver " + className + "... ");
      Class.forName(className).newInstance();
      System.out.println("Available");
    }
    catch (Exception e)
    {
      System.out.println("Unavailable");
    }
  }

  private Connection connection;

  public JdbcConnection()
  {
  }

  public JdbcConnection(String url)
  {
    open(url, null, null);
  }

  public JdbcConnection(String url, String user, String password)
  {
    open(url, user, password);
  }

  public void open(String url, String user, String password)
  {
    try
    {
      connection = DriverManager.getConnection(url, user, password);
    }
    catch (Exception e)
    {
      throw new RuntimeException(e);
    }
  }

  public PreparedStatement createPreparedStatement(String sql, Object... parameters)
  {
    PreparedStatement preparedStatement = null;
    try
    {
      preparedStatement = connection.prepareStatement(sql);
      for (int i = 0; i < parameters.length; i++)
      {
        Object value = parameters[i];
        preparedStatement.setObject(i + 1, value);
      }
      preparedStatement.executeQuery();
      return preparedStatement;
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e.toString() + "\n" + sql);
    }
  }

  public Connection getConnection()
  {
    return connection;
  }

  public void close(ResultSet resultSet)
  {
    try
    {
      if (resultSet != null)
      {
        resultSet.close();
      }
    }
    catch (SQLException e)
    {
    }
  }

  public Results executeTransaction(PreparedStatement... preparedStatements)
  {
    try
    {
      try
      {
        Results results = new Results();
        connection.setAutoCommit(false);
        for (PreparedStatement preparedStatement : preparedStatements)
        {
          Result result;
          boolean isResultSet = preparedStatement.execute();
          if (isResultSet)
          {
            ResultSet resultSet = preparedStatement.getResultSet();
            result = new Result(resultSet);
          }
          else
          {
            int updateCount = preparedStatement.getUpdateCount();
            result = new Result(updateCount);
          }
          results.add(result);
        }
        return results;
      }
      finally
      {
        connection.setAutoCommit(true);
      }
    }
    catch (SQLException e)
    {
      throw new RuntimeException(e);
    }
  }

  public static class Results
  {
    private ArrayList<Result> results = new ArrayList<Result>();

    public void add(Result result)
    {
      results.add(result);
    }
    
    public void closeAll()
    {
      try
      {
        for (Result result : results)
        {
          if (result.isResultSet())
          {
            result.getResultSet().close();
          }
        }
      }
      catch (SQLException e)
      {
        throw new RuntimeException(e);
      }
    }

    public Result get(int offset)
    {
      return results.get(offset);
    }
  }

  public static class Result
  {
    private ResultSet resultSet;
    private int updateCount;

    public Result(ResultSet resultSet)
    {
      this.resultSet = resultSet;
    }

    public Result(int updateCount)
    {
      this.updateCount = updateCount;
    }

    public boolean isResultSet()
    {
      return resultSet != null;
    }

    public ResultSet getResultSet()
    {
      return resultSet;
    }

    public int getUpdateCount()
    {
      return updateCount;
    }

  }
}
