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
import com.example.analyzer.client.TableTypeIconProvider;
import com.example.analyzer.client.widgets.TableTreePanel;
import com.example.analyzer.client.widgets.ViewPanel;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.shared.DbVerticalJoinType;
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.Radio;
import com.google.gwt.user.client.Element;

public class JoinTablesVertically extends ViewPanel
{
  private static JoinTablesVertically instance;

  public static JoinTablesVertically getInstance()
  {
    return JoinTablesVertically.instance;
  }

  private Button clearButton;
  private Button joinButton;
  private TableTreePanel topTreePanel;
  private TableTreePanel bottomTreePanel;
  private Radio unionRadio;
  private Radio unionAllRadio;
  private Radio intersectionRadio;
  private Radio complementRadio;

  public JoinTablesVertically(String heading)
  {
    super(heading);
    setInstance(this);
    setIcon(Resources.APPLICATION_TILE_VERTICAL);
  }

  private void clear()
  {
  }

  private boolean isSet(ModelData modelData)
  {
    return modelData != null && modelData.get(Keys.NAME) != null;
  }

  public void joinTablesVertically()
  {
    ModelData topTable = topTreePanel.getValue();
    ModelData bottomTable = bottomTreePanel.getValue();

    int topUserTableId = topTable.get(Keys.USER_TABLE_ID);
    int bottomUserTableId = bottomTable.get(Keys.USER_TABLE_ID);

    DbVerticalJoinType verticalJoinType = DbVerticalJoinType.UNION_ALL;

    if (unionRadio.getValue())
    {
      verticalJoinType = DbVerticalJoinType.UNION;
    }
    else if (intersectionRadio.getValue())
    {
      verticalJoinType = DbVerticalJoinType.INTERSECTION;
    }
    else if (complementRadio.getValue())
    {
      verticalJoinType = DbVerticalJoinType.COMPLEMENT;
    }

    Analyzer.getInstance().joinTablesVertically(topUserTableId, bottomUserTableId, verticalJoinType);
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    add(new Html("Select <b>Top</b> Table:"), new Constraint("w=1,t=5,l=5"));
    
    topTreePanel = new TableTreePanel();
    topTreePanel.setIconProvider(TableTypeIconProvider.getInstance());
    add(topTreePanel, new Constraint("w=1,h=-.5,t=1,l=5,r=5"));

    add(new Html("Select <b>Type</b> of Vertical Join:"), new Constraint("w=1,t=5,l=5"));

    add(new Label(), new Constraint("w=-.20,t=1,l=5,V=m"));
    unionRadio = new Radio();
    unionRadio.setEnabled(false);
    unionRadio.setName("joinTablesVerticallySelector");
    add(unionRadio, new Constraint("s,t=1,l=10"));
    add(new Label("Union"), new Constraint("s,t=1,l=1,V=m"));

    add(new Label(), new Constraint("s,w=-.20,t=1,l=5,V=m"));
    unionAllRadio = new Radio();
    unionAllRadio.setValue(true);
    unionAllRadio.setName("joinTablesVerticallySelector");
    add(unionAllRadio, new Constraint("s,t=1,l=10"));
    add(new Label("Union All"), new Constraint("s,t=1,l=1,V=m"));

    add(new Label(), new Constraint("s,w=-.20,t=1,l=5,V=m"));
    intersectionRadio = new Radio();
    intersectionRadio.setEnabled(false);
    intersectionRadio.setName("joinTablesVerticallySelector");
    add(intersectionRadio, new Constraint("s,t=1,l=10"));
    add(new Label("Intersection"), new Constraint("s,t=1,l=1,V=m"));

    add(new Label(), new Constraint("s,w=-.20,t=1,l=5,V=m"));
    complementRadio = new Radio();
    complementRadio.setEnabled(false);
    complementRadio.setName("joinTablesVerticallySelector");
    add(complementRadio, new Constraint("s,t=1,l=10"));
    add(new Label("Complement"), new Constraint("s,t=1,l=1,V=m"));

    add(new Html("Select <b>Bottom</b> Table:"), new Constraint("w=1,t=10,l=5"));

    bottomTreePanel = new TableTreePanel();
    bottomTreePanel.setIconProvider(TableTypeIconProvider.getInstance());
    add(bottomTreePanel, new Constraint("w=1,h=-.5,t=1,l=5,r=5"));

    add(new Label(), new Constraint("w=-1,t=5,l=2,H=l,V=m"));

    clearButton = new Button("Clear", new ClearButtonListener());
    clearButton.setToolTip("Clear form and start over");
    // add(clearButton, new Constraint("s,t=5,b=5,l=5"));

    joinButton = new Button("Join", new JoinButtonListener());
    joinButton.setToolTip("Join tables and display results");
    add(joinButton, new Constraint("s,t=5,b=5,l=5,r=5"));

    updateFormState();

    TableSelectionChangedListener tableSelectionChangedListener = new TableSelectionChangedListener();
    topTreePanel.getSelectionModel().addSelectionChangedListener(tableSelectionChangedListener);
    bottomTreePanel.getSelectionModel().addSelectionChangedListener(tableSelectionChangedListener);
  }

  private void setInstance(JoinTablesVertically joinTablesVertically)
  {
    JoinTablesVertically.instance = joinTablesVertically;
  }

  public void updateFormState()
  {
    boolean isReady = isSet(topTreePanel.getSelectionModel().getSelectedItem()) && isSet(bottomTreePanel.getSelectionModel().getSelectedItem());

    joinButton.setEnabled(isReady);
    clearButton.setEnabled(isReady);
  }

  public void updateHistory()
  {
    topTreePanel.expandAll();
    bottomTreePanel.expandAll();
    updateFormState();
  }

  private final class ClearButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      clear();
    }
  }

  private final class JoinButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      joinTablesVertically();
    }
  }

  private final class TableSelectionChangedListener extends SelectionChangedListener<ModelData>
  {
    @Override
    public void selectionChanged(SelectionChangedEvent<ModelData> se)
    {
      updateFormState();
    }
  }

}
