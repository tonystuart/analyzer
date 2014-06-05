// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import java.util.LinkedList;
import java.util.List;

import com.example.analyzer.client.Analyzer;
import com.example.analyzer.shared.ColumnDescriptor;
import com.example.analyzer.shared.TableContent;
import com.example.analyzer.shared.UserTableDescriptor;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.event.TabPanelEvent;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.TabItem;
import com.extjs.gxt.ui.client.widget.TabPanel;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;

public class ManagedTabPanel extends TabPanel implements ManagedPanel
{

  private static ManagedTabPanel instance;

  public static ManagedTabPanel getInstance()
  {
    return ManagedTabPanel.instance;
  }

  private Rectangle bounds;
  private ViewManager viewManager;

  public ManagedTabPanel()
  {
    hide();
    setInstance(this);
    setBodyBorder(false);
    setResizeTabs(true);
    setMonitorWindowResize(true);
    addListener(Events.Select, new SelectListener());
    addListener(Events.Remove, new RemoveListener());
    viewManager = new ViewManager(this);
  }

  public void closeAll()
  {
    int itemCount;
    while ((itemCount = getItemCount()) > 0)
    {
      close(getItem(itemCount - 1));
    }
  }

  protected void closeOthers(TabItem thisTabItem)
  {
    for (TabItem currentItem : new LinkedList<TabItem>(getItems()))
    {
      if (currentItem != thisTabItem)
      {
        close(currentItem);
      }
    }
  }

  public void closeTables(Iterable<Integer> userTableIds)
  {
    for (int userTableId : userTableIds)
    {
      TabItem tabItem = findTab(userTableId);
      if (tabItem != null)
      {
        close(tabItem);
      }
    }
  }

  private ManagedTabItem findTab(int userTableId)
  {
    int itemCount = getItemCount();
    for (int itemOffset = 0; itemOffset < itemCount; itemOffset++)
    {
      ManagedTabItem managedTabItem = (ManagedTabItem)getItem(itemOffset);
      if (managedTabItem.getUserTableId() == userTableId)
      {
        return managedTabItem;
      }
    }
    return null;
  }

  private Menu getContextMenu(final TabItem tabItem, final boolean isMultipleTabs)
  {
    Menu contextMenu = new Menu();
    contextMenu.add(new MenuItem("Close", new SelectionListener<MenuEvent>()
    {
      @Override
      public void componentSelected(MenuEvent ce)
      {
        close(tabItem);
      }
    }));
    contextMenu.add(new MenuItem("Close Others", new SelectionListener<MenuEvent>()
    {
      @Override
      public void componentSelected(MenuEvent ce)
      {
        closeOthers(tabItem);
      }
    }));
    contextMenu.add(new MenuItem("Close All", new SelectionListener<MenuEvent>()
    {
      @Override
      public void componentSelected(MenuEvent ce)
      {
        closeAll();
      }
    }));
    contextMenu.getItem(1).setEnabled(isMultipleTabs);
    contextMenu.add(new SeparatorMenuItem());
    contextMenu.add(new MenuItem("Detach Table", new SelectionListener<MenuEvent>()
    {
      @Override
      public void componentSelected(MenuEvent ce)
      {
        viewManager.morphIn(((ManagedTabItem)tabItem).getHeading());
      }
    }));
    return contextMenu;
  }

  @Override
  public Rectangle getFromBounds(ManagedItem managedItem)
  {
    // Used cached bounds because managedItem's bounds are zero when when its not the active tab
    return bounds;
  }

  @Override
  public ManagedItem getManagedItem(int itemOffset)
  {
    return (ManagedItem)getItem(itemOffset);
  }

  @Override
  public int getManagedItemCount()
  {
    return getItemCount();
  }

  public int getSelectedUserTableId()
  {
    int selectedUserTableId;
    ManagedTabItem managedTabItem = (ManagedTabItem)getSelectedItem();
    if (managedTabItem == null)
    {
      selectedUserTableId = Analyzer.EMPTY_TABLE;
    }
    else
    {
      selectedUserTableId = managedTabItem.getUserTableId();
    }
    return selectedUserTableId;
  }

  @Override
  public Rectangle getToBounds(int visualOffset)
  {
    // Used cached bounds because our bounds are zero when we're hidden
    return bounds;
  }

  @Override
  public boolean insert(TabItem item, int index)
  {
    if (!isVisible())
    {
      show(); // must do before insert, else size is wrong
    }
    return super.insert(item, index);
  }

  @Override
  public boolean insertManagedItem(ManagedItem managedItem, int itemOffset)
  {
    return insert((TabItem)managedItem, itemOffset);
  }

  protected boolean isItem(TabItem tabItem)
  {
    return findItem(tabItem.getElement()) != null;
  }

  @Override
  protected void onItemContextMenu(final TabItem tabItem, int x, int y)
  {
    final boolean isMultipleTabs = getItemCount() > 1;
    Menu contextMenu = getContextMenu(tabItem, isMultipleTabs);
    contextMenu.showAt(x, y);
  }

  @Override
  protected void onResize(int width, int height)
  {
    super.onResize(width, height);
    int itemCount = getItemCount();
    for (int itemOffset = 0; itemOffset < itemCount; itemOffset++)
    {
      TabItem tabItem = getItem(itemOffset);
      tabItem.setSize(width, height);
      tabItem.layout(true);
    }
    bounds = new Rectangle(getAbsoluteLeft(), getAbsoluteTop(), width, height);
  }

  public void openTable(TableContent tableContent)
  {
    UserTableDescriptor userTableDescriptor = tableContent.getUserTableDescriptor();
    int userTableId = userTableDescriptor.getUserTableId();
    List<ColumnDescriptor> columnDescriptors = userTableDescriptor.getColumnDescriptors();
    String description = userTableDescriptor.getDescription();

    TablePanel tablePanel = new TablePanel(userTableId, columnDescriptors, description);
    ManagedTabItem managedTabItem = new ManagedTabItem(viewManager, tablePanel);
    int insertPoint = viewManager.findInsertPoint(description);
    insert(managedTabItem, insertPoint);
    setSelection(managedTabItem);
    viewManager.addManagedItem(managedTabItem);
  }

  @Override
  public boolean removeManagedItem(ManagedItem managedItem)
  {
    return remove((TabItem)managedItem);
  }

  @Override
  public void selectManagedItem(ManagedItem managedItem)
  {
    setSelection((TabItem)managedItem);
  }

  private void setInstance(ManagedTabPanel managedTabPanel)
  {
    ManagedTabPanel.instance = managedTabPanel;
  }

  public boolean showTable(int userTableId)
  {
    ManagedTabItem managedTabItem = findTab(userTableId);
    if (managedTabItem != null)
    {
      if (getSelectedItem() != managedTabItem)
      {
        setSelection(managedTabItem);
      }
      return true;
    }
    return false;
  }

  private final class RemoveListener implements Listener<TabPanelEvent>
  {
    @Override
    public void handleEvent(TabPanelEvent be)
    {
      if (getItemCount() == 0)
      {
        Analyzer.getInstance().displayTableData(Analyzer.EMPTY_TABLE);
        hide();
      }
    }
  }

  private final class SelectListener implements Listener<TabPanelEvent>
  {
    @Override
    public void handleEvent(TabPanelEvent be)
    {
      int selectedUserTableId = getSelectedUserTableId();
      Analyzer.getInstance().displayTableData(selectedUserTableId);
    }
  }

}
