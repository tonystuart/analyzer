// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import com.example.analyzer.extgwt.tools.layout.constrained.ConstrainedLayoutContainer;
import com.google.gwt.user.client.ui.AbstractImagePrototype;
import com.google.gwt.user.client.ui.Widget;

public abstract class ViewPanel extends ConstrainedLayoutContainer
{
  protected final String heading;
  private AbstractImagePrototype icon;

  public ViewPanel(String heading)
  {
    this.heading = heading;
    addStyleName("dm-form-font");
  }

  protected Widget getFocusWidget()
  {
    return null;
  }

  public String getHeading()
  {
    return heading;
  }

  public AbstractImagePrototype getIcon()
  {
    return icon;
  }

  /**
   * Workaround a bug that appeared in GXT 2.2.0 in which a ViewPanel in a
   * collapsed AccordionPanel incorrectly returns isVisible == true.
   */

  public boolean isVisibleWithGxtWorkaround()
  {
    return rendered && !hidden && el().isVisible(true);
  }

  public void setIcon(AbstractImagePrototype icon)
  {
    this.icon = icon;
  }

}
