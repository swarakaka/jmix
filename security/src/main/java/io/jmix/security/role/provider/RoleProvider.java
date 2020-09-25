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

package io.jmix.security.role.provider;

import io.jmix.security.model.Role;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Interface must be implemented by classes that provide roles from a particular source type. A source type may be a
 * database, annotated interfaces, etc.
 * <p>
 * Role providers are used by {@link io.jmix.security.role.RoleRepository}
 */
public interface RoleProvider {

    Collection<Role> getAllRoles();

    @Nullable
    Role getRoleByCode(String code);

    boolean deleteRole(Role role);
}
