// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

public interface ManagedItem
{

  String getHeading();

  boolean isCollapsed(); // could use isVisible if respected for both AccordionItem and TabItem

  void activate();

  ViewPanel getViewPanel();

  void deactivate();

}
