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
import com.vaadin.flow.component.HasEnabled;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.HasStyle;
import com.vaadin.flow.component.HasTheme;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.contextmenu.MenuItemsArrayGenerator;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.dependency.NpmPackage;
import com.vaadin.flow.component.menubar.MenuBarVariant;
import com.vaadin.flow.function.SerializableConsumer;
import io.jmix.flowui.kit.component.contextmenu.JmixMenuManager;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Tag("jmix-menu-bar")
@NpmPackage(value = "@vaadin/polymer-legacy-adapter", version = "23.1.6")
@JsModule("@vaadin/polymer-legacy-adapter/style-modules.js")
@JsModule("./menubarConnector.js")
@JsModule("@vaadin/menu-bar/src/vaadin-menu-bar.js")
@NpmPackage(value = "@vaadin/menu-bar", version = "23.1.6")
@NpmPackage(value = "@vaadin/vaadin-menu-bar", version = "23.1.6")
public class JmixMenuBar extends Component
        implements HasJmixMenuItems, HasSize, HasStyle, HasTheme, HasEnabled {

    protected JmixMenuManager<JmixMenuBar, JmixMenuItem, JmixSubMenu> menuManager;
    protected MenuItemsArrayGenerator<JmixMenuItem> menuItemsArrayGenerator;

    protected boolean updateScheduled = false;

    public JmixMenuBar() {
        menuItemsArrayGenerator = new MenuItemsArrayGenerator<>(this);

        menuManager = new JmixMenuManager<>(this, this::resetContent,
                (menu, contentReset) -> new JmixMenuBarRootItem(this, contentReset),
                JmixMenuItem.class, null);

        addAttachListener(event -> {
            String appId = event.getUI().getInternals().getAppId();
            initConnector(appId);
            resetContent();
        });
    }

    public JmixMenuItem addItem(String text) {
        return menuManager.addItem(text);
    }

    public JmixMenuItem addItem(Component component) {
        return menuManager.addItem(component);
    }

    @Override
    public JmixMenuItem addItem(String text,
                                ComponentEventListener<ClickEvent<JmixMenuItem>> clickListener) {
        return menuManager.addItem(text, clickListener);
    }

    @Override
    public JmixMenuItem addItem(Component component,
                                ComponentEventListener<ClickEvent<JmixMenuItem>> clickListener) {
        return menuManager.addItem(component, clickListener);
    }

    public JmixMenuItem addItemAtIndex(int index, String text) {
        return menuManager.addItemAtIndex(index, text);
    }

    public JmixMenuItem addItemAtIndex(int index, Component component) {
        return menuManager.addItemAtIndex(index, component);
    }

    @Override
    public JmixMenuItem addItemAtIndex(int index, String text,
                                       ComponentEventListener<ClickEvent<JmixMenuItem>> clickListener) {
        return menuManager.addItemAtIndex(index, text, clickListener);
    }

    @Override
    public JmixMenuItem addItemAtIndex(int index, Component component,
                                       ComponentEventListener<ClickEvent<JmixMenuItem>> clickListener) {
        return menuManager.addItemAtIndex(index, component, clickListener);
    }

    public List<JmixMenuItem> getItems() {
        return menuManager.getItems();
    }

    public void remove(JmixMenuItem... items) {
        menuManager.remove(items);
    }

    public void removeAll() {
        menuManager.removeAll();
    }

    @Override
    public Stream<Component> getChildren() {
        return menuManager.getChildren();
    }

    public void setOpenOnHover(boolean openOnHover) {
        getElement().setProperty("openOnHover", openOnHover);
    }

    public boolean isOpenOnHover() {
        return getElement().getProperty("openOnHover", false);
    }

    /**
     * Adds theme variants to the component.
     *
     * @param variants
     *            theme variants to add
     */
    public void addThemeVariants(MenuBarVariant... variants) {
        getThemeNames()
                .addAll(Stream.of(variants).map(MenuBarVariant::getVariantName)
                        .collect(Collectors.toList()));
    }

    /**
     * Removes theme variants from the component.
     *
     * @param variants
     *            theme variants to remove
     */
    public void removeThemeVariants(MenuBarVariant... variants) {
        Stream.of(variants).map(MenuBarVariant::getVariantName)
                .collect(Collectors.toList()).forEach(
                        getThemeNames()::remove);
    }

    void resetContent() {
        menuItemsArrayGenerator.generate();
        updateButtons();
    }

    void updateButtons() {
        if (updateScheduled) {
            return;
        }
        runBeforeClientResponse(ui -> {
            // When calling `generateItems` without providing a node id, it will
            // use the previously generated items tree, only updating the
            // disabled and hidden properties of the root items = the menu bar
            // buttons.
            getElement().executeJs("this.$connector.generateItems()");
            updateScheduled = false;
        });
        updateScheduled = true;
    }

    protected void initConnector(String appId) {
        getElement().executeJs(
                "window.Vaadin.Flow.menubarConnector.initLazy(this, $0)",
                appId);
    }

    protected void runBeforeClientResponse(SerializableConsumer<UI> command) {
        getElement().getNode().runWhenAttached(ui -> ui
                .beforeClientResponse(this, context -> command.accept(ui)));
    }
}
