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
import java.util.Map.Entry;

import com.example.analyzer.client.Analyzer;
import com.example.analyzer.client.Resources;
import com.example.analyzer.client.widgets.AccordionViewPanel;
import com.example.analyzer.client.widgets.ComboBoxCellEditor;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.example.analyzer.jdbc.shared.ColumnTypes;
import com.example.analyzer.shared.DbColumnExpanderProperties;
import com.example.analyzer.shared.Keys;
import com.extjs.gxt.ui.client.Style.SortDir;
import com.extjs.gxt.ui.client.data.BaseModelData;
import com.extjs.gxt.ui.client.data.ModelData;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionChangedEvent;
import com.extjs.gxt.ui.client.event.SelectionChangedListener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.store.ListStore;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.grid.CheckBoxSelectionModel;
import com.extjs.gxt.ui.client.widget.grid.ColumnConfig;
import com.extjs.gxt.ui.client.widget.grid.ColumnModel;
import com.extjs.gxt.ui.client.widget.grid.Grid;
import com.google.gwt.user.client.Element;

public class AddExpandedColumns extends AccordionViewPanel
{
  private static AddExpandedColumns instance;

  public static AddExpandedColumns getInstance()
  {
    return AddExpandedColumns.instance;
  }

  private CheckBoxSelectionModel<ModelData> checkBoxSelectionModel;
  private Button clearButton;
  private ComboBoxCellEditor<ModelData> columnComboBox;
  private Button expandButton;
  private Iterable<DbColumnExpanderProperties> expandedColumnDescriptors;
  private ComboBoxCellEditor<ModelData> expanderComboBox;
  private ListStore<ModelData> expanderListStore = new ListStore<ModelData>();
  private ColumnModel fieldColumnModel;
  private Grid<ModelData> fieldGrid;
  private CheckBox restrictToMatchingTypeCheckBox;
  private boolean suppressLocalUpdate;

  public AddExpandedColumns(String heading)
  {
    super(heading);
    setInstance(this);
    setIcon(Resources.TABLE_ADD);
  }

  private void addExpandedColumns()
  {
    ModelData expanderModelData = expanderComboBox.getValue();
    String expanderName = expanderModelData.get(Keys.NAME);

    LinkedList<String> fieldNames = new LinkedList<String>();
    List<ModelData> selectedItems = checkBoxSelectionModel.getSelectedItems();
    for (ModelData selectedItem : selectedItems)
    {
      String name = selectedItem.get(Keys.NAME);
      fieldNames.add(name);
    }

    ModelData columnModelData = columnComboBox.getValue();
    String column = columnModelData.get(Keys.NAME);

    Analyzer.getInstance().addExpandedColumns(configuredUserTableId, column, expanderName, fieldNames);
  }

  private void clear()
  {
    fieldGrid.getSelectionModel().deselectAll();
    columnComboBox.setValue(null);
    updateFormState();
  }

  public void configure(Iterable<DbColumnExpanderProperties> expandedColumnDescriptors)
  {
    if (isVisibleWithGxtWorkaround())
    {
      initializeExpanderOperationListStore(expandedColumnDescriptors);
    }
    else
    {
      // Cache column descriptors for first time we're visible to prevent grid display glitches
      this.expandedColumnDescriptors = expandedColumnDescriptors;
    }
  }

  private ListStore<ModelData> getMatchingTypeStore()
  {
    ModelData expander = expanderComboBox.getValue();
    String requiredType = expander.get(Keys.TYPE);
    ListStore<ModelData> restrictedListStore = new ListStore<ModelData>();
    int count = tableListStore.getCount();
    for (int offset = 0; offset < count; offset++)
    {
      ModelData modelData = tableListStore.getAt(offset);
      if (modelData.get(Keys.TYPE).equals(requiredType))
      {
        restrictedListStore.add(modelData);
      }
    }
    return restrictedListStore;
  }

  private void initializeExpanderOperationListStore(Iterable<DbColumnExpanderProperties> expandedColumnDescriptors)
  {
    for (DbColumnExpanderProperties expandedColumnDescriptor : expandedColumnDescriptors)
    {
      BaseModelData expander = new BaseModelData();
      String expanderName = expandedColumnDescriptor.getExpanderName();
      int columnType = expandedColumnDescriptor.getColumnType();
      String columnTypeName = ColumnTypes.getTypeName(columnType);
      ListStore<ModelData> propertiesListStore = new ListStore<ModelData>();
      for (Entry<String, String> property : expandedColumnDescriptor.getProperties().entrySet())
      {
        BaseModelData propertyModelData = new BaseModelData();
        String name = property.getKey();
        String description = property.getValue();
        propertyModelData.set(Keys.NAME, name);
        propertyModelData.set(Keys.TOOL_TIP, description);
        propertiesListStore.add(propertyModelData);
      }
      propertiesListStore.sort(Keys.NAME, SortDir.ASC);
      expander.set(Keys.NAME, expanderName);
      expander.set(Keys.PROPERTIES, propertiesListStore);
      expander.set(Keys.TYPE, columnTypeName);
      expanderListStore.add(expander);
    }
    expanderComboBox.setValue(expanderListStore.getAt(0));
  }

