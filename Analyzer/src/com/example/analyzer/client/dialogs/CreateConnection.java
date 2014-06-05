// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.dialogs;

import com.example.analyzer.client.Analyzer;
import com.example.analyzer.extgwt.tools.layout.constrained.ConstrainedLayoutContainer;
import com.example.analyzer.extgwt.tools.layout.constrained.Constraint;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.ComponentEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.FieldEvent;
import com.extjs.gxt.ui.client.event.KeyListener;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.CheckBox;
import com.extjs.gxt.ui.client.widget.form.TextField;
import com.extjs.gxt.ui.client.widget.layout.FitLayout;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Element;

public class CreateConnection extends Window
{
  private static CreateConnection instance;

  public static CreateConnection getInstance()
  {
    return CreateConnection.instance;
  }

  private Button clearButton;
  private CheckBox closeOnConnectCheckBox;
  private TextField<String> connectionNameTextField;
  private Button createButton;
  private TextField<String> passwordTextField;
  private TextField<String> urlTextField;
  private TextField<String> userIdTextField;

  public CreateConnection()
  {
    setInstance(this);
  }

  private void clear()
  {
    connectionNameTextField.clear();
    urlTextField.clear();
    userIdTextField.clear();
    passwordTextField.clear();
    updateFormState();
  }

  private void copyUrlToName()
  {
    connectionNameTextField.setValue(urlTextField.getValue());
  }

  private void createConnection()
  {
    String connectionName = connectionNameTextField.getValue();
    String url = urlTextField.getValue();
    String userId = userIdTextField.getValue();
    String password = passwordTextField.getValue();
    Analyzer.getInstance().createConnection(connectionName, url, userId, password);
  }

  private boolean isSet(String url)
  {
    return url != null && !url.isEmpty();
  }

  @Override
  protected void onRender(Element parent, int index)
  {
    super.onRender(parent, index);
    setLayout(new FitLayout());
    setHeading("Create Connection");
    add(new CreateConnectionPanel());
    setSize(Analyzer.DEFAULT_POPUP_WIDTH, Analyzer.DEFAULT_POPUP_HEIGHT);
  }

  private void setInstance(CreateConnection createConnection)
  {
    CreateConnection.instance = createConnection;
  }

  public void updateConnections()
  {
    if (closeOnConnectCheckBox != null && closeOnConnectCheckBox.getValue())
    {
      hide();
    }
  }

  private void updateFormState()
  {
    boolean isSetUrl = isSet(urlTextField.getValue());
    boolean isSetConnectionName = isSet(connectionNameTextField.getValue());
    boolean isSetUserId = isSet(userIdTextField.getValue());
    boolean isSetPassword = isSet(passwordTextField.getValue());

    clearButton.setEnabled(isSetUrl || isSetConnectionName || isSetUserId || isSetPassword);
    createButton.setEnabled(isSetUrl && isSetConnectionName);
  }

  private final class ButtonStateListener extends KeyListener
  {
    @Override
    public void componentKeyUp(ComponentEvent event)
    {
      updateFormState();
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

  private final class CopyUrlToNameAndEnterKeyListener extends KeyListener
  {
    private boolean isCopyUrlToName;

    private CopyUrlToNameAndEnterKeyListener(boolean isCopyUrlToName)
    {
      this.isCopyUrlToName = isCopyUrlToName;
    }

    @Override
    public void componentKeyUp(ComponentEvent event)
    {
      super.componentKeyUp(event);
      if (isCopyUrlToName)
      {
        copyUrlToName();
      }
      int keyCode = event.getKeyCode();
      if (keyCode == KeyCodes.KEY_ENTER)
      {
        createConnection();
      }
    }
  }

  private final class CopyUrlToNameListener implements Listener<FieldEvent>
  {
    @Override
    public void handleEvent(FieldEvent be)
    {
      copyUrlToName();
    }
  }

  private final class CreateConnectionPanel extends ConstrainedLayoutContainer
  {
    @Override
    protected void onRender(Element parent, int index)
    {
      super.onRender(parent, index);

      addStyleName("dm-form-font"); // Let the layout know what font we're using

      passwordTextField = new TextField<String>();
      passwordTextField.setPassword(true);

      ButtonStateListener buttonStateListener = new ButtonStateListener();
      CopyUrlToNameAndEnterKeyListener enterKeyListener = new CopyUrlToNameAndEnterKeyListener(false);

      add(new Label("Database JDBC URL:"), new Constraint("w=1,t=10,l=5,r=5"));
      add(urlTextField = new TextField<String>(), new Constraint("w=1,l=5,r=5"));
      urlTextField.setValue("jdbc:derby://localhost:1527/");
      urlTextField.addListener(Events.Change, new CopyUrlToNameListener());
      urlTextField.addKeyListener(buttonStateListener);
      urlTextField.addKeyListener(new CopyUrlToNameAndEnterKeyListener(true));
      setFocusWidget(urlTextField);

      add(new Label("User Defined Connection Name:"), new Constraint("w=1,t=10,l=5,r=5"));
      add(connectionNameTextField = new TextField<String>(), new Constraint("w=1,l=5,r=5"));
      connectionNameTextField.addKeyListener(buttonStateListener);
      connectionNameTextField.addKeyListener(enterKeyListener);

      add(new Label("Connection User ID:"), new Constraint("w=1,t=10,l=5,r=5"));
      add(userIdTextField = new TextField<String>(), new Constraint("w=1,l=5,r=5"));
      userIdTextField.addKeyListener(buttonStateListener);
      userIdTextField.addKeyListener(enterKeyListener);

      add(new Label("Connection Password:"), new Constraint("w=1,t=10,l=5,r=5"));
      add(passwordTextField, new Constraint("w=1,l=5,r=5"));
      passwordTextField.addKeyListener(buttonStateListener);
      passwordTextField.addKeyListener(enterKeyListener);

      closeOnConnectCheckBox = new CheckBox();
      add(closeOnConnectCheckBox, new Constraint("t=10,l=5"));
      closeOnConnectCheckBox.setValue(true);
      add(new Label("Close on Connect"), new Constraint("s,t=10,l=5,V=m"));
      add(new Label(), new Constraint("s,w=-1"));
      add(clearButton = new Button("Clear", new ClearButtonListener()), new Constraint("s,t=10,l=5"));
      add(createButton = new Button("Create", new OkayButtonListener()), new Constraint("s,t=10,l=5,r=5"));

      updateFormState();
    }
  }

  private final class OkayButtonListener extends SelectionListener<ButtonEvent>
  {
    @Override
    public void componentSelected(ButtonEvent ce)
    {
      createConnection();
    }
  }

}
