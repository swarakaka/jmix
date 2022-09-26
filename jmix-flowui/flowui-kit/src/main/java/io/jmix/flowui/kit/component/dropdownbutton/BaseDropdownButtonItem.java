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

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.contextmenu.MenuItem;

import javax.annotation.Nullable;

public class BaseDropdownButtonItem implements DropdownButtonItem {

    protected Component parent;
    protected MenuItem item;
    protected String id;

    public BaseDropdownButtonItem() {
    }

    @Override
    public void setParent(Component parent) {
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
    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Override
    @Nullable
    public String getId() {
        return id;
    }

    public void setVisible(boolean visible) {
        if (item != null) {
            item.setVisible(visible);
        }
    }

    public void setEnabled(boolean enabled) {
        if (item != null) {
            item.setEnabled(enabled);
        }
    }

    public boolean isVisible() {
        return item != null && item.isVisible();
    }

    public boolean isEnabled() {
        return item != null && item.isEnabled();
    }
}
