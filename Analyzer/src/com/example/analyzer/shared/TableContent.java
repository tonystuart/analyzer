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

public class TableContent implements Serializable
{
  private UserTableDescriptor userTableDescriptor;
  private HistoryContent historyContent;

  public TableContent()
  {
  }

  public TableContent(UserTableDescriptor userTableDescriptor, HistoryContent historyContent)
  {
    this.userTableDescriptor = userTableDescriptor;
    this.historyContent = historyContent;
  }

  public UserTableDescriptor getUserTableDescriptor()
  {
    return userTableDescriptor;
  }

  public void setUserTableDescriptor(UserTableDescriptor userTableDescriptor)
  {
    this.userTableDescriptor = userTableDescriptor;
  }

  public HistoryContent getHistoryContent()
  {
    return historyContent;
  }

  public void setHistoryContent(HistoryContent historyContent)
  {
    this.historyContent = historyContent;
  }

}
