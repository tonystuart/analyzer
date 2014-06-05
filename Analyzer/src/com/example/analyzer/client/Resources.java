// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client;

import com.example.analyzer.client.icons.Icons;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.AbstractImagePrototype;

public class Resources
{
  public static final Icons ICONS = GWT.create(Icons.class);

  public static final AbstractImagePrototype APPLICATION_GET = AbstractImagePrototype.create(ICONS.getApplicationGet());
  public static final AbstractImagePrototype APPLICATION_GO = AbstractImagePrototype.create(ICONS.getApplicationGo());
  public static final AbstractImagePrototype APPLICATION_TILE_HORIZONTAL = AbstractImagePrototype.create(ICONS.getApplicationTileHorizontal());
  public static final AbstractImagePrototype APPLICATION_TILE_VERTICAL = AbstractImagePrototype.create(ICONS.getApplicationTileVertical());
  public static final AbstractImagePrototype CALCULATOR_ADD = AbstractImagePrototype.create(ICONS.getCalculatorAdd());
  public static final AbstractImagePrototype CHART_ORGANIZATION = AbstractImagePrototype.create(ICONS.getChartOrganization());
  public static final AbstractImagePrototype DATABASE = AbstractImagePrototype.create(ICONS.getDatabase());
  public static final AbstractImagePrototype DATABASE_CONNECT = AbstractImagePrototype.create(ICONS.getDatabaseConnect());
  public static final AbstractImagePrototype FOLDER_DATABASE = AbstractImagePrototype.create(ICONS.getFolderDatabase());
  public static final AbstractImagePrototype FOLDER_TABLE = AbstractImagePrototype.create(ICONS.getFolderTable());
  public static final AbstractImagePrototype REPORT = AbstractImagePrototype.create(ICONS.getReport());
  public static final AbstractImagePrototype REPORT_GO = AbstractImagePrototype.create(ICONS.getReportGo());
  public static final AbstractImagePrototype TABLE = AbstractImagePrototype.create(ICONS.getTable());
  public static final AbstractImagePrototype TABLE_ADD = AbstractImagePrototype.create(ICONS.getTableAdd());
  public static final AbstractImagePrototype TABLE_MULTIPLE = AbstractImagePrototype.create(ICONS.getTableMultiple());
  public static final AbstractImagePrototype TABLE_SORT = AbstractImagePrototype.create(ICONS.getTableSort());
  public static final AbstractImagePrototype TEXT_REPLACE = AbstractImagePrototype.create(ICONS.getTextReplace());
}
