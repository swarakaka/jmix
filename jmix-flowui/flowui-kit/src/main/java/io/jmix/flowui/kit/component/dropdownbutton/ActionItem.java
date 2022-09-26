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
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.Key;
import com.vaadin.flow.component.KeyModifier;
import com.vaadin.flow.component.ShortcutRegistration;
import com.vaadin.flow.component.contextmenu.MenuItem;
import com.vaadin.flow.shared.Registration;
import io.jmix.flowui.kit.action.Action;

import javax.annotation.Nullable;

public class ActionItem extends BaseDropdownButtonItem {

    protected Action action;
    protected Component iconComponent;

    public ActionItem() {
    }

    public ActionItem(@Nullable Action action) {
        this.action = action;
    }

    @Nullable
    public Action getAction() {
        return action;
    }

    public void setText(@Nullable String text) {
        if (iconComponent != null) {
            item.setText(text);
            setIcon(iconComponent);
        }
    }

    public String getTest() {
        return item.getText();
    }

    public void setIcon(@Nullable Component icon) {
        if (icon != null && icon.getElement().isTextNode()) {
            throw new IllegalArgumentException(
                    "Text node can't be used as an icon.");
        }
        if (iconComponent != null) {
            item.remove(iconComponent);
        }
        iconComponent = icon;
        if (icon != null) {
            item.add(iconComponent);
        }
        updateThemeAttribute();
    }

    @Nullable
    public Component getIcon() {
        return iconComponent;
    }

    private void updateThemeAttribute() {
        if (iconComponent != null) {
            item.addThemeNames("icon");
        } else {
            item.removeThemeNames("icon");
        }
    }

    @Override
    public Registration addClickListener(ComponentEventListener<ClickEvent<MenuItem>> listener) {
        throw new UnsupportedOperationException(String.format(
                "Unable to add a ClickListener to actionItem '%s'", this.id != null ? this.id : "null"));
    }

    @Override
    public ShortcutRegistration addClickShortcut(Key key, KeyModifier... keyModifiers) {
        throw new UnsupportedOperationException(String.format(
                "Unable to add a ClickShortcut to actionItem '%s'", this.id != null ? this.id : "null"));
    }
}
