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

import javax.annotation.Nullable;

public class ComponentItem extends BaseDropdownButtonItem {

    protected Component content;

    public ComponentItem() {
    }

    public ComponentItem(@Nullable Component content) {
        this.content = content;
    }

    public void setContent(@Nullable Component content) {
        this.content = content;
    }

    @Nullable
    public Component getContent() {
        return content;
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
