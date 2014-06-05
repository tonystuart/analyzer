// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import com.example.analyzer.client.MergeTreeStore;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.shared.BaseTreeContent;
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.TreePanelEvent;
import com.extjs.gxt.ui.client.widget.treepanel.TreePanel.TreeNode;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;

public class MergeTreePanel<T extends BaseTreeContent> extends ViewPanel
{
  protected MergeTreeStore mergeTreeStore;
  protected DoubleClickToolTipTreePanel<ModelData> treePanel;

  public MergeTreePanel(String heading, MergeTreeStore mergeTreeStore)
  {
    super(heading);
    this.mergeTreeStore = mergeTreeStore;
  }

  @SuppressWarnings("rawtypes")
  private void onDoubleClick(TreeNode treeNode)
  {
    if (treeNode.isLeaf())
    {
      onLeafDoubleClick();
    }
    else
    {
      onParentDoubleClick();
    }
  }

  protected void onLeafDoubleClick()
  {
  }

  protected void onParentDoubleClick()
  {
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);
    treePanel = new DoubleClickToolTipTreePanel<ModelData>(mergeTreeStore, Keys.TOOL_TIP);
    treePanel.setDisplayProperty(Keys.NAME);
    treePanel.setStyleAttribute("background-color", "white");
    treePanel.getSelectionModel().addSelectionChangedListener(new MergeTreePanelSelectionChangedListener());
    treePanel.addListener(Events.OnDoubleClick, new MergeTreePanelDoubleClickListener());
    treePanel.setAutoExpand(true);
    treePanel.setBorders(true);
    add(treePanel, new Constraint("w=1,h=-1,t=5,l=5,r=5"));
  }

  protected void onSelectionChanged(ModelData selectedItem)
  {
  }

  public void selectItem(String property, Object value)
  {
    final ModelData selectedNode = mergeTreeStore.find(property, value);
    if (selectedNode != null)
    {
      treePanel.getSelectionModel().select(selectedNode, false);
      // Defer scroll in case containing panel just opened... it messes up and scrolls content off the window
      DeferredCommand.addCommand(new Command()
      {
        public void execute()
        {
          treePanel.scrollIntoView(selectedNode);
        }
      });
    }
  }

  private final class MergeTreePanelDoubleClickListener implements Listener<TreePanelEvent<ModelData>>
  {
    @Override
    public void handleEvent(TreePanelEvent<ModelData> e)
    {
      onDoubleClick(e.getNode());
    }

  }

  private final class MergeTreePanelSelectionChangedListener extends SelectionChangedListener<ModelData>
  {
    @Override
    public void selectionChanged(SelectionChangedEvent<ModelData> se)
    {
      ModelData selectedItem = se.getSelectedItem();
      onSelectionChanged(selectedItem);
    }
  }

}
