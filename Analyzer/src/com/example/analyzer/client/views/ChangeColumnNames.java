// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.views;

import java.util.LinkedList;
import java.util.List;

import com.example.analyzer.client.Analyzer;
import com.example.analyzer.client.Resources;
import com.example.analyzer.client.StoreAdapter;
import com.example.analyzer.client.Utilities;
import com.example.analyzer.client.widgets.AccordionViewPanel;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.EditorEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.CellEditor;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.EditorGrid;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DeferredCommand;
import com.google.gwt.user.client.Element;

public final class ChangeColumnNames extends AccordionViewPanel
{
  private static ChangeColumnNames instance;

  public static ChangeColumnNames getInstance()
  {
    return ChangeColumnNames.instance;
  }

  private Button changeButton;

  private Button clearButton;
  private ClearVisitor clearVisitor;
  private Label columnCountLabel;
  private ColumnModel columnModel;
  private Grid<ModelData> grid;
  private InitializationVisitor initializationVisitor;
  private Button toLowerButton;
  private ToLowerVisitor toLowerVisitor;
  private Button toMixedButton;
  private ToMixedVisitor toMixedVisitor;
  private Button toSpaceButton;
  private ToSpaceVisitor toSpaceVisitor;
  private Button toUnderscoreButton;
  private ToUnderscoreVisitor toUnderscoreVisitor;
  private Button toUpperButton;
  private ToUpperVisitor toUpperVisitor;

  public ChangeColumnNames(String heading)
  {
    super(heading);
    setInstance(this);
    setIcon(Resources.TEXT_REPLACE);
  }

  private void clear()
  {
    tableListStore.visit(getClearVisitor());
    updateFormState();
  }

  private void changeColumnNames()
  {
    ChangeVisitor changeVisitor = new ChangeVisitor();
    tableListStore.visit(changeVisitor);
    LinkedList<String> columnNames = changeVisitor.getNewColumnNames();
    Analyzer.getInstance().changeColumnNames(userTableId, columnNames);
  }

  private StoreAdapter<ModelData> getClearVisitor()
  {
    if (clearVisitor == null)
    {
      clearVisitor = new ClearVisitor();
    }
    return clearVisitor;
  }

  private StoreAdapter<ModelData> getInitializationVisitor()
  {
    if (initializationVisitor == null)
    {
      initializationVisitor = new InitializationVisitor();
    }
    return initializationVisitor;
  }

  private StoreAdapter<ModelData> getToLowerVisitor()
  {
    if (toLowerVisitor == null)
    {
      toLowerVisitor = new ToLowerVisitor();
    }
    return toLowerVisitor;
  }

  private StoreAdapter<ModelData> getToMixedVisitor()
  {
    if (toMixedVisitor == null)
    {
      toMixedVisitor = new ToMixedVisitor();
    }
    return toMixedVisitor;
  }

  private StoreAdapter<ModelData> getToSpaceVisitor()
  {
    if (toSpaceVisitor == null)
    {
      toSpaceVisitor = new ToSpaceVisitor();
    }
    return toSpaceVisitor;
  }

  private StoreAdapter<ModelData> getToUnderscoreVisitor()
  {
    if (toUnderscoreVisitor == null)
    {
      toUnderscoreVisitor = new ToUnderscoreVisitor();
    }
    return toUnderscoreVisitor;
  }

