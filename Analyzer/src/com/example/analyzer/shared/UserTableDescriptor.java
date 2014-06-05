// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.shared;

import java.io.Serializable;
import java.util.List;

public class UserTableDescriptor implements Serializable
{
  private int userTableId;
  private List<ColumnDescriptor> columnDescriptors;
  private String description;

  public UserTableDescriptor()
  {
  }

  public UserTableDescriptor(int userTableId, List<ColumnDescriptor> columnDescriptors, String description)
  {
    this.userTableId = userTableId;
    this.columnDescriptors = columnDescriptors;
    this.description = description;
  }

  public int getUserTableId()
  {
    return userTableId;
  }

  public void setUserTableId(int userTableId)
  {
    this.userTableId = userTableId;
  }

  public List<ColumnDescriptor> getColumnDescriptors()
  {
    return columnDescriptors;
  }

  public void setColumnDescriptors(List<ColumnDescriptor> columnDescriptors)
  {
    this.columnDescriptors = columnDescriptors;
  }

  public String getDescription()
  {
    return description;
  }

  public void setDescription(String description)
  {
    this.description = description;
  }

}
