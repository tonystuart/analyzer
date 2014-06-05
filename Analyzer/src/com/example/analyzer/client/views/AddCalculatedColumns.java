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
import com.example.analyzer.client.Utilities;
import com.example.analyzer.client.widgets.AccordionViewPanel;
import com.example.analyzer.client.widgets.EnumListView;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.shared.DbFunction;
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.core.El;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.util.Scroll;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.ListView;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextArea;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;

public class AddCalculatedColumns extends AccordionViewPanel
{
  private static final String FORMAT_FUNCTION_ARGUMENT_LIST = " (  )";
  private static final String FORMAT_READABILITY_SPACING = " ";
  private static final String FORMAT_SQL92_IDENTIFIER_QUOTE = "\""; // see http://db.apache.org/derby/docs/10.6/ref/crefsqlj34834.html#crefsqlj34834
  
  private static AddCalculatedColumns instance;

  public static AddCalculatedColumns getInstance()
  {
    return AddCalculatedColumns.instance;
  }

  private Button calculateButton;
  private Button clearButton;
  private ListView<ModelData> columnsListView;
  private PositionRetainingTextArea formulaTextArea;
  private EnumListView<DbFunction> functionsListView;
  private Button insertColumnButton;
  private Button insertFunctionButton;
  private TextField<String> newColumnNameTextField;

  public AddCalculatedColumns(String heading)
  {
    super(heading);
    setInstance(this);
    setIcon(Resources.CALCULATOR_ADD);
  }

  private void addCalculatedColumns()
  {
    String formula = formulaTextArea.getRawValue();
    String newColumnName = newColumnNameTextField.getRawValue();
    Analyzer.getInstance().addCalculatedColumn(userTableId, newColumnName, formula);
  }

  private void clear()
  {
    formulaTextArea.clear();
    newColumnNameTextField.clear();
    updateFormState();
  }

  protected void insertColumn()
  {
    ModelData selectedItem = columnsListView.getSelectionModel().getSelectedItem();
    if (selectedItem != null)
    {
      String name = selectedItem.get(Keys.NAME);
      if (requiresQuotes(name))
      {
        name = FORMAT_SQL92_IDENTIFIER_QUOTE + name + FORMAT_SQL92_IDENTIFIER_QUOTE;
      }
      insertText(name);
    }
  }

  protected void insertFunction()
  {
    ModelData selectedItem = functionsListView.getSelectionModel().getSelectedItem();
    if (selectedItem != null)
    {
      String name = selectedItem.get(Keys.NAME) + FORMAT_FUNCTION_ARGUMENT_LIST;
      insertText(name, 3);
    }
  }

  public void insertText(String text)
  {
    insertText(text, 0);
  }
  
  public void insertText(String text, int rightCursorOffset)
  {
    String formattedText = FORMAT_READABILITY_SPACING + text + FORMAT_READABILITY_SPACING;
    formulaTextArea.insertAtCursor(formattedText, true, rightCursorOffset);
    updateFormState();
  }

  private boolean isSet(String rawValue)
  {
    return rawValue != null && rawValue.trim().length() > 0;
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    add(new Html("Enter Formula for <b>Calculated Column</b>:"), new Constraint("w=1,t=5,l=5,r=5"));

    add(formulaTextArea = new PositionRetainingTextArea(), new Constraint("w=1,h=-0.40,t=1,l=5,r=5"));
    UpdateFormStateKeyListener updateFormStateKeyListener = new UpdateFormStateKeyListener();
    formulaTextArea.addKeyListener(updateFormStateKeyListener);

    add(new Label(), new Constraint("w=-0.5"));
    add(new Button("+", new InsertTextButtonListener("+")), new Constraint("s,t=2"));
    add(new Button("-", new InsertTextButtonListener("-")), new Constraint("s,t=2,l=1"));
    add(new Button("*", new InsertTextButtonListener("*")), new Constraint("s,t=2,l=1"));
    add(new Button("/", new InsertTextButtonListener("/")), new Constraint("s,t=2,l=1"));
    add(new Button("(", new InsertTextButtonListener("(")), new Constraint("s,t=2,l=1"));
    add(new Button(")", new InsertTextButtonListener(")")), new Constraint("s,t=2,l=1"));
    add(new Button("<", new InsertTextButtonListener("<")), new Constraint("s,t=2,l=1"));
    add(new Button("<=", new InsertTextButtonListener("<=")), new Constraint("s,t=2,l=1"));
    add(new Button("=", new InsertTextButtonListener("=")), new Constraint("s,t=2,l=1"));
    add(new Button(">=", new InsertTextButtonListener(">=")), new Constraint("s,t=2,l=1"));
    add(new Button(">", new InsertTextButtonListener(">")), new Constraint("s,t=2,l=1"));
    add(new Button("!=", new InsertTextButtonListener("!=")), new Constraint("s,t=2,l=1"));
    add(new Button("and", new InsertTextButtonListener("and")), new Constraint("s,t=2,l=1"));
    add(new Button("or", new InsertTextButtonListener("or")), new Constraint("s,t=2,l=1"));
    add(new Label(), new Constraint("s,w=-0.5"));

    add(new Html("Available Columns:"), new Constraint("w=.5,t=5,H=c"));
    add(new Html("Available Functions:"), new Constraint("s,w=.5,t=5,H=c"));

    columnsListView = new ListView<ModelData>(tableListStore);
    columnsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    columnsListView.setDisplayProperty(Keys.NAME);
    columnsListView.addListener(Events.DoubleClick, new ColumnDoubleClickListener());

    functionsListView = new EnumListView<DbFunction>(DbFunction.values());
    functionsListView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    functionsListView.getSelectionModel().select(index, false);
    functionsListView.setDisplayProperty(Keys.NAME);
    functionsListView.addListener(Events.DoubleClick, new FunctionDoubleClickListener());

    add(columnsListView, new Constraint("w=.5,h=-0.60,t=1,l=5"));
    add(functionsListView, new Constraint("s,w=.5,h=-0.60,t=1,l=5,r=5"));

    add(insertColumnButton = new Button("Insert in Formula", new InsertColumnButtonListener()), new Constraint("w=-0.5,t=2,H=c"));
    add(insertFunctionButton = new Button("Insert in Formula", new InsertFunctionButtonListener()), new Constraint("s,w=-0.5,t=2,H=c"));

    add(new Html("Enter New Name for <b>Calculated Column</b>:"), new Constraint("w=1,t=10,l=5,r=5"));
    add(newColumnNameTextField = new TextField<String>(), new Constraint("w=1,t=1,l=5,r=5"));
    newColumnNameTextField.addKeyListener(updateFormStateKeyListener);

    add(clearButton = new Button("Clear", new ClearButtonListener()), new Constraint("w=-1,t=5,l=5,b=5,H=r"));
    clearButton.setToolTip("Clear form and start over");

    add(calculateButton = new Button("Add", new CalculateButtonListener()), new Constraint("s,t=5,l=5,b=5"));
    calculateButton.setToolTip("Add calculated columns and display results");

    updateFormState();
  }

