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
import java.util.LinkedList;

public class StateContent implements Serializable
{
  private ConnectionContent connectionContent;
  private HistoryContent historyContent;
  private QueryContent queryContent;
  private ViewContent viewContent;
  private LinkedList<DbColumnExpanderProperties> expandedColumnDescriptors;

  public StateContent()
  {
  }

  public StateContent(ConnectionContent connectionContent, HistoryContent historyContent, QueryContent queryContent, ViewContent viewContent, LinkedList<DbColumnExpanderProperties> expandedColumnDescriptors)
  {
    this.connectionContent = connectionContent;
    this.historyContent = historyContent;
    this.queryContent = queryContent;
    this.viewContent = viewContent;
    this.expandedColumnDescriptors = expandedColumnDescriptors;
  }

  public ConnectionContent getConnectionContent()
  {
    return connectionContent;
  }

  public void setConnectionContent(ConnectionContent connectionContent)
  {
    this.connectionContent = connectionContent;
  }

  public HistoryContent getHistoryContent()
  {
    return historyContent;
  }

  public void setHistoryContent(HistoryContent historyContent)
  {
    this.historyContent = historyContent;
  }

  public QueryContent getQueryContent()
  {
    return queryContent;
  }

  public void setQueryContent(QueryContent queryContent)
  {
    this.queryContent = queryContent;
  }

  public ViewContent getViewContent()
  {
    return viewContent;
  }

  public void setViewContent(ViewContent viewContent)
  {
    this.viewContent = viewContent;
  }

  public LinkedList<DbColumnExpanderProperties> getExpandedColumnDescriptors()
  {
    return expandedColumnDescriptors;
  }

  public void setExpandedColumnDescriptors(LinkedList<DbColumnExpanderProperties> expandedColumnDescriptors)
  {
    this.expandedColumnDescriptors = expandedColumnDescriptors;
  }

}
