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

package io.jmix.flowui.component.dropdownbutton;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.html.Hr;
import com.vaadin.flow.component.menubar.MenuBar;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.dom.ClassList;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.dom.ThemeList;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.HasAction;
import io.jmix.flowui.kit.component.HasTitle;
import io.jmix.flowui.kit.component.KeyCombination;
import io.jmix.flowui.kit.component.dropdownbutton.ActionItem;
import io.jmix.flowui.kit.component.dropdownbutton.ComponentItem;
import io.jmix.flowui.kit.component.dropdownbutton.DropdownButtonItem;
import io.jmix.flowui.kit.component.dropdownbutton.TextItem;
import org.springframework.beans.factory.InitializingBean;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class DropdownButton extends Composite<MenuBar>
        implements AttachNotifier, DetachNotifier,
        HasTitle, HasSize, HasTheme, HasEnabled, InitializingBean, HasStyle, HasText {

    protected boolean explicitTitle = false;
    protected boolean iconAfterText = false;
    protected MenuItem rootItem = null;
    protected Component iconComponent = null;
    protected List<HasMenuItem> items = new ArrayList<>();
    protected MenuBar content = null;

    @Override
    protected MenuBar initContent() {
        MenuBar menuBar = super.initContent();
        rootItem = menuBar.addItem(new Button());
        return menuBar;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        content = getContent();
    }

    public DropdownButtonItem addItem(@Nullable Action action) {
        ActionItemImpl actionItem = new ActionItemImpl(action);
        actionItem.setParent(this);


        MenuItem menuItem = rootItem.getSubMenu()
                .addItem(action != null ? action.getText() : null);

        actionItem.setItem(menuItem);
        actionItem.setAction(action);
        items.add(actionItem);
        return actionItem;
    }

    public DropdownButtonItem addItem(@Nullable String text) {
        TextItemImpl textItem = new TextItemImpl(text);
        textItem.setParent(this);
        textItem.setItem(rootItem.getSubMenu().addItem(text));
        items.add(textItem);
        return textItem;
    }

    public DropdownButtonItem addItem(@Nullable Component component) {
        ComponentItemImpl componentItem = new ComponentItemImpl(component);
        componentItem.setParent(this);
        componentItem.setItem(rootItem.getSubMenu().addItem(component));
        items.add(componentItem);
        return componentItem;
    }

    public DropdownButtonItem addItem(String text,
                                      ComponentEventListener<ClickEvent<MenuItem>> componentEventListener) {
        HasMenuItem textItem = (HasMenuItem) addItem(text);
        textItem.addClickListener(componentEventListener);
        items.add(textItem);
        return textItem;
    }

    public DropdownButtonItem addItem(Component component,
                                      ComponentEventListener<ClickEvent<MenuItem>> componentEventListener) {
        HasMenuItem componentItem = (HasMenuItem) addItem(component);
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

    protected HasMenuItem _getItem(DropdownButtonItem item) {
        return items.stream()
                .filter(item::equals)
                .findAny()
                .orElseThrow(() -> new UnsupportedOperationException("DropdownButton is not contains item"));
    }

    public List<DropdownButtonItem> getItems() {
        return new ArrayList<>(items);
    }

    public void remove(String itemId) {
        DropdownButtonItem item = getItem(itemId);

        if (item != null) {
            HasMenuItem element = _getItem(item);

            rootItem.getSubMenu().remove(element.getItem());
            items.remove(element);
        }
    }

    public void remove(DropdownButtonItem item) {
        HasMenuItem element = _getItem(item);
        rootItem.getSubMenu().remove(element.getItem());
        items.remove(element);
    }

    @SuppressWarnings("SuspiciousMethodCalls")
    public void remove(DropdownButtonItem... items) {
        MenuItem[] arrayMenuItems = new MenuItem[items.length];
        ArrayList<MenuItem> menuItems = new ArrayList<>();

        Arrays.stream(items).forEach(item -> menuItems.add(_getItem(item).getItem()));

        rootItem.getSubMenu().remove(menuItems.toArray(arrayMenuItems));
        this.items.removeAll(Arrays.stream(items).collect(Collectors.toList()));
    }

    public void removeAll() {
        items.clear();
        rootItem.getSubMenu().removeAll();
    }

    public void addSeparator() {
        rootItem.getSubMenu().add(new Hr());
    }

    @Override
    public Registration addAttachListener(ComponentEventListener<AttachEvent> listener) {
        return content.addAttachListener(listener);
    }

    @Override
    public boolean isAttached() {
        return content.isAttached();
    }

    @Override
    public Registration addDetachListener(ComponentEventListener<DetachEvent> listener) {
        return content.addDetachListener(listener);
    }

    @Override
    public void setText(String text) {
        rootItem.setText(text);

        if (!explicitTitle) {
            setTitleInternal(text);
        }

        updateThemeAttribute();
    }

    @Override
    public String getText() {
        return rootItem.getText();
    }

    @Override
    public void setWhiteSpace(WhiteSpace value) {
        rootItem.setWhiteSpace(value);
    }

    @Override
    public WhiteSpace getWhiteSpace() {
        return rootItem.getWhiteSpace();
    }

    @Override
    public void setTitle(@Nullable String title) {
        explicitTitle = true;

        setTitleInternal(title);
    }

    protected void setTitleInternal(@Nullable String title) {
        HasTitle.super.setTitle(title);
    }

    public void setIcon(@Nullable Component icon) {
        if (icon != null && icon.getElement().isTextNode()) {
            throw new IllegalArgumentException(
                    "Text node can't be used as an icon.");
        }
        if (iconComponent != null) {
            rootItem.remove(iconComponent);
        }
        iconComponent = icon;
        if (icon != null) {
            updateIconSlot();
        }
        updateThemeAttribute();
    }

    @Nullable
    public Component getIcon() {
        return iconComponent;
    }

    private void updateThemeAttribute() {
        if (iconComponent != null) {
            rootItem.addThemeNames("icon");
        } else {
            rootItem.removeThemeNames("icon");
        }
    }

    protected void updateIconSlot() {
        if (iconAfterText) {
            rootItem.add(iconComponent);
        } else {
            rootItem.addComponentAsFirst(iconComponent);
        }
    }

    public void setIconAfterText(boolean iconAfterText) {
        this.iconAfterText = iconAfterText;
        if (iconComponent != null) {
            rootItem.remove(iconComponent);
            updateIconSlot();
        }
    }

    //TODO: kremnevda, protect 26.09.2022
    @Override
    public Element getElement() {
        return super.getElement();
    }

    public boolean isIconAfterText() {
        return this.iconAfterText;
    }

    @Override
    public void setClassName(String className) {
        content.setClassName(className);
    }

    @Override
    public void setClassName(String className, boolean set) {
        content.setClassName(className, set);
    }

    @Override
    public String getClassName() {
        return content.getClassName();
    }

    @Override
    public void addClassName(String className) {
        content.addClassName(className);
    }

    @Override
    public boolean removeClassName(String className) {
        return content.removeClassName(className);
    }

    @Override
    public ClassList getClassNames() {
        return content.getClassNames();
    }

    @Override
    public void addClassNames(String... classNames) {
        content.addClassNames(classNames);
    }

    @Override
    public void removeClassNames(String... classNames) {
        content.removeClassNames(classNames);
    }

    @Override
    public boolean hasClassName(String className) {
        return content.hasClassName(className);
    }

    @Override
    public void setThemeName(String themeName) {
        content.setThemeName(themeName);
    }

    @Override
    public void setThemeName(String themeName, boolean set) {
        content.setThemeName(themeName, set);
    }

    @Override
    public String getThemeName() {
        return content.getThemeName();
    }

    @Override
    public void addThemeName(String themeName) {
        content.addThemeName(themeName);
    }

    @Override
    public boolean removeThemeName(String themeName) {
        return content.removeThemeName(themeName);
    }

    @Override
    public boolean hasThemeName(String themeName) {
        return content.hasThemeName(themeName);
    }

    @Override
    public ThemeList getThemeNames() {
        return content.getThemeNames();
    }

    @Override
    public void addThemeNames(String... themeNames) {
        content.removeThemeNames(themeNames);
    }

    @Override
    public void removeThemeNames(String... themeNames) {
        content.removeThemeNames(themeNames);
    }

    public void addThemeVariants(MenuBarVariant... variants) {
        content.addThemeVariants(variants);
    }

    public void removeThemeVariants(MenuBarVariant... variants) {
        content.removeThemeVariants(variants);
    }

    public void setOpenOnHover(boolean openOnHover) {
        content.setOpenOnHover(openOnHover);
    }

    public boolean isOpenOnHover() {
        return content.isOpenOnHover();
    }

    @Override
    public void setEnabled(boolean enabled) {
        content.setEnabled(enabled);
    }

    @Override
    public boolean isEnabled() {
        return content.isEnabled();
    }

    @Override
    public void setWidth(String width) {
        content.setWidth(width);
    }

    @Override
    public void setWidth(float width, Unit unit) {
        content.setWidth(width, unit);
    }

    @Override
    public void setMinWidth(String minWidth) {
        content.setMinWidth(minWidth);
    }

    @Override
    public void setMinWidth(float minWidth, Unit unit) {
        content.setMinWidth(minWidth, unit);
    }

    @Override
    public void setMaxWidth(String maxWidth) {
        content.setMaxWidth(maxWidth);
    }

    @Override
    public void setMaxWidth(float maxWidth, Unit unit) {
        content.setMaxWidth(maxWidth, unit);
    }

    @Override
    public String getWidth() {
        return content.getWidth();
    }

    @Override
    public String getMinWidth() {
        return content.getMinWidth();
    }

    @Override
    public String getMaxWidth() {
        return content.getMaxWidth();
    }

    @Override
    public Optional<Unit> getWidthUnit() {
        return content.getWidthUnit();
    }

    @Override
    public void setHeight(String height) {
        content.setHeight(height);
    }

    @Override
    public void setHeight(float height, Unit unit) {
        content.setHeight(height, unit);
    }

    @Override
    public void setMinHeight(String minHeight) {
        content.setMinHeight(minHeight);
    }

    @Override
    public void setMinHeight(float minHeight, Unit unit) {
        content.setMinHeight(minHeight, unit);
    }

    @Override
    public void setMaxHeight(String maxHeight) {
        content.setMaxHeight(maxHeight);
    }

    @Override
    public void setMaxHeight(float maxHeight, Unit unit) {
        content.setMaxHeight(maxHeight, unit);
    }

    @Override
    public String getHeight() {
        return content.getHeight();
    }

    @Override
    public String getMinHeight() {
        return content.getMinHeight();
    }

    @Override
    public String getMaxHeight() {
        return content.getMaxHeight();
    }

    @Override
    public Optional<Unit> getHeightUnit() {
        return content.getHeightUnit();
    }

    @Override
    public void setSizeFull() {
        content.setSizeFull();
    }


    @Override
    public void setWidthFull() {
        content.setWidthFull();
    }

    @Override
    public void setHeightFull() {
        content.setHeightFull();
    }

    @Override
    public void setSizeUndefined() {
        content.setSizeUndefined();
    }

    protected static class ActionItemImpl extends ActionItem implements HasMenuItem, HasAction {

        protected ActionItemActionSupport actionSupport;

        public ActionItemImpl(@Nullable Action action) {
            super(action);
        }

        @Override
        public void setItem(MenuItem item) {
            super.item = item;
        }

        @Nullable
        @Override
        public MenuItem getItem() {
            return item;
        }

        @Override
        public void setAction(@Nullable Action action, boolean overrideComponentProperties) {
            this.action = action;
            getActionSupport().setAction(action, overrideComponentProperties);
        }

        @Nullable
        @Override
        public Action getAction() {
            return getActionSupport().getAction();
        }

        public void setShortcutCombination(@Nullable KeyCombination shortcutCombination) {
            if (shortcutCombination != null) {
                item.addClickShortcut(shortcutCombination.getKey(), shortcutCombination.getKeyModifiers());
            }
        }

        protected ActionItemActionSupport getActionSupport() {
            if (actionSupport == null) {
                actionSupport = new ActionItemActionSupport(this, this.item);
            }
            return actionSupport;
        }
    }

    protected static class ComponentItemImpl extends ComponentItem implements HasMenuItem {

        public ComponentItemImpl(@Nullable Component component) {
            super(component);
        }

        @Override
        public void setItem(MenuItem item) {
            super.item = item;
        }

        @Nullable
        @Override
        public MenuItem getItem() {
            return item;
        }
    }

    protected static class TextItemImpl extends TextItem implements HasMenuItem {

        public TextItemImpl(@Nullable String text) {
            super(text);
        }

        @Override
        public void setItem(MenuItem item) {
            super.item = item;
        }

        @Nullable
        @Override
        public MenuItem getItem() {
            return item;
        }
    }

    protected interface HasMenuItem extends DropdownButtonItem {

        void setItem(MenuItem item);

        @Nullable
        MenuItem getItem();
    }
}
