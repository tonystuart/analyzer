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

import com.extjs.gxt.ui.client.data.BaseTreeModel;

public class ConnectionContent extends BaseTreeContent implements Serializable
{
  private String connectionName;

  public ConnectionContent()
  {
  }

  public ConnectionContent(BaseTreeModel baseTreeModel)
  {
    super(baseTreeModel);
  }

  public ConnectionContent(String connectionName, BaseTreeModel baseTreeModel)
  {
    super(baseTreeModel);
    this.connectionName = connectionName;
  }

  public String getConnectionName()
  {
    return connectionName;
  }

  public void setConnectionName(String connectionName)
  {
    this.connectionName = connectionName;
  }

}