  @Override
  public void onExpand()
  {
    if (expandedColumnDescriptors != null)
    {
      initializeExpanderOperationListStore(expandedColumnDescriptors);
      expandedColumnDescriptors = null;
    }
    super.onExpand();
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);

    add(new Html("Select an <b>Expander Operation</b>:"), new Constraint("w=1,l=5,t=5,r=5"));

    expanderComboBox = new ComboBoxCellEditor<ModelData>();
    expanderComboBox.setStore(expanderListStore);
    expanderComboBox.addSelectionChangedListener(new ExpanderOperationSelectionChangedListener());
    add(expanderComboBox, new Constraint("w=1,t=1,l=5,r=5"));

    add(new Html("Select one or more <b>Columns to Add</b>:"), new Constraint("w=1,l=5,t=5,r=5"));

    List<ColumnConfig> columnsToGenerateColumnConfigs = new LinkedList<ColumnConfig>();

    checkBoxSelectionModel = new CheckBoxSelectionModel<ModelData>();
    checkBoxSelectionModel.addSelectionChangedListener(new ColumnsToGenerateSelectionChangedListener());
    columnsToGenerateColumnConfigs.add(checkBoxSelectionModel.getColumn());

    ColumnConfig nameColumnConfig = new ColumnConfig(Keys.NAME, "Name", 100);
    columnsToGenerateColumnConfigs.add(nameColumnConfig);

    ColumnConfig descriptionColumnConfig = new ColumnConfig(Keys.TOOL_TIP, "Description", 125);
    columnsToGenerateColumnConfigs.add(descriptionColumnConfig);

    fieldColumnModel = new ColumnModel(columnsToGenerateColumnConfigs);

    fieldGrid = new Grid<ModelData>(new ListStore<ModelData>(), fieldColumnModel);
    fieldGrid.setAutoExpandColumn(Keys.NAME); // TODO: hmmm, doesn't seem to work? Is it a problem with reconfigure? No, SelectColumns is the same and it works.
    fieldGrid.setBorders(true);
    fieldGrid.setSelectionModel(checkBoxSelectionModel);
    fieldGrid.addPlugin(checkBoxSelectionModel);
    add(fieldGrid, new Constraint("w=1,h=-1,t=1,l=5,r=5"));

    add(new Html("Select the <b>Column to Expand</b>:"), new Constraint("w=1,l=5,t=5,r=5"));

    columnComboBox = new ComboBoxCellEditor<ModelData>();
    columnComboBox.setStore(tableListStore);
    columnComboBox.addSelectionChangedListener(new ColumnSelectionChangedListener());
    add(columnComboBox, new Constraint("w=1,t=1,l=5,r=5"));

    restrictToMatchingTypeCheckBox = new CheckBox();
    restrictToMatchingTypeCheckBox.setValue(true);
    restrictToMatchingTypeCheckBox.addListener(Events.Change, new RestrictToMatchingTypeListener());
    add(restrictToMatchingTypeCheckBox, new Constraint("t=5,l=5,b=5"));
    add(new Html("Restrict to Matching Type"), new Constraint("s,w=-1,t=5,l=2,b=5,H=l,V=m"));

    add(clearButton = new Button("Clear", new ClearButtonListener()), new Constraint("s,t=5,l=5,b=5"));
    clearButton.setToolTip("Clear form and start over");

    add(expandButton = new Button("Add", new ExpandButtonListener()), new Constraint("s,t=5,l=5,b=5,r=5"));
    expandButton.setToolTip("Add expanded columns and display results");

