// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client;

import com.example.analyzer.client.views.AddCalculatedColumns;
import com.example.analyzer.client.views.AddExpandedColumns;
import com.example.analyzer.client.views.ChangeColumnNames;
import com.example.analyzer.client.views.JoinTablesHorizontally;
import com.example.analyzer.client.views.JoinTablesVertically;
import com.example.analyzer.client.views.SelectColumns;
import com.example.analyzer.client.views.SelectRows;
import com.example.analyzer.client.views.ShowDatabaseConnections;
import com.example.analyzer.client.views.SortTable;
import com.example.analyzer.client.views.SummarizeColumns;
import com.example.analyzer.shared.Keys;
import com.example.analyzer.shared.TableType;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.data.ModelIconProvider;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class TableTypeIconProvider implements ModelIconProvider<ModelData>
{
  private static TableTypeIconProvider instance = new TableTypeIconProvider();

  private TableTypeIconProvider()
  {
  }

  public static TableTypeIconProvider getInstance()
  {
    return instance;
  }

  @Override
  public AbstractImagePrototype getIcon(ModelData modelData)
  {
    AbstractImagePrototype icon;
    TableType tableType = modelData.get(Keys.TABLE_TYPE);
    switch (tableType)
    {
      case ADD_CALCULATED_COLUMNS:
        icon = AddCalculatedColumns.getInstance().getIcon();
        break;
      case ADD_EXPANDED_COLUMNS:
        icon = AddExpandedColumns.getInstance().getIcon();
        break;
      case CHANGE_COLUMN_NAMES:
        icon = ChangeColumnNames.getInstance().getIcon();
        break;
      case JOIN_TABLES_HORIZONTALLY:
        icon = JoinTablesHorizontally.getInstance().getIcon();
        break;
      case JOIN_TABLES_VERTICALLY:
        icon = JoinTablesVertically.getInstance().getIcon();
        break;
      case OPEN_TABLE:
        icon = ShowDatabaseConnections.getInstance().getTableIcon(); // NB: use special table icon instead of view icon
        break;
      case SELECT_COLUMNS:
        icon = SelectColumns.getInstance().getIcon();
        break;
      case SELECT_ROWS:
        icon = SelectRows.getInstance().getIcon();
        break;
      case SORT_TABLE:
        icon = SortTable.getInstance().getIcon();
        break;
      case SUMMARIZE_COLUMNS:
        icon = SummarizeColumns.getInstance().getIcon();
        break;
      default:
        icon = null;
        break;
    }
    return icon;
  }
}
