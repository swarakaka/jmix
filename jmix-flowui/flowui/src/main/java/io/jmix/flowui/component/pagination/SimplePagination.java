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

package io.jmix.flowui.component.pagination;

import io.jmix.core.Messages;
import io.jmix.core.common.util.Preconditions;
import io.jmix.flowui.UiComponents;
import io.jmix.flowui.kit.component.pagination.JmixSimplePagination;
import io.jmix.flowui.model.CollectionChangeType;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

public class SimplePagination extends JmixSimplePagination implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected Messages messages;
    protected UiComponents uiComponents;

    protected ItemsPerPage itemsPerPage;
    protected PaginationLoader loader;

    protected boolean samePage; // todo rp ?

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    protected void autowireDependencies() {
        uiComponents = applicationContext.getBean(UiComponents.class);
        messages = applicationContext.getBean(Messages.class);
    }

    protected void initComponent() {
        itemsPerPage = createItemsPerPage();
        initItemsPerPage(itemsPerPage);
    }

    @Nullable
    public Integer getItemsPerPageDefaultValue() {
        return itemsPerPage.getItemsPerPageDefaultValue();
    }

    public void setItemsPerPageDefaultValue(@Nullable Integer defaultValue) {
        itemsPerPage.setItemsPerPageDefaultValue(defaultValue);
    }

    /**
     *
     * @return
     */
    public Collection<Integer> getItemsPerPageItems() {
        return itemsPerPage.getItemsPerPageItems();
    }

    /**
     *
     * @param itemsPerPageItems
     */
    public void setItemsPerPageItems(List<Integer> itemsPerPageItems) {
        itemsPerPage.setItemsPerPageItems(itemsPerPageItems);
    }

    /**
     *
     * @return
     */
    public boolean getItemsPerPageUnlimitedOptionVisible() {
        return itemsPerPage.getItemsPerPageUnlimitedOptionVisible();
    }

    /**
     *
     * @param unlimitedOptionVisible
     */
    public void setItemsPerPageUnlimitedOptionVisible(boolean unlimitedOptionVisible) {
        itemsPerPage.setItemsPerPageUnlimitedOptionVisible(unlimitedOptionVisible);
    }

    /**
     *
     * @return
     */
    public boolean isItemsPerPageVisible() {
        return getJmixRowsPerPage() != null;
    }

    /**
     *
     * @param itemsPerPageVisible
     */
    public void setItemsPerPageVisible(boolean itemsPerPageVisible) {
        if (isItemsPerPageVisible() != itemsPerPageVisible) {
            setItemsPerPage(itemsPerPageVisible ? itemsPerPage : null);
        }
    }

    /**
     *
     * @param loader
     */
    public void setPaginationLoader(PaginationLoader loader) {
        Preconditions.checkNotNullArgument(loader);

        if (this.loader != null) {
            this.loader.removeCollectionChangeListener();
        }
        this.loader = loader;

        loader.setCollectionChangeListener(this::onRefreshItems);

        removeListeners();

        initItemsPerPage(itemsPerPage);

        initListeners();

        // todo rp
        /*if (dataBinderContainsItems()) {
            // if items has already loaded we should reload them
            // with maxResult from ComboBox or update shown items
            if (isItemsPerPageVisible()) {
                dataBinder.refresh();
            } else {
                onCollectionChanged();
            }
        }*/
    }

    protected void onRefreshItems(CollectionChangeType changeType) {
        samePage = CollectionChangeType.REFRESH != changeType;
        onCollectionChanged();
    }

    protected void onCollectionChanged() {
        // todo rp
    }

    protected void removeListeners() {
        // todo rp
    }

    protected void initListeners() {
        removeListeners();
        // todo rp
    }

    protected ItemsPerPage createItemsPerPage() {
        return uiComponents.create(ItemsPerPage.class);
    }

    protected void initItemsPerPage(ItemsPerPage itemsPerPage) {
        itemsPerPage.setPaginationLoader(loader);
    }
}
