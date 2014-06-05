// Copyright 2010 Anthony F. Stuart - All rights reserved.
//
// This program and the accompanying materials are made available
// under the terms of the GNU General Public License. For other license
// options please contact the copyright owner.
//
// This program is made available on an "as is" basis, without
// warranties or conditions of any kind, either express or implied.

package com.example.analyzer.client.widgets;

import java.util.HashMap;

import com.example.analyzer.client.Utilities;
import com.extjs.gxt.ui.client.fx.BaseEffect;
import com.extjs.gxt.ui.client.fx.Fx;
import com.extjs.gxt.ui.client.fx.FxConfig;
import com.extjs.gxt.ui.client.util.Rectangle;
import com.extjs.gxt.ui.client.widget.BoxComponent;

// morphIn - put into a separate window
// morphOut - take out of a separate window

public class ViewManager
{
  private static final int FX_INTERVAL = 750;

  private ManagedItem autoSelectedItem;
  private HashMap<String, ManagedItem> managedItems = new HashMap<String, ManagedItem>();
  private ManagedPanel managedPanel;
  private HashMap<String, WindowWrapper> windowWrappers = new HashMap<String, WindowWrapper>();

  public ViewManager(ManagedPanel managedPanel)
  {
    this.managedPanel = managedPanel;
  }

  public void addManagedItem(ManagedItem managedItem)
  {
    ViewPanel viewPanel = managedItem.getViewPanel();
    String heading = viewPanel.getHeading();
    managedItems.put(heading, managedItem);
  }

  public int findByHeading(ManagedItem managedItem)
  {
    String heading = managedItem.getHeading();
    return findManagedItemByHeading(heading);
  }

  public int findInsertPoint(ManagedItem managedItem)
  {
    String heading = managedItem.getHeading();
    return findInsertPoint(heading);
  }

  public int findInsertPoint(String heading)
  {
    int itemCount = managedPanel.getManagedItemCount();
    for (int itemOffset = 0; itemOffset < itemCount; itemOffset++)
    {
      ManagedItem managedTabItem = managedPanel.getManagedItem(itemOffset);
      if (Utilities.compareAlphaNumerically(managedTabItem.getHeading(), heading) > 0)
      {
        return itemOffset;
      }
    }
    return itemCount;
  }

  public int findManagedItemByHeading(String heading)
  {
    int itemCount = managedPanel.getManagedItemCount();
    for (int itemOffset = 0; itemOffset < itemCount; itemOffset++)
    {
      ManagedItem item = managedPanel.getManagedItem(itemOffset);
      if (item.getHeading().equals(heading))
      {
        return itemOffset;
      }
    }
    return -1;
  }

  public ManagedItem getManagedItem(ViewPanel viewPanel)
  {
    String heading = viewPanel.getHeading();
    return managedItems.get(heading);
  }

  private int getReinsertOffset(WindowWrapper windowWrapper)
  {
    String heading = windowWrapper.getHeading();
    int itemOffset = findInsertPoint(heading);
    int reinsertOffset;
    boolean isActivateOnReinsert = isActivateOnReinsert();
    if (isActivateOnReinsert)
    {
      reinsertOffset = itemOffset - 1;
      if (reinsertOffset < 0)
      {
        reinsertOffset = 0; // reinsert and activate first item
      }
    }
    else
    {
      reinsertOffset = itemOffset;
    }
    return reinsertOffset;
  }

  public WindowWrapper getWindowWrapper(ViewPanel viewPanel)
  {
    String heading = viewPanel.getHeading();
    WindowWrapper windowWrapper = windowWrappers.get(heading);
    if (windowWrapper == null)
    {
      windowWrapper = new WindowWrapper(this, viewPanel);
      windowWrappers.put(heading, windowWrapper);
    }
    return windowWrapper;
  }

  public void insertByHeading(ManagedItem managedItem)
  {
    int itemOffset = findInsertPoint(managedItem);
    managedPanel.insertManagedItem(managedItem, itemOffset);
  }

  public boolean isActivateOnReinsert()
  {
    boolean isActivateOnReinsert = autoSelectedItem != null && !autoSelectedItem.isCollapsed();
    return isActivateOnReinsert;
  }

  public void morphIn(ManagedItem managedItem)
  {
    ViewPanel viewPanel = managedItem.getViewPanel();
    Rectangle fromBounds = managedPanel.getFromBounds(managedItem);
    selectNextManagedItem(managedItem);
    managedItem.deactivate();
    managedPanel.removeManagedItem(managedItem);

    WindowWrapper windowWrapper = getWindowWrapper(viewPanel);
    Rectangle toBounds = windowWrapper.getMorphInBounds();
    windowWrapper.setBounds(fromBounds);
    windowWrapper.show();
    windowWrapper.toFront();

    morphIn(windowWrapper, fromBounds, toBounds);
  }

