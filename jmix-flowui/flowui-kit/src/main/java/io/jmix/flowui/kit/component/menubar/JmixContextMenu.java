/*
 * Copyright 2022 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.jmix.flowui.kit.component.menubar;

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.contextmenu.ContextMenu;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.contextmenu.MenuManager;
import com.vaadin.flow.component.contextmenu.SubMenu;
import com.vaadin.flow.function.SerializableRunnable;
import io.jmix.flowui.kit.component.contextmenu.JmixMenuManager;

public class JmixContextMenu extends ContextMenu
        implements HasJmixMenuItems {

    /**
     * Creates an empty context menu.
     */
    public JmixContextMenu() {
        getElement().setAttribute("suppress-template-warning", true);
    }

    /**
     * Creates an empty context menu with the given target component.
     *
     * @param target the target component for this context menu
     * @see #setTarget(Component)
     */
    public JmixContextMenu(Component target) {
        this();
        setTarget(target);
    }

    @Override
    public JmixMenuItem addItem(String text,
                                ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return (JmixMenuItem) getMenuManager().addItem(text, clickListener);
    }

    @Override
    public JmixMenuItem addItem(Component component,
                                ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return (JmixMenuItem) getMenuManager().addItem(component, clickListener);
    }

    @Override
    public JmixMenuItem addItemAtIndex(int index, String text) {
        return (JmixMenuItem) getMenuManager().addItemAtIndex(index, text);
    }

    @Override
    public JmixMenuItem addItemAtIndex(int index, Component component) {
        return (JmixMenuItem) getMenuManager().addItemAtIndex(index, component);
    }

    @Override
    public JmixMenuItem addItemAtIndex(int index, String text,
                                       ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return (JmixMenuItem) getMenuManager().addItemAtIndex(index, text, clickListener);
    }

    @Override
    public JmixMenuItem addItemAtIndex(int index, Component component,
                                       ComponentEventListener<ClickEvent<MenuItem>> clickListener) {
        return (JmixMenuItem) getMenuManager().addItemAtIndex(index, component, clickListener);
    }

    @Override
    protected MenuManager<ContextMenu, MenuItem, SubMenu> createMenuManager(
            SerializableRunnable contentReset) {
        return new JmixMenuManager<>(this, contentReset, JmixMenuItem::new,
                MenuItem.class, null);
    }

    @Override
    protected JmixMenuManager<ContextMenu, MenuItem, SubMenu> getMenuManager() {
        return (JmixMenuManager<ContextMenu, MenuItem, SubMenu>) super.getMenuManager();
    }
}
