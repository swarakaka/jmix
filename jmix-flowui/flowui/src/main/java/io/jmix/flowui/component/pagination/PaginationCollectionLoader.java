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

import io.jmix.core.DataManager;
import io.jmix.core.LoadContext;
import io.jmix.core.ValueLoadContext;
import io.jmix.core.common.util.Preconditions;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.flowui.model.*;
import io.jmix.flowui.model.CollectionContainer.CollectionChangeEvent;
import io.jmix.flowui.model.impl.WeakCollectionChangeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Consumer;

@Component("flowui_PaginationDataProviderImpl")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class PaginationCollectionLoader implements PaginationLoader {
    private static final Logger log = LoggerFactory.getLogger(PaginationCollectionLoader.class);

    protected DataManager dataManager;

    protected CollectionContainer<?> container;
    protected BaseCollectionLoader loader;

    protected Consumer<CollectionChangeType> refreshListener;
    protected WeakCollectionChangeListener<?> weakContainerCollectionChangeListener;

    public PaginationCollectionLoader(BaseCollectionLoader loader) {
        Preconditions.checkNotNullArgument(loader);
        Preconditions.checkNotNullArgument(loader.getContainer(),
                "Pagination data provider does not work without loader that is bound with container");

        this.loader = loader;
        this.container = loader.getContainer();

        attachCollectionChangeListener();
    }

    protected void attachCollectionChangeListener() {
        Consumer<CollectionChangeEvent<?>> containerCollectionChangeListener = e -> {
            if (refreshListener != null) {
                refreshListener.accept(e.getChangeType());
            }
        };

        weakContainerCollectionChangeListener =
                new WeakCollectionChangeListener<>(container, (Consumer) containerCollectionChangeListener);
    }

    @Autowired
    public void setDataManager(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    @Override
    public int getFirstResult() {
        return loader.getFirstResult();
    }

    @Override
    public void setFirstResult(int startPosition) {
        loader.setFirstResult(startPosition);
    }

    @Override
    public int getMaxResults() {
        return loader.getMaxResults();
    }

    @Override
    public void setMaxResults(int maxResults) {
        loader.setMaxResults(maxResults);
    }

    @Override
    public int getCount() {
        if (loader instanceof CollectionLoader) {
            LoadContext context = ((CollectionLoader) loader).createLoadContext();
            return (int) dataManager.getCount(context);
        } else if (loader instanceof KeyValueCollectionLoader) {
            ValueLoadContext context = ((KeyValueCollectionLoader) loader).createLoadContext();
            return (int) dataManager.getCount(context);
        } else {
            log.warn("Unsupported loader type: {}", loader.getClass().getName());
            return 0;
        }
    }

    @Override
    public int size() {
        return container.getItems().size();
    }

    @Override
    public void refresh() {
        loader.load();
    }

    @Override
    public void removeCollectionChangeListener() {
        weakContainerCollectionChangeListener.removeItself();
    }

    @Override
    public void setCollectionChangeListener(Consumer<CollectionChangeType> listener) {
        this.refreshListener = listener;
    }

    @Override
    public MetaClass getEntityMetaClass() {
        return container.getEntityMetaClass();
    }
}