  public void morphIn(String heading)
  {
    int itemOffset = findManagedItemByHeading(heading);
    if (itemOffset == -1)
    {
      WindowWrapper windowWrapper = windowWrappers.get(heading);
      windowWrapper.toFront();
      windowWrapper.focus();
    }
    else
    {
      ManagedItem managedItem = managedPanel.getManagedItem(itemOffset);
      morphIn(managedItem);
    }
  }

  public void morphIn(final WindowWrapper windowWrapper, Rectangle fromBounds, Rectangle toBounds)
  {
    MorphEffect morphEffect = new MorphEffect(windowWrapper, fromBounds, toBounds, new Runnable()
    {
      @Override
      public void run()
      {
        windowWrapper.activate();
        windowWrapper.focus();
      }
    });
    Fx fx = new Fx(new FxConfig(FX_INTERVAL));
    fx.run(morphEffect);
  }

  public void morphOut(String heading)
  {
    WindowWrapper windowWrapper = windowWrappers.get(heading);
    if (windowWrapper != null)
    {
      morphOut(windowWrapper);
    }
  }

  public void morphOut(WindowWrapper windowWrapper)
  {
    Rectangle fromBounds = windowWrapper.saveMorphInBounds();
    int visualOffset = getReinsertOffset(windowWrapper);
    Rectangle toBounds = managedPanel.getToBounds(visualOffset);
    windowWrapper.deactivate();
    morphOut(windowWrapper, fromBounds, toBounds);
  }

  public void morphOut(final WindowWrapper windowWrapper, Rectangle fromBounds, Rectangle toBounds)
  {
    MorphEffect morphEffect = new MorphEffect(windowWrapper, fromBounds, toBounds, new Runnable()
    {
      @Override
      public void run()
      {
        windowWrapper.hide();
        ViewPanel viewPanel = windowWrapper.getViewPanel();
        ManagedItem managedItem = getManagedItem(viewPanel);
        insertByHeading(managedItem);
        managedItem.activate(); // must occur after insert, otherwise size is zero
        if (isActivateOnReinsert() || managedPanel.getManagedItemCount() == 1)
        {
          managedPanel.selectManagedItem(managedItem);
        }
      }
    });

    Fx fx = new Fx(new FxConfig(FX_INTERVAL));
    fx.run(morphEffect);
  }

  public void selectNextManagedItem(ManagedItem managedItem)
  {
    ViewPanel viewPanel = managedItem.getViewPanel();
    if (viewPanel.isVisibleWithGxtWorkaround())
    {
      // View is currently visible (selected)
      int itemOffset = findByHeading(managedItem);
      if (itemOffset > 0)
      {
        // Not the first, so select previous item
        ManagedItem item = managedPanel.getManagedItem((itemOffset - 1));
        managedPanel.selectManagedItem(item);
        autoSelectedItem = item;
      }
      else if ((itemOffset + 1) < managedPanel.getManagedItemCount())
      {
        // First, but not last, so select next item
        ManagedItem item = managedPanel.getManagedItem((itemOffset + 1));
        managedPanel.selectManagedItem(item);
        autoSelectedItem = item;
      }
      else
      {
        // Only one item in this managed panel
        autoSelectedItem = null;
      }
    }
    else
    {
      // View is not visible (i.e. we're not the visible item)
      autoSelectedItem = null;
    }
  }

  public void selectViewPanel(ViewPanel viewPanel)
  {
    String heading = viewPanel.getHeading();
    WindowWrapper windowWrapper = windowWrappers.get(heading);
    if (windowWrapper == null || windowWrapper.getViewPanel().getParent() != windowWrapper)
    {
      ManagedItem managedItem = managedItems.get(heading);
      managedPanel.selectManagedItem(managedItem);
    }
    else
    {
      windowWrapper.toFront();
      windowWrapper.focus();
    }
  }

  public void setActiveViewPanel(ViewPanel viewPanel)
  {
    ManagedItem managedItem = getManagedItem(viewPanel);
    managedPanel.selectManagedItem(managedItem);
  }

  public class MorphEffect extends BaseEffect
  {
    private BoxComponent component;
    private Rectangle fromBounds;
    private Runnable runnable;
    private Rectangle toBounds;

    public MorphEffect(BoxComponent component, Rectangle fromBounds, Rectangle toBounds, Runnable runnable)
    {
      super(component.el());

      this.component = component;
      this.fromBounds = fromBounds;
      this.toBounds = toBounds;
      this.runnable = runnable;
    }

    @Override
    public void onComplete()
    {
      super.onComplete();
      //el.setBounds(toX, toY, toWidth, toHeight);
      component.setBounds(toBounds);
      if (runnable != null)
      {
        runnable.run();
      }
    }

    @Override
    public void onUpdate(double progress)
    {
      int x = (int)getValue(fromBounds.x, toBounds.x, progress);
      int y = (int)getValue(fromBounds.y, toBounds.y, progress);
      int width = (int)getValue(fromBounds.width, toBounds.width, progress);
      int height = (int)getValue(fromBounds.height, toBounds.height, progress);

      //el.setBounds(x, y, width, height);
      component.setBounds(x, y, width, height);
    }
  }

}
