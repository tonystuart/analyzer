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
import com.example.analyzer.client.widgets.AccordionViewPanel;
import com.example.analyzer.client.widgets.EnumCellEditorComboBox;
import com.example.analyzer.client.widgets.EnumItem;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.shared.DbCompareOperation;
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.Style.SelectionMode;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;

public class SelectRows extends AccordionViewPanel
{
  private static final DbCompareOperation DEFAULT_COMPARE_OPERATION = DbCompareOperation.EqualTo;

  private static SelectRows instance;

  public static SelectRows getInstance()
  {
    return SelectRows.instance;
  }

  private ColumnModel columnModel;

  private EnumCellEditorComboBox<DbCompareOperation> compareOperationComboBox;
  private Grid<ModelData> grid;
  private Button selectButton;
  private TextField<String> valueTextField;
  public SelectRows(String heading)
  {
    super(heading);
    setInstance(this);
    setIcon(Resources.APPLICATION_GO);
  }

  public void configureCompareOperation(int userTableId, int columnOffset, String compareOperation, String value)
  {
    grid.getSelectionModel().select(columnOffset, false);
    grid.getView().ensureVisible(columnOffset, 0, false);
    // TODO: Reconcile use of enum name() in menu and nls string in combobox
    compareOperationComboBox.select(DbCompareOperation.valueOf(compareOperation));
    valueTextField.setValue(value);
    updateFormState();
  }

  private boolean isEmpty(TextField<String> textField)
  {
    return valueTextField.getValue() == null || valueTextField.getValue().length() == 0;
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    List<ColumnConfig> columnConfigs = new LinkedList<ColumnConfig>();
    ColumnConfig nameColumnConfig = new ColumnConfig(Keys.NAME, "Column Name", 125);
    columnConfigs.add(nameColumnConfig);
    ColumnConfig typeColumnConfig = new ColumnConfig(Keys.TYPE, "Column Type", 125);
    columnConfigs.add(typeColumnConfig);
    columnModel = new ColumnModel(columnConfigs);
    grid = new Grid<ModelData>(tableListStore, columnModel);
    grid.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    grid.getSelectionModel().addSelectionChangedListener(new SelectionChangedListener<ModelData>()
    {
      @Override
      public void selectionChanged(SelectionChangedEvent<ModelData> se)
      {
        updateFormState();
      }
    });
    //grid.setHideHeaders(true);
    grid.setAutoExpandColumn(Keys.NAME);
    grid.setBorders(true);

    add(new Label("Where:"), new Constraint("w=1,t=5,l=5,r=5"));
    add(grid, new Constraint("w=1,h=-1,l=5,r=5"));

    add(new Label("Operation:"), new Constraint("w=1,t=5,l=5,r=5"));
    add(compareOperationComboBox = new EnumCellEditorComboBox<DbCompareOperation>(DbCompareOperation.values()), new Constraint("w=1,l=5,r=5"));
    compareOperationComboBox.select(DEFAULT_COMPARE_OPERATION);

    add(new Label("Value:"), new Constraint("w=1,t=5,l=5,r=5"));
    add(valueTextField = new TextField<String>(), new Constraint("w=1,l=5,r=5"));
    valueTextField.addKeyListener(new EnterKeyListener());

    add(new Label(), new Constraint("w=-1"));
    add(selectButton = new Button("Select", new SelectButtonListener()), new Constraint("s,t=5,l=5,b=5,r=5"));
    selectButton.setToolTip("Select rows and display results");

    updateFormState();
  }

  protected void reconfigure()
  {
    List<ModelData> selectedItems = grid.getSelectionModel().getSelectedItems();
    SelectRowProperties selectRowProperties = new SelectRowProperties(selectedItems, compareOperationComboBox.getValue(), valueTextField.getValue());
    configuredTableListStore.getProperties().set(Keys.SELECT_ROW_PROPERTIES, selectRowProperties);

    grid.reconfigure(tableListStore, columnModel);

    selectRowProperties = tableListStore.getProperties().get(Keys.SELECT_ROW_PROPERTIES);
    if (selectRowProperties == null)
    {
      // Initialize to defaults on new table
      grid.getSelectionModel().deselectAll();
      compareOperationComboBox.select(DEFAULT_COMPARE_OPERATION);
      valueTextField.clear();
    }
    else
    {
      grid.getSelectionModel().setSelection(selectRowProperties.getSelectedItems());
      compareOperationComboBox.setValue(selectRowProperties.getCompareOperation());
      valueTextField.setValue(selectRowProperties.getValue());
    }
  }

  private void selectRows()
  {
    String columnName = grid.getSelectionModel().getSelectedItem().get(Keys.NAME);
    DbCompareOperation compareOperation = compareOperationComboBox.getValue().getValue();
    String value = valueTextField.getValue();
    Analyzer.getInstance().selectRows(userTableId, columnName, compareOperation, value);
  }

  private void setInstance(SelectRows selectRows)
  {
    SelectRows.instance = selectRows;
  }

  protected void updateFormState()
  {
    selectButton.setEnabled(grid.getSelectionModel().getSelectedItem() != null && !isEmpty(valueTextField));
  }

  private final class EnterKeyListener extends KeyListener
  {
    @Override
    public void componentKeyUp(ComponentEvent event)
    {
      super.componentKeyUp(event);
      updateFormState();
      int keyCode = event.getKeyCode();
      if (keyCode == KeyCodes.KEY_ENTER && selectButton.isEnabled())
      {
        selectRows();
      }
    }
  }

  private final class SelectButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      selectRows();
    }
  }

  public class SelectRowProperties
  {
    private EnumItem<DbCompareOperation> compareOperation;
    private List<ModelData> selectedItems;
    private String value;

    public SelectRowProperties(List<ModelData> selectedItems, EnumItem<DbCompareOperation> compareOperation, String value)
    {
      this.selectedItems = selectedItems;
      this.compareOperation = compareOperation;
      this.value = value;
    }

    public EnumItem<DbCompareOperation> getCompareOperation()
    {
      return compareOperation;
    }

    public List<ModelData> getSelectedItems()
    {
      return selectedItems;
    }

    public String getValue()
    {
      return value;
    }

  }

}
