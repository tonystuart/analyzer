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

public class BaseTreeContent implements Serializable
{
  protected BaseTreeModel baseTreeModel;

  public BaseTreeContent()
  {

  }

  public BaseTreeContent(BaseTreeModel baseTreeModel)
  {
    this.baseTreeModel = baseTreeModel;
  }

  public BaseTreeModel getBaseTreeModel()
  {
    return baseTreeModel;
  }

  public void setBaseTreeModel(BaseTreeModel connectionTree)
  {
    this.baseTreeModel = connectionTree;
  }

}
