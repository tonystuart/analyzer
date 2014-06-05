// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.dialogs;

import java.util.List;

import com.example.analyzer.client.Analyzer;
import com.example.analyzer.extgwt.tools.layout.constrained.ConstrainedLayoutContainer;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.shared.ArrayModelData;
import com.example.analyzer.shared.ColumnDescriptor;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.user.client.Element;

public class RowDetails extends Window
{
  private static RowDetails instance;

  public synchronized static RowDetails getInstance()
  {
    if (RowDetails.instance == null)
    {
      RowDetails.instance = new RowDetails();
    }
    return RowDetails.instance;
  }

  private RowDetailsPanel rowDetailsPanel = new RowDetailsPanel();

  public void display(String heading, int rowNumber, List<ColumnDescriptor> columnDescriptors, ArrayModelData arrayModelData)
  {
    rowDetailsPanel.display(heading, rowNumber, columnDescriptors, arrayModelData);
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);
    setLayout(new FitLayout());
    add(rowDetailsPanel);
    setSize(Analyzer.DEFAULT_POPUP_WIDTH, Analyzer.DEFAULT_POPUP_HEIGHT);
  }

  private final class RowDetailsPanel extends ConstrainedLayoutContainer
  {
    private Html html;

    public RowDetailsPanel()
    {
      html = new Html();
      html.setStyleAttribute("overflow", "auto");
    }

    public void display(String heading, int rowNumber, List<ColumnDescriptor> columnDescriptors, ArrayModelData arrayModelData)
    {
      setHeading("Row " + rowNumber + " in " + heading);
      int offset = 0;
      String[] values = arrayModelData.getData();
      StringBuilder s = new StringBuilder();
      s.append("<table class='row-details'>\n");
      for (ColumnDescriptor columnDescriptor : columnDescriptors)
      {
        String name = columnDescriptor.getColumnName();
        String value = values[offset++];
        s.append("<tr><td class='row-details-name'>");
        s.append(name);
        s.append("</td><td class='row-details-value'>");
        s.append(value);
        s.append("</td></tr>\n");
      }
      s.append("</table>\n");
      html.setHtml(s.toString());
    }

    @Override
    protected void onRender(Element parent, int index)
    {
      super.onRender(parent, index);
      addStyleName("dm-form-font"); // Let the layout know what font we're using
      add(html, new Constraint("w=1,h=1"));
    }
  }

}
