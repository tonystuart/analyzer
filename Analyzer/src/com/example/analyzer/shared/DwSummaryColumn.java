// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.shared;

import java.io.Serializable;

public class DwSummaryColumn implements Serializable
{
  private String columnName;
  private DwSummaryOperation summaryOperation;

  public DwSummaryColumn()
  {
  }

  public DwSummaryColumn(String columnName, DwSummaryOperation summaryOperation)
  {
    this.columnName = columnName;
    this.summaryOperation = summaryOperation;
  }

  public String getColumnName()
  {
    return columnName;
  }

  public void setColumnName(String columnName)
  {
    this.columnName = columnName;
  }

  public DwSummaryOperation getDwSummary()
  {
    return summaryOperation;
  }

  public void setDwSummary(DwSummaryOperation summaryOperation)
  {
    this.summaryOperation = summaryOperation;
  }

}
