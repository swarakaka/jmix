/*
 * Copyright 2019 Haulmont.
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
package com.haulmont.cuba.core.global;

import io.jmix.core.*;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO that contains information about currently committed entities.
 * <p>
 * Used by {@link DataManager}.
 */
@SuppressWarnings("unchecked")
public class CommitContext extends SaveContext {

    private static final long serialVersionUID = -3092858740670863815L;

    protected ValidationMode validationMode = ValidationMode.DEFAULT;
    protected List<Class> validationGroups;

    protected CommitContext() {
        joinTransaction = false;
    }

    /**
     * @param commitInstances changed entities to be committed to the database
     */
    public CommitContext(JmixEntity... commitInstances) {
        this();
        saving(Arrays.asList(commitInstances));
    }

    /**
     * @param commitInstances collection of changed entities to be committed to the database
     */
    public CommitContext(Collection commitInstances) {
        this();
        saving(commitInstances);
    }

    /**
     * @param commitInstances collection of changed entities to be committed to the database
     * @param removeInstances collection of entities to be removed from the database
     */
    public CommitContext(Collection commitInstances, Collection removeInstances) {
        this();
        saving(commitInstances);
        removing(removeInstances);
    }

    /**
     * Adds an entity to be committed to the database.
     *
     * @param entity entity instance
     * @return this instance for chaining
     */
    public CommitContext addInstanceToCommit(JmixEntity entity) {
        saving(entity);
        return this;
    }

    /**
     * Adds an entity to be committed to the database.
     *
     * @param entity    entity instance
     * @param fetchPlan fetch plan which is used in merge operation to ensure all required attributes are loaded in the returned instance
     * @return this instance for chaining
     */
    public CommitContext addInstanceToCommit(JmixEntity entity, @Nullable FetchPlan fetchPlan) {
        saving(entity, fetchPlan);
        return this;
    }

    /**
     * Adds an entity to be committed to the database.
     *
     * @param entity        entity instance
     * @param fetchPlanName view which is used in merge operation to ensure all required attributes are loaded in the returned instance
     * @return this instance for chaining
     */
    public CommitContext addInstanceToCommit(JmixEntity entity, @Nullable String fetchPlanName) {
        saving(entity, getFetchPlanFromRepository(entity, fetchPlanName));
        return this;
    }

    /**
     * Adds an entity to be removed from the database.
     *
     * @param entity entity instance
     * @return this instance for chaining
     */
    public CommitContext addInstanceToRemove(JmixEntity entity) {
        removing(entity);
        return this;
    }

    /**
     * @return direct reference to collection of changed entities that will be committed to the database.
     * The collection is modifiable.
     */
    public Collection<JmixEntity> getCommitInstances() {
        return castCollection(getEntitiesToSave());
    }

    /**
     * @param commitInstances collection of changed entities that will be committed to the database
     */
    public void setCommitInstances(Collection commitInstances) {
        this.entitiesToSave = commitInstances;
    }

    /**
     * @return direct reference to collection of entities that will be removed from the database.
     * The collection is modifiable.
     */
    public Collection<JmixEntity> getRemoveInstances() {
        return castCollection(getEntitiesToRemove());
    }

    /**
     * @param removeInstances collection of entities to be removed from the database
     */
    public void setRemoveInstances(Collection removeInstances) {
        this.entitiesToRemove = removeInstances;
    }

    /**
     * @return true if calling code does not need committed instances, which allows for performance optimization
     */
    public boolean isDiscardCommitted() {
        return isDiscardSaved();
    }

    /**
     * Set to true if calling code does not need committed instances, which allows for performance optimization.
     */
    public void setDiscardCommitted(boolean discardCommitted) {
        setDiscardSaved(discardCommitted);
    }

    /**
     * @return {@link ValidationMode} of commit context.
     */
    public ValidationMode getValidationMode() {
        return validationMode;
    }

    /**
     * Sets {@link ValidationMode} for commit context.
     * Validation type is responsible for whether entity bean validation will be applied on {@link DataManager} level.
     *
     * @param validationMode validation type
     * @see BeanValidation
     */
    public void setValidationMode(ValidationMode validationMode) {
        this.validationMode = validationMode;
    }

    @Nullable
    private FetchPlan getFetchPlanFromRepository(JmixEntity entity, @Nullable String fetchPlanName) {
        if (fetchPlanName == null)
            return null;
        Metadata metadata = AppBeans.get(Metadata.NAME);
        FetchPlanRepository viewRepository = AppBeans.get(FetchPlanRepository.NAME);
        return viewRepository.getFetchPlan(metadata.findClass(entity.getClass()), fetchPlanName);
    }

    /**
     * @return groups targeted for validation.
     * @see javax.validation.Validator#validate(Object, Class[])
     */
    public List<Class> getValidationGroups() {
        return validationGroups;
    }

    /**
     * Sets groups targeted for validation.
     *
     * @param validationGroups {@code Set} of groups
     * @see javax.validation.Validator#validate(Object, Class[])
     */
    public void setValidationGroups(List<Class> validationGroups) {
        this.validationGroups = validationGroups;
    }

    /**
     * Validation mode. Affects entity bean validation on {@link DataManager} level.
     */
    public enum ValidationMode {
        /**
         * Use value from {@code cuba.dataManagerBeanValidation} application property.
         */
        DEFAULT,
        /**
         * Always perform validation.
         */
        ALWAYS_VALIDATE,
        /**
         * Do not validate.
         */
        NEVER_VALIDATE
    }

    private Collection<JmixEntity> castCollection(Collection<Object> collection) {
        return collection == null ? null :
                collection.stream()
                        .map(o -> (JmixEntity) o)
                        .collect(Collectors.toList());
    }
}