  private StoreAdapter<ModelData> getToUpperVisitor()
  {
    if (toUpperVisitor == null)
    {
      toUpperVisitor = new ToUpperVisitor();
    }
    return toUpperVisitor;
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    add(new Html("Click on item in <b>New Name</b> column to change it:"), new Constraint("w=1,t=5,l=5,r=5"));

    List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
    ColumnConfig oldNameColumnConfig = new ColumnConfig(Keys.NAME, "Old Name", 125);
    columnConfigs.add(oldNameColumnConfig);
    ColumnConfig newNameColumnConfig = new ColumnConfig(Keys.NEW_NAME, "New Name", 125);
    TextField<String> textField = new TextField<String>();
    CellEditor cellEditor = new CellEditor(textField);

    cellEditor.addListener(Events.Complete, new Listener<EditorEvent>()
    {
      @Override
      public void handleEvent(EditorEvent be)
      {
        DeferredCommand.addCommand(new Command()
        {
          @Override
          public void execute()
          {
            // NB: Must defer execution to give all event handlers a chance to handle this event... otherwise we may process the event before the store has added the record to the modified list.
            tableListStore.commitChanges();
            updateFormState();
          }
        });
      }
    });
    newNameColumnConfig.setEditor(cellEditor);
    columnConfigs.add(newNameColumnConfig);
    columnModel = new ColumnModel(columnConfigs);

    grid = new EditorGrid<ModelData>(tableListStore, columnModel);
    grid.getView().setAutoFill(true);
    grid.setBorders(true);
    add(grid, new Constraint("w=1,h=-1,t=0,l=5,r=5"));

    add(new Label(), new Constraint("w=-.5,t=1"));

    toUpperButton = new Button("a to A", new ToUpperButtonListener());
    toUpperButton.setToolTip("Convert names to upper case");
    add(toUpperButton, new Constraint("s,t=2,l=1,b=5"));

    toLowerButton = new Button("A to a", new ToLowerButtonListener());
    toLowerButton.setToolTip("Convert names to lower case");
    add(toLowerButton, new Constraint("s,t=2,l=1,b=5"));

    toMixedButton = new Button("AA to Aa", new ToMixedButtonListener());
    toMixedButton.setToolTip("Convert names to mixed case");
    add(toMixedButton, new Constraint("s,t=2,l=1,b=5"));

    toSpaceButton = new Button("_ to sp", new ToSpaceButtonListener());
    toSpaceButton.setToolTip("Convert underscores to spaces");
    add(toSpaceButton, new Constraint("s,t=2,l=1,b=5"));

    toUnderscoreButton = new Button("sp  to _", new ToUnderscoreButtonListener());
    toUnderscoreButton.setToolTip("Convert spaces to underscores");
    add(toUnderscoreButton, new Constraint("s,t=2,l=1,b=5"));

    add(new Label(), new Constraint("s,w=-.5,t=1"));

    add(columnCountLabel = new Label(), new Constraint("w=-1,t=5,l=5,V=m"));

    clearButton = new Button("Clear", new ClearButtonListener());
    clearButton.setToolTip("Clear modified columns and start over");
    add(clearButton, new Constraint("s,t=5,l=5,b=5"));

    changeButton = new Button("Change", new ChangeButtonListener());
    changeButton.setToolTip("Change column names and display results");
    add(changeButton, new Constraint("s,t=5,l=5,b=5,r=5,"));

    grid.getSelectionModel().addListener(Events.SelectionChange, new SelectionChangeListener());
    tableListStore.setMonitorChanges(true);

    updateFormState();
  }

  protected void reconfigure()
  {
    tableListStore.visit(getInitializationVisitor());
    grid.reconfigure(tableListStore, columnModel);
  }

  private void setInstance(ChangeColumnNames changeColumnNames)
  {
    ChangeColumnNames.instance = changeColumnNames;
  }

  public void toLower()
  {
    tableListStore.visit(getToLowerVisitor());
    updateFormState();
  }

  public void toMixed()
  {
    tableListStore.visit(getToMixedVisitor());
    updateFormState();
  }

  public String toMixed(String text)
  {
    boolean needUpper = true;
    int length = text.length();
    StringBuilder s = new StringBuilder(length);
    for (int offset = 0; offset < length; offset++)
    {
      char c = text.charAt(offset);
      if (needUpper && Utilities.isLowerCase(c))
      {
        c = Utilities.toUpperCase(c);
      }
      else if (!needUpper && Utilities.isUpperCase(c))
      {
        c = Utilities.toLowerCase(c);
      }
      needUpper = (c == ' ' || c == '_');
      s.append(c);
    }
    return s.toString();
  }

  public void toSpace()
  {
    tableListStore.visit(getToSpaceVisitor());
    updateFormState();
  }

  public void toUnderscore()
  {
    tableListStore.visit(getToUnderscoreVisitor());
    updateFormState();
  }

  public void toUpper()
  {
    tableListStore.visit(getToUpperVisitor());
    updateFormState();
  }

  protected void updateFormState()
  {
    UpdateFormStateVisitor updateFormStateVisitor = new UpdateFormStateVisitor();
    tableListStore.visit(updateFormStateVisitor);
    toUpperButton.setEnabled(updateFormStateVisitor.isEnableToUpper());
    toLowerButton.setEnabled(updateFormStateVisitor.isEnableToLower());
    toMixedButton.setEnabled(updateFormStateVisitor.isEnableToMixed());
    toSpaceButton.setEnabled(updateFormStateVisitor.isEnableToSpace());
    toUnderscoreButton.setEnabled(updateFormStateVisitor.isEnableToUnderscore());
    int modifiedCount = updateFormStateVisitor.getModifiedCount();
    boolean isModified = modifiedCount > 0;
    clearButton.setEnabled(isModified);
    changeButton.setEnabled(isModified);
    columnCountLabel.setText(modifiedCount + " of " + tableListStore.getCount() + " Columns");
  }

