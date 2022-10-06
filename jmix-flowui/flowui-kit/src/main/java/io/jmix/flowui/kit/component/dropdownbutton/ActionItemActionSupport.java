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

import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;
import io.jmix.flowui.kit.component.KeyCombination;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.beans.PropertyChangeEvent;
import java.util.Objects;

public class ActionItemActionSupport {

    protected final DropdownButton.ActionItemImpl actionItem;

    protected Action action;
    protected MenuItem item;

    protected Registration itemClickRegistration;
    protected Registration actionPropertyChangeRegistration;

    public ActionItemActionSupport(DropdownButton.ActionItemImpl actionItem, MenuItem item) {
        this.actionItem = actionItem;
        this.item = item;
    }

    public void setAction(@Nullable Action action, boolean overrideComponentProperties) {
        if (Objects.equals(this.action, action)) {
            return;
        }

        removeRegistrations();

        this.action = action;

        if (action != null) {
            updateEnabled();
            updateVisible();
            updateText(overrideComponentProperties);
            updateIcon(overrideComponentProperties);
            updateShortcutCombination(overrideComponentProperties);

            itemClickRegistration = item.addClickListener(this::onButtonClick);
            actionPropertyChangeRegistration = action.addPropertyChangeListener(this::onActionPropertyChange);
        }
    }

    @Nullable
    public Action getAction() {
        return action;
    }

    protected void removeRegistrations() {
        if (this.action != null) {
            if (itemClickRegistration != null) {
                itemClickRegistration.remove();
                itemClickRegistration = null;
            }

            if (actionPropertyChangeRegistration != null) {
                actionPropertyChangeRegistration.remove();
                actionPropertyChangeRegistration = null;
            }
        }
    }

    protected void updateText(boolean overrideComponentProperties) {
        if (StringUtils.isEmpty(item.getText()) || overrideComponentProperties) {
            actionItem.setText(action.getText());
        }
    }

    protected void updateEnabled() {
        actionItem.setEnabled(action.isEnabled());
    }

    protected void updateVisible() {
        actionItem.setVisible(action.isVisible());
    }

    protected void updateIcon(boolean overrideComponentProperties) {
        if (actionItem.getIcon() == null || overrideComponentProperties) {
            actionItem.setIcon(action.getIcon());
        }
    }

    protected void updateShortcutCombination(boolean overrideComponentProperties) {
        if (overrideComponentProperties) {
            actionItem.setShortcutCombination(action.getShortcutCombination());
        }
    }

    protected void onButtonClick(ClickEvent<MenuItem> event) {
        this.action.actionPerform(event.getSource());
    }

    protected void onActionPropertyChange(PropertyChangeEvent event) {
        switch (event.getPropertyName()) {
            case Action.PROP_TEXT:
                actionItem.setText((String) event.getNewValue());
                break;
            case Action.PROP_ENABLED:
                actionItem.setEnabled((Boolean) event.getNewValue());
                break;
            case Action.PROP_VISIBLE:
                actionItem.setVisible((Boolean) event.getNewValue());
                break;
            case Action.PROP_ICON:
                actionItem.setIcon((Icon) event.getNewValue());
                break;
            case Action.PROP_SHORTCUT_COMBINATION:
                actionItem.setShortcutCombination((KeyCombination) event.getNewValue());
                break;
        }
    }
}