  @Override
  protected void reconfigure()
  {
    ModelData configuredProperties = configuredTableListStore.getProperties();
    configuredProperties.set(Keys.CALCULATED_COLUMN_FORMULA, formulaTextArea.getRawValue());
    configuredProperties.set(Keys.CALCULATED_COLUMN_NAME, newColumnNameTextField.getRawValue());
    columnsListView.setStore(tableListStore);
    ModelData properties = tableListStore.getProperties();
    formulaTextArea.setRawValue((String)properties.get(Keys.CALCULATED_COLUMN_FORMULA));
    newColumnNameTextField.setRawValue((String)properties.get(Keys.CALCULATED_COLUMN_NAME));
  }

  private boolean requiresQuotes(String name)
  {
    int length = name.length();
    for (int offset = 0; offset < length; offset++)
    {
      char c = name.charAt(offset);
      if (Utilities.isLowerCase(c) || c == ' ')
      {
        return true;
      }
    }
    return false;
  }

  private void setInstance(AddCalculatedColumns addCalculatedColumns)
  {
    AddCalculatedColumns.instance = addCalculatedColumns;
  }

  public void updateFormState()
  {
    boolean isFormulaSet = isSet(formulaTextArea.getRawValue());
    boolean isNewColumnNameSet = isSet(newColumnNameTextField.getRawValue());
    boolean isValidTable = tableListStore.getCount() > 0;
    clearButton.setEnabled(isFormulaSet || isNewColumnNameSet);
    calculateButton.setEnabled(isFormulaSet && isNewColumnNameSet && isValidTable);
    insertColumnButton.setEnabled(isValidTable);
    insertFunctionButton.setEnabled(isValidTable);
    if (isValidTable)
    {
      columnsListView.getSelectionModel().select(0, false);
    }
  }

  private final class CalculateButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      addCalculatedColumns();
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

  private final class ColumnDoubleClickListener implements Listener<BaseEvent>
  {
    @Override
    public void handleEvent(BaseEvent be)
    {
      insertColumn();
    }
  }

  private final class FunctionDoubleClickListener implements Listener<BaseEvent>
  {
    @Override
    public void handleEvent(BaseEvent be)
    {
      insertFunction();
    }
  }

  private final class InsertColumnButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      insertColumn();
    }
  }

  private final class InsertFunctionButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      insertFunction();
    }
  }

  private final class InsertTextButtonListener extends SelectionListener<ButtonEvent>
  {
    private String text;

    public InsertTextButtonListener(String text)
    {
      this.text = text;
    }

    @Override
    public void componentSelected(ButtonEvent ce)
    {
      insertText(text);
    }
  }

  public class PositionRetainingTextArea extends TextArea
  {
    public void insertAtCursor(String text, boolean isAdvanceCursor, int rightCursorOffset)
    {
      int cursorPos = getCursorPos();
      String rawValue = getRawValue();
      String newValue = rawValue.substring(0, cursorPos) + text + rawValue.substring(cursorPos);
      El inputEl = getInputEl();
      Scroll scroll = inputEl.getScroll();
      int scrollTop = scroll.getScrollTop();
      inputEl.setValue(newValue);
      inputEl.setScrollTop(scrollTop);
      if (isAdvanceCursor)
      {
        cursorPos += text.length() - rightCursorOffset;
      }
      setCursorPos(cursorPos);
      inputEl.setFocus(true);
    }
  }

  private final class UpdateFormStateKeyListener extends KeyListener
  {
    @Override
    public void componentKeyUp(ComponentEvent event)
    {
      super.componentKeyUp(event);
      updateFormState();
      int keyCode = event.getKeyCode();
      if (keyCode == KeyCodes.KEY_ENTER && event.getComponent() == newColumnNameTextField && calculateButton.isEnabled())
      {
        addCalculatedColumns();
      }
    }
  }

}
