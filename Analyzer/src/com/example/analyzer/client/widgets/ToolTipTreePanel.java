// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import java.util.Map;

import com.extjs.gxt.ui.client.core.Template;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.store.TreeStore;
import com.extjs.gxt.ui.client.util.Params;
import com.extjs.gxt.ui.client.widget.Component;
import com.extjs.gxt.ui.client.widget.tips.ToolTip;
import com.extjs.gxt.ui.client.widget.tips.ToolTipConfig;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanelView;

public class ToolTipTreePanel<M extends ModelData> extends TreePanel<M>
{
  private String toolTipPropertyName;
  private ToolTipTreePanelView<M> toolTipTreePanelView;
  private ToolTipConfig toolTipConfig;

  public ToolTipTreePanel(TreeStore<M> store, String toolTipPropertyName)
  {
    super(store);
    this.toolTipPropertyName = toolTipPropertyName;
    setView(toolTipTreePanelView = new ToolTipTreePanelView<M>());
    toolTipConfig = new ToolTipConfig();
    toolTipConfig.setTrackMouse(true);
    Template template = new Template("{" + toolTipPropertyName + "}");
    template.compile();
    toolTipConfig.setTemplate(template);
    toolTipConfig.setDismissDelay(0);
    toolTip = new TreePanelToolTip(this, toolTipConfig);
  }

  public void setHideDelay(int hideDelay)
  {
    toolTipConfig.setHideDelay(hideDelay);
  }

  public void setDismissDelay(int hideDelay)
  {
    toolTipConfig.setDismissDelay(hideDelay);
  }

  private final class TreePanelToolTip extends ToolTip
  {
    private ModelData lastOver;

    private TreePanelToolTip(Component target, ToolTipConfig config)
    {
      super(target, config);
    }

    @Override
    protected void onMouseMove(ComponentEvent ce)
    {
      ModelData over = toolTipTreePanelView.getOver();
      if (over != null)
      {
        if (over == lastOver)
        {
          // Just move tip
          super.onMouseMove(ce);
        }
        else
        {
          // Change tip
          Map<String, Object> properties = over.getProperties();
          if (properties.get(toolTipPropertyName) == null)
          {
            hide();
          }
          else
          {
            toolTipConfig.setParams(new Params(properties));
            clearTimer("hide");
            targetXY = ce.getXY();
            delayShow();
          }
        }
      }
      else
      {
        if (toolTipConfig.getParams() != null)
        {
          toolTipConfig.setParams(null);
          hide();
        }
      }
      lastOver = over;
    }

    @Override
    protected void updateContent()
    {
      String title = this.title;
      getHeader().setText(title == null ? "" : title);
      Template template;
      Params params;
      if ((template = toolTipConfig.getTemplate()) != null && (params = toolTipConfig.getParams()) != null)
      {
        template.overwrite(getBody().dom, params);
      }
      else
      {
        String text = this.text;
        if (text != null)
        {
          getBody().update(text);
        }
      }
    }

    @Override
    public void hide()
    {
      super.hide();
      lastOver = null;
    }
  }

  private final class ToolTipTreePanelView<N extends ModelData> extends TreePanelView<N>
  {
    public ModelData getOver()
    {
      return over == null ? null : over.getModel();
    }
  }

}