    updateFormState();
  }

  public void onRestrictToMatchingTypeClicked()
  {
    if (!suppressLocalUpdate)
    {
      refreshColumnComboBoxListStore();
    }
  }
  
  @Override
  protected void reconfigure()
  {
    saveProperties(configuredTableListStore.getProperties());
    restoreProperties(tableListStore.getProperties());
  }

  public void refreshColumnComboBoxListStore()
  {
    if (restrictToMatchingTypeCheckBox.getValue())
    {
      ListStore<ModelData> restrictedListStore = getMatchingTypeStore();
      columnComboBox.setStore(restrictedListStore);
    }
    else
    {
      columnComboBox.setStore(tableListStore);
    }
  }

  protected void restoreProperties(ModelData properties)
  {
    ExpandedColumnProperties expandedColumnProperties = properties.get(Keys.EXPANDED_COLUMN_PROPERTIES);
    if (expandedColumnProperties == null)
    {
      // Initialize to defaults on new table
      expanderComboBox.setValue(expanderListStore.getAt(0));
      fieldGrid.getSelectionModel().deselectAll();
      columnComboBox.setStore(getMatchingTypeStore());
      columnComboBox.clear();
      setRestrictToMatchingTypeCheckBoxValue(true);
    }
    else
    {
      ModelData expanderModelData = expandedColumnProperties.getExpanderModelData();
      List<ModelData> selectedItems = expandedColumnProperties.getSelectedItems();
      ListStore<ModelData> columnListStore = expandedColumnProperties.getColumnListStore();
      ModelData columnModelData = expandedColumnProperties.getColumnModelData();
      boolean isRestrictToMatchingType = expandedColumnProperties.isRestrictToMatchingType();

      expanderComboBox.setValue(expanderModelData);
      fieldGrid.getSelectionModel().setSelection(selectedItems);
      columnComboBox.setStore(columnListStore);
      columnComboBox.setValue(columnModelData);
      setRestrictToMatchingTypeCheckBoxValue(isRestrictToMatchingType);
    }
  }

  protected void saveProperties(ModelData properties)
  {
    ModelData expanderModelData = expanderComboBox.getValue();
    List<ModelData> selectedItems = fieldGrid.getSelectionModel().getSelectedItems();
    ListStore<ModelData> columnListStore = columnComboBox.getStore();
    ModelData columnModelData = columnComboBox.getValue();
    boolean isRestrictToMatchingType = restrictToMatchingTypeCheckBox.getValue();
    ExpandedColumnProperties expandedColumnProperties = new ExpandedColumnProperties(expanderModelData, selectedItems, columnListStore, columnModelData, isRestrictToMatchingType);
    properties.set(Keys.EXPANDED_COLUMN_PROPERTIES, expandedColumnProperties);
  }

  private void setInstance(AddExpandedColumns addExpandedColumns)
  {
    AddExpandedColumns.instance = addExpandedColumns;
  }

  private void setRestrictToMatchingTypeCheckBoxValue(boolean isRestrictToMatchingType)
  {
    suppressLocalUpdate = true;
    restrictToMatchingTypeCheckBox.setValue(isRestrictToMatchingType);
    suppressLocalUpdate = false;
  }

  public void updateFormState()
  {
    boolean fieldsSelected = checkBoxSelectionModel.getSelectedItems().size() > 0;
    boolean columnSelected = columnComboBox.getValue() != null;
    clearButton.setEnabled(fieldsSelected || columnSelected);
    expandButton.setEnabled(fieldsSelected && columnSelected);
  }

  private final class ClearButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      clear();
    }
  }

  public class ColumnSelectionChangedListener extends SelectionChangedListener<ModelData>
  {
    @Override
    public void selectionChanged(SelectionChangedEvent<ModelData> se)
    {
      updateFormState();
    }
  }

  private final class ColumnsToGenerateSelectionChangedListener extends SelectionChangedListener<ModelData>
  {
    @Override
    public void selectionChanged(SelectionChangedEvent<ModelData> se)
    {
      updateFormState();
    }
  }

  private final class ExpandButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      addExpandedColumns();
    }
  }

  private final class ExpandedColumnProperties
  {
    private ListStore<ModelData> columnListStore;
    private ModelData columnModelData;
    private ModelData expanderModelData;
    private boolean isRestrictToMatchingType;
    private List<ModelData> selectedItems;

    public ExpandedColumnProperties(ModelData expanderModelData, List<ModelData> selectedItems, ListStore<ModelData> columnListStore, ModelData columnModelData, boolean isRestrictToMatchingType)
    {
      this.expanderModelData = expanderModelData;
      this.selectedItems = selectedItems;
      this.columnListStore = columnListStore;
      this.columnModelData = columnModelData;
      this.isRestrictToMatchingType = isRestrictToMatchingType;
    }

    public ListStore<ModelData> getColumnListStore()
    {
      return columnListStore;
    }

    public ModelData getColumnModelData()
    {
      return columnModelData;
    }

    public ModelData getExpanderModelData()
    {
      return expanderModelData;
    }

    public List<ModelData> getSelectedItems()
    {
      return selectedItems;
    }

    public boolean isRestrictToMatchingType()
    {
      return isRestrictToMatchingType;
    }

  }

  public class ExpanderOperationSelectionChangedListener extends SelectionChangedListener<ModelData>
  {
    @Override
    public void selectionChanged(SelectionChangedEvent<ModelData> se)
    {
      ModelData selectedItem = expanderComboBox.getValue();
      if (selectedItem != null)
      {
        ListStore<ModelData> fieldListStore = selectedItem.get(Keys.PROPERTIES);
        fieldGrid.reconfigure(fieldListStore, fieldColumnModel);
        refreshColumnComboBoxListStore();
      }
    }
  }

  private final class RestrictToMatchingTypeListener implements Listener<FieldEvent>
  {
    @Override
    public void handleEvent(FieldEvent be)
    {
      onRestrictToMatchingTypeClicked();
    }

  }

}
