/*
 * Copyright 2020 Haulmont.
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

package io.jmix.dynattr.impl;

import io.jmix.core.*;
import io.jmix.core.common.util.ReflectionHelper;
import io.jmix.core.metamodel.datatype.Datatype;
import io.jmix.core.metamodel.datatype.DatatypeRegistry;
import io.jmix.core.metamodel.model.MetaClass;
import io.jmix.core.metamodel.model.MetaProperty;
import io.jmix.data.PersistenceHints;
import io.jmix.data.StoreAwareLocator;
import io.jmix.dynattr.*;
import io.jmix.dynattr.model.Category;
import io.jmix.dynattr.model.CategoryAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import java.io.Serializable;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component(DynAttrMetadata.NAME)
public class DynAttrMetadataImpl implements DynAttrMetadata {

    @Autowired
    protected StoreAwareLocator storeAwareLocator;
    @Autowired
    protected ExtendedEntities extendedEntities;
    @Autowired
    protected Metadata metadata;
    @Autowired
    protected DatatypeRegistry datatypeRegistry;
    @Autowired
    protected FetchPlans fetchPlans;
    @Autowired
    protected CacheManager cacheManager;
    @Autowired
    protected CacheOperations cacheOperations;

    protected Cache cache;
    protected String dynamicAttributesStore = Stores.MAIN;

    @PostConstruct
    protected void init() {
        cache = cacheManager.getCache(DYN_ATTR_CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException(String.format("Unable to find cache: %s", DYN_ATTR_CACHE_NAME));
        }
    }

    @Override
    public Collection<AttributeDefinition> getAttributes(MetaClass metaClass) {
        String key = extendedEntities.getOriginalOrThisMetaClass(metaClass).getName();
        CacheItem value = cache.get(key, () -> loadCacheItem(key));
        return value == null ? Collections.emptyList() : Collections.unmodifiableCollection(value.getAttributes());
    }

    @Override
    public Optional<AttributeDefinition> getAttributeByCode(MetaClass metaClass, String code) {
        String key = extendedEntities.getOriginalOrThisMetaClass(metaClass).getName();
        CacheItem value = cache.get(key, () -> loadCacheItem(key));
        return value == null ? Optional.empty() : value.getAttributeByCode(code);
    }

    @Override
    public Collection<CategoryDefinition> getCategories(MetaClass metaClass) {
        String key = extendedEntities.getOriginalOrThisMetaClass(metaClass).getName();
        CacheItem value = cache.get(key, () -> loadCacheItem(key));
        return value == null ? Collections.emptyList() : value.getCategories();
    }

    @Override
    public void reload() {
        cache.invalidate();
    }

    protected CacheItem loadCacheItem(String entityName) {
        List<CategoryDefinition> categories = loadCategoryDefinitions(entityName);
        Map<String, AttributeDefinition> attributes = categories.stream()
                .flatMap(category -> category.getAttributeDefinitions().stream())
                .collect(Collectors.toMap(AttributeDefinition::getCode, Function.identity()));

        return new CacheItem(categories, attributes);
    }

    protected List<CategoryDefinition> loadCategoryDefinitions(String entityName) {
        //noinspection ConstantConditions
        return storeAwareLocator.getTransactionTemplate(dynamicAttributesStore)
                .execute(transactionStatus -> {
                    EntityManager entityManager = storeAwareLocator.getEntityManager(dynamicAttributesStore);

                    FetchPlan fetchPlan = fetchPlans.builder(Category.class)
                            .addFetchPlan(FetchPlan.LOCAL)
                            .add("categoryAttrs", builder -> {
                                builder.addFetchPlan(FetchPlan.LOCAL);
                                builder.add("category", FetchPlan.LOCAL);
                                builder.add("defaultEntity", FetchPlan.LOCAL);
                            })
                            .build();

                    return entityManager.createQuery("select c from sys_Category c where c.entityType = :entityType", Category.class)
                            .setParameter("entityType", entityName)
                            .setHint(PersistenceHints.FETCH_PLAN, fetchPlan)
                            .getResultList().stream()
                            .map(this::buildCategoryDefinition)
                            .collect(Collectors.toList());
                });
    }

    protected CategoryDefinition buildCategoryDefinition(Category category) {
        MetaClass metaClass = extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(category.getEntityType()));
        List<AttributeDefinition> attributes;
        if (category.getCategoryAttrs() != null) {
            attributes = Collections.unmodifiableList(category.getCategoryAttrs().stream()
                    .map(attr -> new CommonAttributeDefinition(attr, buildMetaProperty(attr, metaClass)))
                    .collect(Collectors.toList()));
        } else {
            attributes = Collections.emptyList();
        }
        return new CommonCategoryDefinition(category, attributes);
    }

    protected MetaProperty buildMetaProperty(CategoryAttribute categoryAttribute, MetaClass metaClass) {
        String name = DynAttrUtils.getPropertyFromAttributeCode(categoryAttribute.getCode());

        Class<?> javaClass;
        Datatype<?> datatype = null;
        MetaClass propertyMetaClass = null;
        if (categoryAttribute.getDataType() == AttributeType.ENTITY) {
            javaClass = ReflectionHelper.getClass(categoryAttribute.getEntityClass());
            propertyMetaClass = extendedEntities.getOriginalOrThisMetaClass(metadata.getClass(javaClass));
        } else {
            javaClass = DynAttrUtils.getDatatypeClass(categoryAttribute.getDataType());
            datatype = datatypeRegistry.get(javaClass);
        }
        return new DynAttrMetaProperty(name, metaClass, javaClass, propertyMetaClass, datatype);
    }

    protected static class CacheItem implements Serializable {
        protected final Collection<CategoryDefinition> categories;
        protected final Map<String, AttributeDefinition> attributes;

        public CacheItem(Collection<CategoryDefinition> categories, Map<String, AttributeDefinition> attributes) {
            this.categories = categories;
            this.attributes = attributes;
        }

        public Collection<AttributeDefinition> getAttributes() {
            return attributes.values();
        }

        public Optional<AttributeDefinition> getAttributeByCode(String code) {
            return Optional.ofNullable(attributes.get(code));
        }

        public Collection<CategoryDefinition> getCategories() {
            return categories;
        }
    }
}
