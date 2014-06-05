// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.views;

import com.example.analyzer.client.Analyzer;
import com.example.analyzer.client.Resources;
import com.example.analyzer.client.widgets.MergeTreePanel;
import com.example.analyzer.shared.ViewContent;

public class ShowUserDefinedViews extends MergeTreePanel<ViewContent>
{
  public ShowUserDefinedViews(String heading)
  {
    super(heading, Analyzer.getInstance().getViewMergeTreeStore());
    setIcon(Resources.FOLDER_TABLE);
  }
}
