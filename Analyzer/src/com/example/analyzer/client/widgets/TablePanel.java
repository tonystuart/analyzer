// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.example.analyzer.client.Analyzer;
import com.example.analyzer.client.dialogs.RowDetails;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.jdbc.shared.ColumnTypes;
import com.example.analyzer.shared.ArrayModelData;
import com.example.analyzer.shared.ColumnDescriptor;
import com.example.analyzer.shared.DbCompareOperation;
import com.example.analyzer.shared.DwSummaryOperation;
import com.example.analyzer.shared.PageResult;
import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BasePagingLoadConfig;
import com.extjs.gxt.ui.client.data.BasePagingLoader;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.PagingLoadConfig;
import com.extjs.gxt.ui.client.data.PagingLoader;
import com.extjs.gxt.ui.client.data.RpcProxy;
import com.extjs.gxt.ui.client.event.ColumnModelEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.GridEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnHeader;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.extjs.gxt.ui.client.widget.grid.GridView;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.menu.SeparatorMenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.PagingToolBar;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class TablePanel extends ViewPanel
{
  private static final int MAX_CHARACTERS = 20;
  private static final int MAX_COLUMN_WIDTH_CHECK_ROWS = 10;
  private static final int PIXEL_WIDTH_PER_CHARACTER = 10;

  private List<ColumnDescriptor> columnDescriptors;
  private Grid<ModelData> grid;
  private boolean initialized;
  private PagingLoader<PageResult> loader;
  private PagingToolBar toolBar;
  private int userTableId;

  public TablePanel(int userTableId, List<ColumnDescriptor> columnDescriptors, String description)
  {
    super(description);
    this.userTableId = userTableId;
    this.columnDescriptors = columnDescriptors;
  }

  public void adjustWidth(List<ModelData> data)
  {
    ColumnHeader columnHeader = grid.getView().getHeader();
    int minimumColumnWidth = columnHeader.getMinColumnWidth();
    ColumnModel columnModel = grid.getColumnModel();
    int columnCount = columnModel.getColumnCount();
    for (int columnOffset = 0; columnOffset < columnCount; columnOffset++)
    {
      int columnWidthInCharacters = getColumnWidthInCharacters(data, columnModel, columnOffset);
      if (columnWidthInCharacters > MAX_CHARACTERS)
      {
        columnWidthInCharacters = MAX_CHARACTERS;
      }
      int columnWidthInPixels = columnWidthInCharacters * PIXEL_WIDTH_PER_CHARACTER;
      columnWidthInPixels = Math.max(minimumColumnWidth, columnWidthInPixels);
      columnModel.setColumnWidth(columnOffset, columnWidthInPixels, false); // Notifies GridView.onColumnWidthChanged, which does nothing because it's not forceFit or stateful
    }
  }

  private void displayDetails(GridEvent<ModelData> be)
  {
    int rowOffset = be.getRowIndex();
    boolean isCreateNewWindow = be.getEvent().getCtrlKey();
    displayDetails(rowOffset, isCreateNewWindow);
  }

  public void displayDetails(int rowOffset, boolean isCreateNewWindow)
  {
    ModelData selectedItem = grid.getSelectionModel().getSelectedItem();
    if (selectedItem != null)
    {
      RowDetails rowDetails;
      if (isCreateNewWindow)
      {
        rowDetails = new RowDetails();
      }
      else
      {
        rowDetails = RowDetails.getInstance();
      }
      int rowNumber = (toolBar.getActivePage() - 1) * toolBar.getPageSize() + rowOffset + 1;
      rowDetails.display(getHeading(), rowNumber, columnDescriptors, (ArrayModelData)selectedItem);
      rowDetails.show();
      rowDetails.toFront();
      rowDetails.focus();
    }
  }

  private int getColumnWidthInCharacters(List<ModelData> modelDataList, ColumnModel columnModel, int columnOffset)
  {
    ColumnConfig columnConfig = columnModel.getColumn(columnOffset);
    String id = columnConfig.getId();
    int columnWidth = columnModel.getColumnHeader(columnOffset).length();
    for (ModelData modelData : new MaximumItemIterable<ModelData>(modelDataList, MAX_COLUMN_WIDTH_CHECK_ROWS))
    {
      if (modelData != null)
      {
        Object value = modelData.get(id);
        if (value != null)
        {
          int width = value.toString().length();
          if (width > columnWidth)
          {
            columnWidth = width;
          }
        }
      }
    }
    return columnWidth;
  }

  public void getFirstPage()
  {
    PagingLoadConfig config = new BasePagingLoadConfig();
    config.setOffset(0);
    config.setLimit(50);

    Map<String, Object> state = grid.getState();
    if (state.containsKey("offset"))
    {
      int offset = (Integer)state.get("offset");
      int limit = (Integer)state.get("limit");
      config.setOffset(offset);
      config.setLimit(limit);
    }
    loader.load(config);
  }

  public int getUserTableId()
  {
    return userTableId;
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    RpcProxy<PageResult> proxy = new PageResultRpcProxy();
    loader = new BasePagingLoader<PageResult>(proxy);
    ListStore<ModelData> listStore = new ListStore<ModelData>(loader);

    toolBar = new PagingToolBar(50);
    toolBar.bind(loader);

    int columnOffset = 0;
    List<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
    for (ColumnDescriptor columnDescriptor : columnDescriptors)
    {
      String id = Integer.toString(columnOffset);
      String columnName = columnDescriptor.getColumnName();
      ColumnConfig columnConfig = new ColumnConfig();
      columnConfig.setId(id);
      columnConfig.setHeader(columnName);
      int columnType = columnDescriptor.getColumnType();
      if (ColumnTypes.isNumber(columnType))
      {
        columnConfig.setAlignment(HorizontalAlignment.RIGHT);
      }
      else if (columnType == ColumnTypes.DATE)
      {
        columnConfig.setDateTimeFormat(DateTimeFormat.getShortDateFormat());
      }
      columnConfigs.add(columnConfig);
      columnOffset++;
    }

    final ColumnModel columnModel = new ColumnModel(columnConfigs);
    columnModel.addListener(Events.HiddenChange, new Listener<ColumnModelEvent>()
    {
      @Override
      public void handleEvent(ColumnModelEvent be)
      {
        Analyzer.getInstance().configureSelectedColumns(columnModel);
      }
    });

    grid = new Grid<ModelData>(listStore, columnModel);
    grid.setLoadMask(true);
    grid.setBorders(false);
    // TODO: Add stripeRows and trackMouseOver to "View Settings" view
    //grid.setStripeRows(true);
    //grid.setTrackMouseOver(false);
    grid.setView(new EnhancedContextMenuGridView());
    grid.addListener(Events.CellDoubleClick, new CellDoubleClickGridListener());

    ContentPanel panel = new ContentPanel();
    panel.setHeaderVisible(false);
    panel.setFrame(false);
    panel.setLayout(new FitLayout());
    panel.add(grid);
    panel.setBottomComponent(toolBar);

    add(panel, new Constraint("h=1,w=1"));
    getFirstPage();
  }

  private final class CellDoubleClickGridListener implements Listener<GridEvent<ModelData>>
  {
    @Override
    public void handleEvent(GridEvent<ModelData> be)
    {
      displayDetails(be);
    }

  }

  public class ColumnWidthAdjustingPageResultCallbackWrapper implements AsyncCallback<PageResult>
  {
    private AsyncCallback<PageResult> callback;

    public ColumnWidthAdjustingPageResultCallbackWrapper(AsyncCallback<PageResult> callback)
    {
      this.callback = callback;
    }

    @Override
    public void onFailure(Throwable caught)
    {
      callback.onFailure(caught);
    }

    @Override
    public void onSuccess(PageResult result)
    {
      initialized = true;
      adjustWidth(result.getData());
      callback.onSuccess(result);
    }

  }

  private final class EnhancedContextMenuGridView extends GridView
  {
    @Override
    protected Menu createContextMenu(final int columnOffset)
    {
      final String columnName = columnDescriptors.get(columnOffset).getColumnName();
      final String value = getSelectedItemValue(columnOffset);

      // TODO: Consider delegating menu building to Analyzer or to individual views

      final Menu menu = super.createContextMenu(columnOffset);

      menu.add(new SeparatorMenuItem());

      MenuItem item = new MenuItem();
      item.setText("Join on Column (Left)");
      // TODO: item.setIcon(getImages().getSortAsc());
      item.addSelectionListener(new SelectionListener<MenuEvent>()
      {
        public void componentSelected(MenuEvent ce)
        {
          Analyzer.getInstance().configureLeftJoinColumn(userTableId, columnName);
        }
      });
      menu.add(item);

      item = new MenuItem();
      item.setText("Join on Column (Right)");
      // TODO: item.setIcon(getImages().getSortDesc());
      item.addSelectionListener(new SelectionListener<MenuEvent>()
      {
        public void componentSelected(MenuEvent ce)
        {
          Analyzer.getInstance().configureRightJoinColumn(userTableId, columnName);
        }
      });
      menu.add(item);

      menu.add(new SeparatorMenuItem());

      item = new MenuItem();
      item.setText("Select Rows");
      // TODO: columns.setText(GXT.MESSAGES.gridView_columnsText());
      // TODO: columns.setIcon(getImages().getColumns());
      Menu selectRowsMenu = new Menu();
      for (DbCompareOperation compareOperation : DbCompareOperation.values())
      {
        MenuItem subMenuItem = new MenuItem();
        subMenuItem.setText(compareOperation.name());
        subMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
        {
          public void componentSelected(MenuEvent ce)
          {
            Component item = ce.getItem();
            if (item instanceof MenuItem)
            {
              String compareOperation = ((MenuItem)item).getText();
              Analyzer.getInstance().configureCompareOperation(userTableId, columnOffset, compareOperation, value);
            }
          }
        });
        selectRowsMenu.add(subMenuItem);
      }
      item.setSubMenu(selectRowsMenu);
      menu.add(item);

      menu.add(new SeparatorMenuItem());

      item = new MenuItem();
      item.setText("Group By");
      // TODO: item.setIcon(getImages().getSortDesc());
      item.addSelectionListener(new SelectionListener<MenuEvent>()
      {
        public void componentSelected(MenuEvent ce)
        {
          Analyzer.getInstance().configureGroupingColumn(userTableId, columnName);
        }
      });
      menu.add(item);

      item = new MenuItem();
      item.setText("Summarize By");
      // TODO: columns.setText(GXT.MESSAGES.gridView_columnsText());
      // TODO: columns.setIcon(getImages().getColumns());
      Menu summaryMenu = new Menu();
      for (DwSummaryOperation summaryOperation : DwSummaryOperation.values())
      {
        MenuItem subMenuItem = new MenuItem();
        subMenuItem.setText(summaryOperation.name());
        subMenuItem.addSelectionListener(new SelectionListener<MenuEvent>()
        {
          public void componentSelected(MenuEvent ce)
          {
            Component item = ce.getItem();
            if (item instanceof MenuItem)
            {
              String summaryOperation = ((MenuItem)item).getText();
              Analyzer.getInstance().configureSummaryOperation(userTableId, columnName, summaryOperation);
            }
          }
        });
        summaryMenu.add(subMenuItem);
      }
      item.setSubMenu(summaryMenu);
      menu.add(item);

      return menu;
    }

    private String getSelectedItemValue(final int columnOffset)
    {
      ModelData selectedItem = grid.getSelectionModel().getSelectedItem();
      final String value = selectedItem == null ? "" : ((ArrayModelData)selectedItem).getData()[columnOffset];
      return value;
    }

    @Override
    protected void doSort(int colIndex, SortDir sortDir)
    {
      // Do not invoke base class
      Analyzer.getInstance().configureSortColumn(colIndex, sortDir);
    }

  }

  private final class PageResultRpcProxy extends RpcProxy<PageResult>
  {
    @Override
    public void load(Object loadConfig, final AsyncCallback<PageResult> callback)
    {
      // NB: loadConfig is the BasePagingLoadConfig created by getFirstPage and reused by PagingToolBar when isReuseConfig() is true
      if (initialized)
      {
        Analyzer.getInstance().getPage(userTableId, (PagingLoadConfig)loadConfig, callback);
      }
      else
      {
        Analyzer.getInstance().getPage(userTableId, (PagingLoadConfig)loadConfig, new ColumnWidthAdjustingPageResultCallbackWrapper(callback));
      }
    }
  }

}
