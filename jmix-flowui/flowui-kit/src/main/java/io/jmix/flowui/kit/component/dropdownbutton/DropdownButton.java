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

package io.jmix.flowui.kit.component.dropdownbutton;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.dom.ClassList;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.menubar.JmixMenuBar;
import io.jmix.flowui.kit.component.menubar.JmixMenuItem;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DropdownButton extends Composite<JmixMenuBar>
        implements AttachNotifier, DetachNotifier, HasTitle, HasSize,
        HasTheme, HasEnabled, HasStyle, HasText {

    protected boolean explicitTitle = false;
    protected List<HasMenuItem> items = new ArrayList<>();

    protected JmixMenuItem rootItem;
    protected Icon dropdownIcon = new Icon("lumo", "dropdown");
    protected Icon iconComponent;

    @Override
    protected JmixMenuBar initContent() {
        JmixMenuBar menuBar = super.initContent();
        rootItem = menuBar.addItem(new Button());

        rootItem.addThemeNames("icon");
        updateDropdownIconSlot();

        return menuBar;
    }

    protected JmixMenuItem getRootItem() {
        if (rootItem == null) {
            // root item will be initialized
            getContent();
        }
        return rootItem;
    }

    public DropdownButtonItem addItem(Action action, String id) {
        return addItem(action, id, -1);
    }

    public DropdownButtonItem addItem(Action action, String id, int index) {
        ActionItemImpl actionItem = new ActionItemImpl(action);
        actionItem.setParent(this);

        Div actionLayout = new Div();
        actionLayout.add(action.getText());


        MenuItem menuItem = index < 0
                ? getRootItem().getSubMenu().addItem(actionLayout)
                : getRootItem().getSubMenu().addItemAtIndex(index, actionLayout);

        actionItem.setItem(menuItem);
        actionItem.setActionLayout(actionLayout);
        actionItem.setId(id);
        actionItem.setAction(action);

        items.add(actionItem);
        return actionItem;
    }

    public DropdownButtonItem addItem(String text, String id) {
        TextItemImpl textItem = new TextItemImpl(text);

        textItem.setParent(this);
        textItem.setItem(getRootItem().getSubMenu().addItem(text));
        textItem.setId(id);

        items.add(textItem);
        return textItem;
    }

    public DropdownButtonItem addItem(Component component, String id) {
        ComponentItemImpl componentItem = new ComponentItemImpl(component);

        componentItem.setParent(this);
        componentItem.setItem(getRootItem().getSubMenu().addItem(component));
        componentItem.setId(id);

        items.add(componentItem);
        return componentItem;
    }

    public DropdownButtonItem addItem(String text,
                                      String id,
                                      ComponentEventListener<ClickEvent<MenuItem>> componentEventListener) {
        HasMenuItem textItem = (HasMenuItem) addItem(text, id);

        textItem.addClickListener(componentEventListener);

        items.add(textItem);
        return textItem;
    }

    public DropdownButtonItem addItem(Component component,
                                      String id,
                                      ComponentEventListener<ClickEvent<MenuItem>> componentEventListener) {
        HasMenuItem componentItem = (HasMenuItem) addItem(component, id);

        componentItem.addClickListener(componentEventListener);

        items.add(componentItem);
        return componentItem;
    }

    @Nullable
    public DropdownButtonItem getItem(String itemId) {
        return items.stream()
                .filter(item -> itemId.equals(item.getId()))
                .findAny()
                .orElse(null);
    }

    protected HasMenuItem getItemImpl(DropdownButtonItem item) {
        return items.stream()
                .filter(item::equals)
                .findAny()
                .orElseThrow(() -> new IllegalStateException(
                                String.format("%s is not contains item", getClass().getSimpleName())
                        )
                );
    }

    public List<DropdownButtonItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void remove(String itemId) {
        DropdownButtonItem item = getItem(itemId);

        if (item != null) {
            HasMenuItem element = getItemImpl(item);

            getRootItem().getSubMenu().remove(element.getItem());
            items.remove(element);
        }
    }

    public void remove(DropdownButtonItem item) {
        HasMenuItem element = getItemImpl(item);

        getRootItem().getSubMenu().remove(element.getItem());
        items.remove(element);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public void remove(DropdownButtonItem... items) {
        MenuItem[] arrayMenuItems = new MenuItem[items.length];
        ArrayList<MenuItem> menuItems = new ArrayList<>();

        Arrays.stream(items).forEach(item -> menuItems.add(getItemImpl(item).getItem()));

        getRootItem().getSubMenu().remove(menuItems.toArray(arrayMenuItems));
        this.items.removeAll(Arrays.stream(items).collect(Collectors.toList()));
    }

    public void removeAll() {
        items.clear();
        getRootItem().getSubMenu().removeAll();
    }

    public void addSeparator() {
        getRootItem().getSubMenu().add(new Hr());
    }

    public void addSeparatorAtIndex(int index) {
        getRootItem().getSubMenu().addComponentAtIndex(index, new Hr());
    }

    @Override
    public Registration addAttachListener(ComponentEventListener<AttachEvent> listener) {
        return getContent().addAttachListener(listener);
    }

    @Override
    public boolean isAttached() {
        return getContent().isAttached();
    }

    @Override
    public Registration addDetachListener(ComponentEventListener<DetachEvent> listener) {
        return getContent().addDetachListener(listener);
    }

    @Override
    public void setText(String text) {
        getRootItem().setText(text);

        if (!explicitTitle) {
            setTitleInternal(text);
        }

        updateDropdownIconSlot();
        updateIconSlot();
    }

    @Override
    public String getText() {
        return getRootItem().getText();
    }

    @Override
    public void setWhiteSpace(WhiteSpace value) {
        getRootItem().setWhiteSpace(value);
    }

    @Override
    public WhiteSpace getWhiteSpace() {
        return getRootItem().getWhiteSpace();
    }

    @Override
    public void setTitle(@Nullable String title) {
        explicitTitle = true;

        setTitleInternal(title);
    }

    protected void setTitleInternal(@Nullable String title) {
        HasTitle.super.setTitle(title);
    }

    public void setIcon(@Nullable Icon icon) {
        if (icon != null && icon.getElement().isTextNode()) {
            throw new IllegalArgumentException(
                    "Text node can't be used as an icon.");
        }
        if (iconComponent != null) {
            getRootItem().remove(iconComponent);
        }
        iconComponent = icon;

        updateIconSlot();
    }

    @Nullable
    public Icon getIcon() {
        return iconComponent;
    }

    protected void updateDropdownIconSlot() {
        getRootItem().add(dropdownIcon);
    }

    protected void updateIconSlot() {
        if (iconComponent != null) {
            getRootItem().addComponentAsFirst(iconComponent);
        }
    }

    @Override
    public void setClassName(String className) {
        getContent().setClassName(className);
    }

    @Override
    public void setClassName(String className, boolean set) {
        getContent().setClassName(className, set);
    }

    @Override
    public String getClassName() {
        return getContent().getClassName();
    }

    @Override
    public void addClassName(String className) {
        getContent().addClassName(className);
    }

    @Override
    public boolean removeClassName(String className) {
        return getContent().removeClassName(className);
    }

    @Override
    public ClassList getClassNames() {
        return getContent().getClassNames();
    }

    @Override
    public void addClassNames(String... classNames) {
        getContent().addClassNames(classNames);
    }

    @Override
    public void removeClassNames(String... classNames) {
        getContent().removeClassNames(classNames);
    }

    @Override
    public boolean hasClassName(String className) {
        return getContent().hasClassName(className);
    }

    @Override
    public void setThemeName(String themeName) {
        getContent().setThemeName(themeName);
    }

    @Override
    public void setThemeName(String themeName, boolean set) {
        getContent().setThemeName(themeName, set);
    }

    @Override
    public String getThemeName() {
        return getContent().getThemeName();
    }

    @Override
    public void addThemeName(String themeName) {
        getContent().addThemeName(themeName);
    }

    @Override
    public boolean removeThemeName(String themeName) {
        return getContent().removeThemeName(themeName);
    }

    @Override
    public boolean hasThemeName(String themeName) {
        return getContent().hasThemeName(themeName);
    }

    @Override
    public ThemeList getThemeNames() {
        return getContent().getThemeNames();
    }

    @Override
    public void addThemeNames(String... themeNames) {
        getContent().addThemeNames(themeNames);
    }

    @Override
    public void removeThemeNames(String... themeNames) {
        getContent().removeThemeNames(themeNames);
    }

    public void addThemeVariants(MenuBarVariant... variants) {
        getContent().addThemeVariants(variants);
    }

    public void removeThemeVariants(MenuBarVariant... variants) {
        getContent().removeThemeVariants(variants);
    }

    public void setOpenOnHover(boolean openOnHover) {
        getContent().setOpenOnHover(openOnHover);
    }

    public boolean isOpenOnHover() {
        return getContent().isOpenOnHover();
    }

    @Override
    public void setEnabled(boolean enabled) {
        getContent().setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return getContent().isEnabled();
    }

    @Override
    public void setWidth(String width) {
        getContent().setWidth(width);
    }

    @Override
    public void setWidth(float width, Unit unit) {
        getContent().setWidth(width, unit);
    }

    @Override
    public void setMinWidth(String minWidth) {
        getContent().setMinWidth(minWidth);
    }

    @Override
    public void setMinWidth(float minWidth, Unit unit) {
        getContent().setMinWidth(minWidth, unit);
    }

    @Override
    public void setMaxWidth(String maxWidth) {
        getContent().setMaxWidth(maxWidth);
    }

    @Override
    public void setMaxWidth(float maxWidth, Unit unit) {
        getContent().setMaxWidth(maxWidth, unit);
    }

    @Override
    public String getWidth() {
        return getContent().getWidth();
    }

    @Override
    public String getMinWidth() {
        return getContent().getMinWidth();
    }

    @Override
    public String getMaxWidth() {
        return getContent().getMaxWidth();
    }

    @Override
    public Optional<Unit> getWidthUnit() {
        return getContent().getWidthUnit();
    }

    @Override
    public void setHeight(String height) {
        getContent().setHeight(height);
    }

    @Override
    public void setHeight(float height, Unit unit) {
        getContent().setHeight(height, unit);
    }

    @Override
    public void setMinHeight(String minHeight) {
        getContent().setMinHeight(minHeight);
    }

    @Override
    public void setMinHeight(float minHeight, Unit unit) {
        getContent().setMinHeight(minHeight, unit);
    }

    @Override
    public void setMaxHeight(String maxHeight) {
        getContent().setMaxHeight(maxHeight);
    }

    @Override
    public void setMaxHeight(float maxHeight, Unit unit) {
        getContent().setMaxHeight(maxHeight, unit);
    }

    @Override
    public String getHeight() {
        return getContent().getHeight();
    }

    @Override
    public String getMinHeight() {
        return getContent().getMinHeight();
    }

    @Override
    public String getMaxHeight() {
        return getContent().getMaxHeight();
    }

    @Override
    public Optional<Unit> getHeightUnit() {
        return getContent().getHeightUnit();
    }

    @Override
    public void setSizeFull() {
        getContent().setSizeFull();
    }

    @Override
    public void setWidthFull() {
        getContent().setWidthFull();
    }

    @Override
    public void setHeightFull() {
        getContent().setHeightFull();
    }

    @Override
    public void setSizeUndefined() {
        getContent().setSizeUndefined();
    }

    protected static class ActionItemImpl implements ActionItem, HasMenuItem {

        protected DropdownButton parent;
        protected MenuItem item;
        protected String id;

        protected Action action;
        protected Icon iconComponent;
        protected Div actionLayout;

        protected ActionItemActionSupport actionSupport;

        public ActionItemImpl(Action action) {
            this.action = action;
        }

        @Override
        public void setParent(DropdownButton parent) {
            if (this.parent != null) {
                throw new UnsupportedOperationException(String.format(
                        "Item '%s' is already bound to the parent", this.id != null ? this.id : "null")
                );
            }
            this.parent = parent;
        }

        @Override
        public Component getParent() {
            return parent;
        }

        @Override
        public void setItem(MenuItem item) {
            this.item = item;
        }

        @Override
        public MenuItem getItem() {
            return item;
        }

        @Override
        public void setId(String id) {
            this.id = id;
            item.setId(id);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setAction(@Nullable Action action, boolean overrideComponentProperties) {
            this.action = action;
            getActionSupport().setAction(action, overrideComponentProperties);
        }

        @Override
        public Action getAction() {
            return getActionSupport().getAction();
        }

        public void setShortcutCombination(@Nullable KeyCombination shortcutCombination) {
            if (shortcutCombination != null) {
                item.addClickShortcut(shortcutCombination.getKey(), shortcutCombination.getKeyModifiers());
            }
        }

        public void setIcon(@Nullable Icon icon) {
            if (icon != null && icon.getElement().isTextNode()) {
                throw new IllegalArgumentException(
                        "Text node can't be used as an icon.");
            }
            if (iconComponent != null) {
                actionLayout.remove(iconComponent);
            }
            iconComponent = icon;
            if (icon != null) {
                actionLayout.addComponentAsFirst(iconComponent);
            }
            updateThemeAttribute();
        }

        @Nullable
        public Icon getIcon() {
            return iconComponent;
        }

        public void setVisible(boolean visible) {
            if (item != null) {
                item.setVisible(visible);
            }
        }

        public boolean isVisible() {
            return item != null && item.isVisible();
        }

        public void setEnabled(boolean enabled) {
            if (item != null) {
                item.setEnabled(enabled);
            }
        }

        public boolean isEnabled() {
            return item != null && item.isEnabled();
        }

        public void setText(@Nullable String text) {
            actionLayout.setText(text);
            if (iconComponent != null) {
                setIcon(iconComponent);
            }
        }

        @Nullable
        public String getText() {
            return actionLayout.getText();
        }

        public void setActionLayout(Div actionLayout) {
            this.actionLayout = actionLayout;
        }

        public Div getActionLayout() {
            return actionLayout;
        }

        protected void updateThemeAttribute() {
            if (iconComponent != null) {
                item.addThemeNames("icon");

                iconComponent.getStyle().set("width", "var(--lumo-icon-size-s)");
                iconComponent.getStyle().set("height", "var(--lumo-icon-size-s)");
                iconComponent.getStyle().set("marginRight", "var(--lumo-space-s)");
            } else {
                item.removeThemeNames("icon");
            }
        }

        protected ActionItemActionSupport getActionSupport() {
            if (actionSupport == null) {
                actionSupport = new ActionItemActionSupport(this, this.item);
            }
            return actionSupport;
        }
    }

    protected static class ComponentItemImpl implements ComponentItem, HasMenuItem {

        protected DropdownButton parent;
        protected MenuItem item;
        protected String id;

        protected Component content;

        public ComponentItemImpl(Component content) {
            this.content = content;
        }

        @Override
        public void setParent(DropdownButton parent) {
            if (this.parent != null) {
                throw new UnsupportedOperationException(String.format(
                        "Item '%s' is already bound to the parent", this.id != null ? this.id : "null")
                );
            }
            this.parent = parent;
        }

        @Override
        public Component getParent() {
            return parent;
        }

        @Override
        public void setItem(MenuItem item) {
            this.item = item;
        }

        @Override
        public MenuItem getItem() {
            return item;
        }

        @Override
        public void setId(String id) {
            this.id = id;
            item.setId(id);
        }

        @Override
        public String getId() {
            return id;
        }

        @Override
        public void setContent(Component content) {
            this.content = content;
        }

        @Override
        public Component getContent() {
            return content;
        }

        public void setVisible(boolean visible) {
            if (item != null) {
                item.setVisible(visible);
            }
        }

        public boolean isVisible() {
            return item != null && item.isVisible();
        }

        public void setEnabled(boolean enabled) {
            if (item != null) {
                item.setEnabled(enabled);
            }
        }

        public boolean isEnabled() {
            return item != null && item.isEnabled();
        }

        @Override
        public Registration addClickListener(ComponentEventListener<ClickEvent<MenuItem>> listener) {
            return item.addClickListener(listener);
        }

        @Override
        public ShortcutRegistration addClickShortcut(Key key, KeyModifier... keyModifiers) {
            return item.addClickShortcut(key, keyModifiers);
        }
    }

    protected static class TextItemImpl implements TextItem, HasMenuItem {

        protected DropdownButton parent;
        protected MenuItem item;
        protected String id;

        protected String text;

        public TextItemImpl(@Nullable String text) {
            this.text = text;
        }

        @Override
        public void setParent(DropdownButton parent) {
            if (this.parent != null) {
                throw new UnsupportedOperationException(String.format(
                        "Item '%s' is already bound to the parent", this.id != null ? this.id : "null")
                );
            }
            this.parent = parent;
        }

        @Override
        public Component getParent() {
            return parent;
        }

        @Override
        public void setItem(MenuItem item) {
            this.item = item;
        }

        @Override
        public MenuItem getItem() {
            return item;
        }

        @Override
        public void setId(String id) {
            this.id = id;
            item.setId(id);
        }

        @Override
        public String getId() {
            return id;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setVisible(boolean visible) {
            if (item != null) {
                item.setVisible(visible);
            }
        }

        public boolean isVisible() {
            return item != null && item.isVisible();
        }

        public void setEnabled(boolean enabled) {
            if (item != null) {
                item.setEnabled(enabled);
            }
        }

        public boolean isEnabled() {
            return item != null && item.isEnabled();
        }

        @Override
        public Registration addClickListener(ComponentEventListener<ClickEvent<MenuItem>> listener) {
            return item.addClickListener(listener);
        }

        @Override
        public ShortcutRegistration addClickShortcut(Key key, KeyModifier... keyModifiers) {
            return item.addClickShortcut(key, keyModifiers);
        }
    }

    protected interface HasMenuItem extends DropdownButtonItem {

        void setItem(MenuItem item);

        MenuItem getItem();
    }
}
