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

public class SerializableClasses implements Serializable
{
  // Add references to Serializable classes that are not part of the service method signature (e.g. passed within ModelData)
  public UserTableDescriptor userTableDescriptor;
  public ArrayModelData arrayModelData;
  public DbCompareOperation compareOperation;
  public TableType tableType;

  public SerializableClasses()
  {
  }

}
