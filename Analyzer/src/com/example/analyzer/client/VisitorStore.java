// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client;

import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;

public class VisitorStore<M extends ModelData> extends ListStore<M>
{
  public void visit(StoreVisitor<M> storeVisitor)
  {
    for (M modelData : all)
    {
      if (!storeVisitor.visitConditional(modelData))
      {
        return;
      }
    }
  }
}
