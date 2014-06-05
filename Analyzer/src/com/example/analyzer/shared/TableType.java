// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.shared;

// TODO: Determine whether this enum is necessary, or whether instanceof will suffice.
public enum TableType
{
  ADD_CALCULATED_COLUMNS, //
  ADD_EXPANDED_COLUMNS, //
  CHANGE_COLUMN_NAMES, //
  JOIN_TABLES_HORIZONTALLY, //
  JOIN_TABLES_VERTICALLY, //
  OPEN_TABLE, //
  SELECT_COLUMNS, //
  SELECT_ROWS, //
  SHOW_DATABASE_CONNECTIONS_AND_TABLES, //
  SHOW_HISTORY, //
  SHOW_USER_DEFINED_QUERIES, //
  SHOW_USER_DEFINED_VIEW, //
  SORT_TABLE, //
  SUMMARIZE_COLUMNS, //
  SUMMARIZE_ROWS, //
}
