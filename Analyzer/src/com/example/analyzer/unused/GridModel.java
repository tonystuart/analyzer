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
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;

public class GridModel
{
  private Table table;

  public GridModel(Table table)
  {
    this.table = table;
  }

  public List<ModelData> getList()
  {
    ArrayList<ModelData> listStore = new ArrayList<ModelData>();
    int rowCount = table.getRowCount();
    int columnCount = table.getColumnCount();
    for (int rowIndex = 0; rowIndex < rowCount; rowIndex++)
    {
      BaseModelData baseModelData = new BaseModelData();
      for (int columnIndex = 0; columnIndex < columnCount; columnIndex++)
      {
        String property = table.getColumnName(columnIndex);
        Object value = table.getValueAt(rowIndex, columnIndex);
        baseModelData.set(property, value);
      }
      listStore.add(baseModelData);
    }
    return listStore;
  }

  public ListStore<ModelData> getListStore()
  {
    ListStore<ModelData> listStore = new ListStore<ModelData>();
    List<ModelData> list = getList();
    listStore.add(list);
    return listStore;
  }

  public ColumnModel getColumnModel()
  {
    List<ColumnConfig> columnConfigs = new ArrayList<ColumnConfig>();
    int columnCount = table.getColumnCount();
    for (int columnIndex = 0; columnIndex < columnCount; columnIndex++)
    {
      String columnName = table.getColumnName(columnIndex);
      ColumnConfig columnConfig = new ColumnConfig(columnName, columnName, 150);
      columnConfigs.add(columnConfig);
    }
    ColumnModel columnModel = new ColumnModel(columnConfigs);
    return columnModel;
  }

}
