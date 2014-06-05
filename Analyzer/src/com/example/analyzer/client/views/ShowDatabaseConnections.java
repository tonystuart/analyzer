// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.views;

import com.example.analyzer.client.Analyzer;
import com.example.analyzer.client.Resources;
import com.example.analyzer.client.dialogs.CreateConnection;
import com.example.analyzer.client.widgets.MergeTreePanel;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.shared.ConnectionContent;
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelSelectionModel;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class ShowDatabaseConnections extends MergeTreePanel<ConnectionContent>
{
  private static ShowDatabaseConnections instance;

  public static ShowDatabaseConnections getInstance()
  {
    return ShowDatabaseConnections.instance;
  }
  private Button createButton;
  private CreateConnection createConnection = new CreateConnection();
  private Button deleteButton;
  private Button openButton;

  private Button refreshButton;

  private Button updateButton;

  public ShowDatabaseConnections(String heading)
  {
    super(heading, Analyzer.getInstance().getConnectionMergeTreeStore());
    setInstance(this);
    setIcon(Resources.DATABASE);
  }

  public void create()
  {
    createConnection.show();
    createConnection.toFront();
    createConnection.focus();
  }

  public void delete()
  {

  }
     public AbstractImagePrototype getTableIcon()
     {
       return Resources.TABLE;
     }
  

  @Override
  public void onLeafDoubleClick()
  {
    openTable();
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);
    
    treePanel.setIconProvider(new DatabaseIconProvider());
    
    add(new Label(), new Constraint("w=-1"));

    createButton = new Button("Create", new CreateListener());
    createButton.setToolTip("Create a new database connection");
    add(createButton, new Constraint("s,t=5,b=5,l=5"));

    updateButton = new Button("Update", new UpdateListener());
    updateButton.setToolTip("Update selected database connection");
    add(updateButton, new Constraint("s,t=5,b=5,l=5"));

    deleteButton = new Button("Delete", new DeleteListener());
    deleteButton.setToolTip("Delete selected database connection");
    add(deleteButton, new Constraint("s,t=5,b=5,l=5"));

    refreshButton = new Button("Refresh", new RefreshListener());
    refreshButton.setToolTip("Refresh contents of existing connections");
    add(refreshButton, new Constraint("s,t=5,b=5,l=5"));

    openButton = new Button("Open", new OpenListener());
    openButton.setToolTip("Open selected table");
    add(openButton, new Constraint("s,t=5,b=5,l=5,r=5"));

    TreePanelSelectionModel<ModelData> selectionModel = treePanel.getSelectionModel();
    selectionModel.setSelectionMode(SelectionMode.SINGLE);
  }

  @Override
  public void onSelectionChanged(ModelData selectedItem)
  {
    updateFormState();
  }

  public void openTable()
  {
    ModelData selectedItem = treePanel.getSelectionModel().getSelectedItem();
    if (selectedItem instanceof BaseTreeModel)
    {
      TreeModel treeModel = (TreeModel)selectedItem;
      if (treeModel.getChildCount() == 0)
      {
        String tableName = treeModel.get(Keys.NAME);
        TreeModel schemaTreeModel = treeModel.getParent();
        String schemaName = schemaTreeModel.get(Keys.NAME);
        TreeModel connectionTreeModel = schemaTreeModel.getParent();
        String connectionName = connectionTreeModel.get(Keys.NAME);
        Analyzer.getInstance().openTable(connectionName, schemaName, tableName);
      }
    }
  }

  public void refresh()
  {
    Analyzer.getInstance().refreshState();
  }

  protected void setInstance(ShowDatabaseConnections showDatabaseConnections)
  {
    ShowDatabaseConnections.instance = showDatabaseConnections;
  }

  public void update()
  {

  }

  public void updateConnections(String newConnectionName)
  {
    treePanel.expandAll();
    updateFormState();
    if (newConnectionName != null)
    {
      selectItem(Keys.NAME, newConnectionName);
    }
  }

  public void updateFormState()
  {
    ModelData selectedItem = treePanel.getSelectionModel().getSelectedItem();
    boolean isConnection;
    boolean isTable;
    if (selectedItem == null)
    {
      isConnection = false;
      isTable = false;
    }
    else
    {
   // See similar logic in getIcon()
      isConnection = mergeTreeStore.getParent(selectedItem) == null;
      isTable = treePanel.isLeaf(selectedItem);
    }
    updateButton.setEnabled(isConnection);
    deleteButton.setEnabled(isConnection);
    openButton.setEnabled(isTable);
  }

  private final class CreateListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      create();
    }
  }
     private final class DatabaseIconProvider implements ModelIconProvider<ModelData>
     {
       @Override
       public AbstractImagePrototype getIcon(ModelData modelData)
       {
         AbstractImagePrototype icon;
  
         // See similar logic in updateFormState()
         if (mergeTreeStore.getParent(modelData) == null)
         {
           icon = Resources.DATABASE_CONNECT;
         }
         else if (treePanel.isLeaf(modelData))
         {
           icon = getTableIcon();
         }
         else
         {
           icon = Resources.TABLE_MULTIPLE;
         }
         return icon;
       }
     }
  

  private final class DeleteListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      delete();
    }
  }

  private final class OpenListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      openTable();
    }
  }

  private final class RefreshListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      refresh();
    }
  }

  private final class UpdateListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      update();
    }
  }

}
