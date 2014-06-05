// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import com.extjs.gxt.ui.client.util.Rectangle;


public interface ManagedPanel
{

  public ManagedItem getManagedItem(int itemOffset);

  public int getManagedItemCount();

  public boolean insertManagedItem(ManagedItem managedItem, int itemOffset);

  public boolean removeManagedItem(ManagedItem managedItem);

  public void selectManagedItem(ManagedItem managedItem);

  public Rectangle getToBounds(int visualOffset);

  public Rectangle getFromBounds(ManagedItem managedItem);

}
