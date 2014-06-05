// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.shared;

import java.util.HashMap;

public final class EnumText
{
  // TODO: Figure out best way to get i18n text to client. Note that enum values are needed before first async call completes.
  private static EnumText instance = new EnumText();
  private HashMap<Enum<?>, String> enumText = new HashMap<Enum<?>, String>();

  public static EnumText getInstance()
  {
    return instance;
  }

  private EnumText()
  {
    enumText.put(DbCompareOperation.Contains, "Contains");
    enumText.put(DbCompareOperation.LessThan, "Less than");
    enumText.put(DbCompareOperation.LessThanOrEqualTo, "Less than or equal to");
    enumText.put(DbCompareOperation.EqualTo, "Equal to");
    enumText.put(DbCompareOperation.GreaterThanOrEqualTo, "Greater than or equal to");
    enumText.put(DbCompareOperation.GreaterThan, "Greater than");
    enumText.put(DbCompareOperation.NotEqualTo, "Not equal to");
  }

  public String get(Enum<?> enumValue)
  {
    String text = enumText.get(enumValue);
    if (text == null)
    {
      text = enumValue.name();
    }
    return text;
  }

  public void put(Enum<?> enumItem, String text)
  {
    enumText.put(enumItem, text);
  }

}
