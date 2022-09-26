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

package io.jmix.flowui.xml.layout.loader.component;

import com.vaadin.flow.component.Component;
import io.jmix.flowui.component.dropdownbutton.DropdownButton;
import io.jmix.flowui.exception.GuiDevelopmentException;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.dropdownbutton.ActionItem;
import io.jmix.flowui.kit.component.dropdownbutton.ComponentItem;
import io.jmix.flowui.kit.component.dropdownbutton.TextItem;
import io.jmix.flowui.xml.layout.ComponentLoader;
import io.jmix.flowui.xml.layout.inittask.AssignDropdownButtonActionInitTask;
import io.jmix.flowui.xml.layout.loader.AbstractComponentLoader;
import io.jmix.flowui.xml.layout.loader.LayoutLoader;
import io.jmix.flowui.xml.layout.support.ActionLoaderSupport;
import org.dom4j.Element;

public class DropdownButtonLoader extends AbstractComponentLoader<DropdownButton> {

    protected ActionLoaderSupport actionLoaderSupport;

    @Override
    protected DropdownButton createComponent() {
        return factory.create(DropdownButton.class);
    }

    @Override
    public void loadComponent() {
        loadBoolean(element, "openOnHover", resultComponent::setOpenOnHover);
        loadBoolean(element, "iconAfterText", resultComponent::setIconAfterText);

        componentLoader().loadSizeAttributes(resultComponent, element);
        componentLoader().loadTitle(resultComponent, element, context);
        componentLoader().loadClassName(resultComponent, element);
        componentLoader().loadThemeName(resultComponent, element);
        componentLoader().loadEnabled(resultComponent, element);
        componentLoader().loadText(resultComponent, element);
        componentLoader().loadWhiteSpace(resultComponent, element);
        componentLoader().loadIcon(element, resultComponent::setIcon);

        loadContent();
    }

    protected void loadContent() {
        Element items = element.element("items");
        items.elements().forEach(this::loadItem);
    }

    protected void loadItem(Element element) {
        if ("actionItem".equals(element.getName())) {
            loadActionItem(element);
        } else if ("componentItem".equals(element.getName())) {
            loadComponentItem(element);
        } else if ("textItem".equals(element.getName())) {
            loadTextItem(element);
        } else if ("separator".equals(element.getName())) {
            loadSeparator();
        } else {
            throw new GuiDevelopmentException("Unexpected dropdownButtonItem", context);
        }
    }

    protected void loadActionItem(Element element) {
        String id = getLoaderSupport().loadString(element, "id")
                .orElse(null);
        ActionItem actionItem;

        String ref = element.attributeValue("ref");
        Element actionElement = element.element("action");
        if (actionElement != null) {
            Action action = getActionLoaderSupport().loadDeclarativeAction(actionElement);
            actionItem = (ActionItem) resultComponent.addItem(action);
            actionItem.setId(id);
        } else if (ref != null) {
            getComponentContext().addInitTask(
                    new AssignDropdownButtonActionInitTask(resultComponent, ref, id, getComponentContext().getView())
            );
        }
    }

    protected void loadComponentItem(Element element) {
        String id = getLoaderSupport().loadString(element, "id")
                .orElse(null);
        Component content = null;

        Element subElement = element.elements().stream()
                .findFirst()
                .orElse(null);

        if (subElement != null) {
            LayoutLoader loader = getLayoutLoader();
            ComponentLoader<?> componentLoader = loader.createComponentLoader(subElement);
            componentLoader.initComponent();
            componentLoader.loadComponent();

            content = componentLoader.getResultComponent();
        }

        ComponentItem componentItem = (ComponentItem) resultComponent.addItem(content);
        componentItem.setId(id);
    }

    protected void loadTextItem(Element element) {
        String id = getLoaderSupport().loadString(element, "id")
                .orElse(null);
        String text = getLoaderSupport().loadResourceString(element, "text", context.getMessageGroup())
                .orElse(null);

        TextItem textItem = (TextItem) resultComponent.addItem(text);
        textItem.setId(id);
    }

    protected void loadSeparator() {
        resultComponent.addSeparator();
    }

    protected ActionLoaderSupport getActionLoaderSupport() {
        if (actionLoaderSupport == null) {
            actionLoaderSupport = applicationContext.getBean(ActionLoaderSupport.class, context);
        }
        return actionLoaderSupport;
    }
}
