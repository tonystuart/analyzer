// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.unused;

import java.util.ArrayList;
import java.util.List;

import com.example.analyzer.jdbc.shared.Table;
import com.extjs.gxt.ui.client.data.BaseTreeModel;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.TreeModel;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.treegrid.TreeGridCellRenderer;

public class TreeGridModel
{
  private Table table;
  private int levels;
  private String titleId;

  public TreeGridModel(Table table, int levels)
  {
    this.table = table;
    this.levels = levels;
    this.titleId = table.getColumnName(levels);
    System.out.println("titleId="+titleId);
  }

  public void getTree(TreeModel parent, int firstRow, int lastRow, int columnIndex)
  {
    int partitionFirstRow = -1;
    BaseTreeModel child = null;
    for (int rowIndex = firstRow, nextIndex = firstRow + 1; rowIndex <= lastRow; rowIndex++, nextIndex++)
    {
      String value = table.get(rowIndex, columnIndex);
      if (partitionFirstRow == -1)
      {
        child = new BaseTreeModel(parent);
        child.set(titleId, value);
        partitionFirstRow = rowIndex;
        String columnName = table.getColumnName(columnIndex);
        child.set("type", columnName);
      }
      if (nextIndex > lastRow || !table.get(nextIndex, columnIndex).equals(value))
      {
        int nextColumnIndex = columnIndex + 1;
        if (nextColumnIndex < levels)
        {
          getTree(child, partitionFirstRow, rowIndex, nextColumnIndex);
        }
        else
        {
          getColumns(child, partitionFirstRow, rowIndex, nextColumnIndex);
        }
        partitionFirstRow = -1;
      }
    }
  }

  private void getColumns(BaseTreeModel child, int firstRow, int lastRow, int nextColumnIndex)
  {
    int columnCount = table.getColumnCount();
    for (int rowIndex = firstRow; rowIndex <= lastRow; rowIndex++)
    {
      BaseTreeModel baseTreeModel = new BaseTreeModel(child);
      for (int columnIndex = nextColumnIndex; columnIndex < columnCount; columnIndex++)
      {
        String columnName = table.getColumnName(columnIndex);
        String value = table.get(rowIndex, columnIndex);
        baseTreeModel.set(columnName, value);
      }
    }
  }

  public TreeStore<ModelData> getTreeStore()
  {
    int lastRow = table.getRowCount() - 1;
    BaseTreeModel root = new BaseTreeModel();
    getTree(root, 0, lastRow, 0);
    TreeStore<ModelData> treeStore = new TreeStore<ModelData>();
    treeStore.add(root.getChildren(), true);
    return treeStore;
  }

  public ColumnModel getColumnModel()
  {
    List<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
    int columnCount = table.getColumnCount();
    for (int columnIndex = levels; columnIndex < columnCount; columnIndex++)
    {
      ColumnConfig columnConfig;
      String columnName = table.getColumnName(columnIndex);
      columnConfig = new ColumnConfig(columnName, columnName, 150);
      if (columnIndex == levels)
      {
        columnConfig.setRenderer(new TreeGridCellRenderer<ModelData>());
      }
      columnConfigs.add(columnConfig);
    }
    ColumnModel columnModel = new ColumnModel(columnConfigs);
    return columnModel;
  }

}
