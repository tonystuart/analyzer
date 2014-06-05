// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.server.expression;

public class Variable
{
  private Value value = Value.NULL; // Uninitialized variables are defined to be null

  public Value getValue()
  {
    return value;
  }

  public void setValue(Value value)
  {
    this.value = value;
  }

  @Override
  public String toString()
  {
    return value.toString();
  }

}
