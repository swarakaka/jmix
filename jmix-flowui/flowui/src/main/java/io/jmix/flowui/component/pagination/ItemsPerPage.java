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
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.FlowuiComponentProperties;
import io.jmix.flowui.FlowuiProperties;
import io.jmix.flowui.kit.component.pagination.JmixItemsPerPage;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemsPerPage extends JmixItemsPerPage implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected Messages messages;
    protected FlowuiComponentProperties componentProperties;
    protected FlowuiProperties flowuiProperties;

    protected PaginationLoader loader;

    protected List<Integer> processedItems;

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
        messages = applicationContext.getBean(Messages.class);
        componentProperties = applicationContext.getBean(FlowuiComponentProperties.class);
        flowuiProperties = applicationContext.getBean(FlowuiProperties.class);
    }

    protected void initComponent() {
        getContent(); // init content component
        setLabelText(messages.getMessage("pagination.itemsPerPage.label.text"));
    }

    public void setPaginationLoader(@Nullable PaginationLoader loader) {
        this.loader = loader;

        if (loader != null) {
            initItemsPerPageOptions();
            initMaxResultValue();
        }
    }

    protected void initItemsPerPageOptions() {
        Preconditions.checkNotNullArgument(loader); // todo rp

        if (CollectionUtils.isNotEmpty(itemsPerPageItems)) {
            processedItems = processOptions(itemsPerPageItems, loader.getEntityMetaClass());
        } else {
            processedItems = processOptions(componentProperties.getPaginationItemsPerPageItems(),
                    loader.getEntityMetaClass());
        }

        itemsPerPageSelect.setItems(processedItems);
    }

    /**
     * Setup MaxResult value to data binder and to items per page ComboBox if it's visible.
     */
    protected void initMaxResultValue() {
        Preconditions.checkNotNullArgument(loader); // todo rp

        Integer optionValue = getDefaultOptionValue(processedItems, loader.getEntityMetaClass());

        if (isItemsPerPageVisible()) {
            itemsPerPageSelect.setValue(optionValue);
        }
        loader.setMaxResults(optionValue);
    }

    /**
     * Sorts options. Options less than or equal 0 are ignored. Values greater than MaxFetchSize
     * are replaced by MaxFetchSize.
     *
     * @param options   items per page options
     * @param metaClass entity's MetaClass
     * @return sorted options
     */
    protected List<Integer> processOptions(List<Integer> options, MetaClass metaClass) {
        int maxFetch = getEntityMaxFetchSize(metaClass);

        List<Integer> result = new ArrayList<>();
        for (Integer option : options) {
            if (option > maxFetch) {
                option = maxFetch;
            }
            if (result.contains(option) || option <= 0) {
                continue;
            }
            result.add(option);
        }

        Collections.sort(result);

        return result;
    }

    protected int getEntityMaxFetchSize(MetaClass metaClass) {
        return flowuiProperties.getEntityMaxFetchSize(metaClass.getName());
    }

    protected int getEntityPageSize(MetaClass metaClass) {
        return flowuiProperties.getEntityPageSize(metaClass.getName());
    }

    protected Integer getDefaultOptionValue(List<Integer> options, MetaClass metaClass) {
        int defaultValue = itemsPerPageDefaultValue != null
                ? itemsPerPageDefaultValue
                : getEntityPageSize(metaClass);

        boolean shouldFindInOptions = isItemsPerPageVisible()
                || CollectionUtils.isNotEmpty(itemsPerPageItems); // options are explicitly set
        return shouldFindInOptions
                ? findClosestValue(defaultValue, options)
                : defaultValue;
    }

    protected int findClosestValue(int maxResults, List<Integer> optionsList) {
        int minimumValue = Integer.MAX_VALUE;
        int closest = maxResults;

        for (int option : optionsList) {
            int diff = Math.abs(option - maxResults);
            if (diff < minimumValue) {
                minimumValue = diff;
                closest = option;
            }
        }
        return closest;
    }

    protected boolean isItemsPerPageVisible() {
        return getParent().isPresent();
    }
}
