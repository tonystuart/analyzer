// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.views;

import com.example.analyzer.client.Resources;
import com.example.analyzer.client.widgets.AccordionViewPanel;
import com.google.gwt.user.client.Element;

public class SummarizeRows extends AccordionViewPanel
{
  private static SummarizeRows instance;

  public static SummarizeRows getInstance()
  {
    return SummarizeRows.instance;
  }

  public SummarizeRows(String heading)
  {
    super(heading);
    setInstance(this);
    setIcon(Resources.REPORT_GO);
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    updateFormState();
  }

  @Override
  protected void reconfigure()
  {
  }

  private void setInstance(SummarizeRows summarizeColumns)
  {
    SummarizeRows.instance = summarizeColumns;
  }

  @Override
  protected void updateFormState()
  {
  }

}