  private final class ChangeButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      changeColumnNames();
    }
  }

  private final class ClearButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      clear();
    }
  }

  private final class ClearVisitor extends StoreAdapter<ModelData>
  {
    @Override
    public void visit(ModelData modelData)
    {
      modelData.set(Keys.NEW_NAME, modelData.get(Keys.NAME));
    }
  }

  private final class ChangeVisitor extends StoreAdapter<ModelData>
  {
    private LinkedList<String> newColumnNames = new LinkedList<String>();

    @Override
    public void visit(ModelData modelData)
    {
      String newName = modelData.get(Keys.NEW_NAME);
      newColumnNames.add(newName);
    }

    public LinkedList<String> getNewColumnNames()
    {
      return newColumnNames;
    }

  }

  private final class InitializationVisitor extends StoreAdapter<ModelData>
  {
    @Override
    public void visit(ModelData modelData)
    {
      String newName = modelData.get(Keys.NEW_NAME);
      if (newName == null || newName.length() == 0)
      {
        newName = modelData.get(Keys.NAME);
        modelData.set(Keys.NEW_NAME, newName);
      }
    }
  }

  private class SelectionChangeListener implements Listener<BaseEvent>
  {
    @Override
    public void handleEvent(BaseEvent be)
    {
      updateFormState();
    }
  }

  private final class ToLowerButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      toLower();
    }
  }

  private final class ToLowerVisitor extends StoreAdapter<ModelData>
  {
    @Override
    public void visit(ModelData modelData)
    {
      modelData.set(Keys.NEW_NAME, ((String)modelData.get(Keys.NEW_NAME)).toLowerCase());
    }
  }

  private final class ToMixedButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      toMixed();
    }
  }

  private final class ToMixedVisitor extends StoreAdapter<ModelData>
  {
    @Override
    public void visit(ModelData modelData)
    {
      modelData.set(Keys.NEW_NAME, toMixed((String)modelData.get(Keys.NEW_NAME)));
    }
  }

  private final class ToSpaceButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      toSpace();
    }
  }

  private final class ToSpaceVisitor extends StoreAdapter<ModelData>
  {
    @Override
    public void visit(ModelData modelData)
    {
      modelData.set(Keys.NEW_NAME, ((String)modelData.get(Keys.NEW_NAME)).replace('_', ' '));
    }
  }

  private final class ToUnderscoreButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      toUnderscore();
    }
  }

  private final class ToUnderscoreVisitor extends StoreAdapter<ModelData>
  {
    @Override
    public void visit(ModelData modelData)
    {
      modelData.set(Keys.NEW_NAME, ((String)modelData.get(Keys.NEW_NAME)).replace(' ', '_'));
    }
  }

  private final class ToUpperButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      toUpper();
    }
  }

  private final class ToUpperVisitor extends StoreAdapter<ModelData>
  {
    @Override
    public void visit(ModelData modelData)
    {
      modelData.set(Keys.NEW_NAME, ((String)modelData.get(Keys.NEW_NAME)).toUpperCase());
    }
  }

  public class UpdateFormStateVisitor extends StoreAdapter<ModelData>
  {
    private boolean enableToLower;
    private boolean enableToMixed;
    private boolean enableToSpace;
    private boolean enableToUnderscore;
    private boolean enableToUpper;
    private int modifiedCount;

    public int getModifiedCount()
    {
      return modifiedCount;
    }

    public boolean isEnableToLower()
    {
      return enableToLower;
    }

    public boolean isEnableToMixed()
    {
      return enableToMixed;
    }

    public boolean isEnableToSpace()
    {
      return enableToSpace;
    }

    public boolean isEnableToUnderscore()
    {
      return enableToUnderscore;
    }

    public boolean isEnableToUpper()
    {
      return enableToUpper;
    }

    @Override
    public void visit(ModelData modelData)
    {
      boolean needUpper = true;
      String oldName = modelData.get(Keys.NAME);
      String newName = modelData.get(Keys.NEW_NAME);
      if (!oldName.equals(newName))
      {
        modifiedCount++;
      }
      int length = newName.length();
      for (int offset = 0; offset < length; offset++)
      {
        char c = newName.charAt(offset);
        boolean isLower = Utilities.isLowerCase(c);
        boolean isUpper = Utilities.isUpperCase(c);

        enableToUpper = enableToUpper || isLower;
        enableToLower = enableToLower || isUpper;
        enableToSpace = enableToSpace || c == '_';
        enableToUnderscore = enableToUnderscore || c == ' ';
        enableToMixed = enableToMixed || ((isLower && needUpper) || (isUpper && !needUpper));
        needUpper = ((c == ' ') || (c == '_'));
      }
    }

  }

}
