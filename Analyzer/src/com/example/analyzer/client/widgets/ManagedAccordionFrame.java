// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import com.example.analyzer.client.views.AddCalculatedColumns;
import com.example.analyzer.client.views.AddExpandedColumns;
import com.example.analyzer.client.views.ChangeColumnNames;
import com.example.analyzer.client.views.JoinTablesHorizontally;
import com.example.analyzer.client.views.JoinTablesVertically;
import com.example.analyzer.client.views.SelectColumns;
import com.example.analyzer.client.views.SelectRows;
import com.example.analyzer.client.views.ShowDatabaseConnections;
import com.example.analyzer.client.views.ShowHistory;
import com.example.analyzer.client.views.ShowUserDefinedQueries;
import com.example.analyzer.client.views.ShowUserDefinedViews;
import com.example.analyzer.client.views.SortTable;
import com.example.analyzer.client.views.SummarizeColumns;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Status;
import com.extjs.gxt.ui.client.widget.layout.AccordionLayout;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.Widget;

public class ManagedAccordionFrame extends ContentPanel implements ManagedPanel
{
  private static ManagedAccordionFrame instance;

  public static ManagedAccordionFrame getInstance()
  {
    return ManagedAccordionFrame.instance;
  }

  private ManagedAccordionLayout accordionLayout;
  private Status status;
  private ViewManager viewManager;

  public ManagedAccordionFrame()
  {
    setInstance(this);
    setBodyBorder(false);
    setBottomComponent(getToolBar());
    viewManager = new ViewManager(this);
  }

  private void addViewPanel(ViewPanel viewPanel)
  {
    ManagedAccordionItem managedAccordionItem = new ManagedAccordionItem(viewManager, viewPanel);
    add(managedAccordionItem);
    viewManager.addManagedItem(managedAccordionItem);
  }

  public void clearStatus()
  {
    status.clearStatus(null);
  }

  @Override
  public Rectangle getFromBounds(ManagedItem managedItem)
  {
    Component component = (Component)managedItem;
    Rectangle fromBounds = new Rectangle(component.getAbsoluteLeft(), component.getAbsoluteTop(), component.getOffsetWidth(), component.getOffsetHeight());
    return fromBounds;
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

  @Override
  public Rectangle getToBounds(int visualOffset)
  {
    Component component;
    int itemCount = getItemCount();
    if (visualOffset < itemCount)
    {
      // Insert before last item
      component = getItem(visualOffset);
    }
    else if (itemCount > 0)
    {
      // Insert last item
      component = getItem(itemCount - 1);
    }
    else
    {
      // ManagedPanel is empty... insert new item
      component = this;
    }
    Rectangle toBounds = new Rectangle(component.getAbsoluteLeft(), component.getAbsoluteTop(), component.getOffsetWidth(), component.getOffsetHeight());
    return toBounds;
  }

  private ToolBar getToolBar()
  {
    ToolBar toolBar = new ToolBar();
    status = new Status();
    toolBar.add(status);
    toolBar.setHeight(27); // TODO: Figure out how to automatically make status toolbar same height as paging toolbar
    return toolBar;
  }

  @Override
  public boolean insertManagedItem(ManagedItem managedItem, int itemOffset)
  {
    return insert((Widget)managedItem, itemOffset);
  }

  public void morphIn(String heading)
  {
    viewManager.morphIn(heading);
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    setHeading("Navigator");
    setBorders(false);
    // TODO: setIcon(Resources.ICONS.accordion());

    accordionLayout = new ManagedAccordionLayout();
    accordionLayout.setHideCollapseTool(true);
    accordionLayout.setTitleCollapse(true);
    setLayout(accordionLayout);

    addViewPanel(new AddCalculatedColumns("Add Calculated Columns"));
    addViewPanel(new AddExpandedColumns("Add Expanded Columns"));
    addViewPanel(new ChangeColumnNames("Change Column Names"));
    addViewPanel(new JoinTablesHorizontally("Join Tables Horizontally"));
    addViewPanel(new JoinTablesVertically("Join Tables Vertically"));
    addViewPanel(new SelectColumns("Select Columns"));
    addViewPanel(new SelectRows("Select Rows"));
    addViewPanel(new ShowDatabaseConnections("Show Database Connections and Tables"));
    addViewPanel(new ShowHistory("Show History"));
    addViewPanel(new ShowUserDefinedQueries("Show User Defined Queries"));
    addViewPanel(new ShowUserDefinedViews("Show User Defined Views"));
    addViewPanel(new SortTable("Sort Table"));
    addViewPanel(new SummarizeColumns("Summarize Columns"));

    setLayoutOnChange(true); // Required for reinserting a ViewPanel
  }

  @Override
  public boolean removeManagedItem(ManagedItem managedItem)
  {
    return remove((Component)managedItem);
  }

  @Override
  public void selectManagedItem(ManagedItem managedItem)
  {
    accordionLayout.setActiveItem((Component)managedItem);
  }

  public void selectViewPanel(ViewPanel viewPanel)
  {
    viewManager.selectViewPanel(viewPanel);
  }

  public void setStatusBusy(String message)
  {
    status.setBusy(message);
  }

  public void setStatusInfo(String message)
  {
    status.setStatus(message, null);
  }

  private void setInstance(ManagedAccordionFrame managedAccordionFrame)
  {
    ManagedAccordionFrame.instance = managedAccordionFrame;
  }

  public class ManagedAccordionLayout extends AccordionLayout
  {
    @Override
    public void setActiveItem(Component component)
    {
      if (activeItem != null && activeItem != component)
      {
        ((ContentPanel)activeItem).collapse();
      }
      super.setActiveItem(component);
    }
  }

}
