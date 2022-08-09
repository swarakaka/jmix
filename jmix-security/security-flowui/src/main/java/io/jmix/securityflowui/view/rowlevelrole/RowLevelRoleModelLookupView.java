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

package io.jmix.securityflowui.view.rowlevelrole;

import com.vaadin.flow.router.Route;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.view.*;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.securityflowui.model.RoleModelConverter;
import io.jmix.securityflowui.model.RowLevelRoleModel;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Route(value = "rowlevelrolemodelslookup", layout = DefaultMainViewParent.class)
@UiController("sec_RowLevelRoleModel.lookup")
@UiDescriptor("row-level-role-model-lookup-view.xml")
@LookupComponent("roleModelsTable")
@DialogMode(width = "50em", height = "37.5em")
public class RowLevelRoleModelLookupView extends StandardListView<RowLevelRoleModel> {

    @ComponentId
    private CollectionContainer<RowLevelRoleModel> roleModelsDc;

    @Autowired
    private RowLevelRoleRepository roleRepository;

    @Autowired
    private RoleModelConverter roleModelConverter;

    @Subscribe
    public void onBeforeShow(BeforeShowEvent event) {
        loadRoles();
    }

    private void loadRoles() {
        List<RowLevelRoleModel> roleModels = roleRepository.getAllRoles().stream()
                .map(roleModelConverter::createRowLevelRoleModel)
                .sorted(Comparator.comparing(RowLevelRoleModel::getName))
                .collect(Collectors.toList());
        roleModelsDc.setItems(roleModels);
    }
}