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
import io.jmix.core.DataManager;
import io.jmix.core.Messages;
import io.jmix.flowui.Notifications;
import io.jmix.flowui.component.grid.DataGrid;
import io.jmix.flowui.kit.action.ActionPerformedEvent;
import io.jmix.flowui.model.CollectionContainer;
import io.jmix.flowui.util.RemoveOperation;
import io.jmix.flowui.view.*;
import io.jmix.security.model.RoleSource;
import io.jmix.security.role.RowLevelRoleRepository;
import io.jmix.securitydata.entity.RoleAssignmentEntity;
import io.jmix.securityflowui.model.BaseRoleModel;
import io.jmix.securityflowui.model.RoleModelConverter;
import io.jmix.securityflowui.model.RowLevelRoleModel;
import io.jmix.securityflowui.util.RemoveRoleConsumer;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "rowlevelrolemodels", layout = DefaultMainViewParent.class)
@UiController("sec_RowLevelRoleModel.list")
@UiDescriptor("row-level-role-model-list-view.xml")
@LookupComponent("roleModelsTable")
@DialogMode(width = "50em", height = "37.5em")
public class RowLevelRoleModelListView extends StandardListView<RowLevelRoleModel> {

    @ComponentId
    private DataGrid<RowLevelRoleModel> roleModelsTable;

    @ComponentId
    private CollectionContainer<RowLevelRoleModel> roleModelsDc;

    @Autowired
    private RowLevelRoleRepository roleRepository;
    @Autowired
    private RoleModelConverter roleModelConverter;
    @Autowired
    private RemoveOperation removeOperation;
    @Autowired
    private DataManager dataManager;
    @Autowired
    private Notifications notifications;
    @Autowired
    private Messages messages;

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

    @Subscribe("roleModelsTable.remove")
    public void onRoleModelsTableRemove(ActionPerformedEvent event) {
        removeOperation.builder(roleModelsTable)
                .withConfirmation(true)
                .beforeActionPerformed(new RemoveRoleConsumer<>(roleRepository, notifications, messages))
                .afterActionPerformed((afterActionConsumer) -> {
                    List<RoleAssignmentEntity> roleAssignmentEntities = dataManager.load(RoleAssignmentEntity.class)
                            .query("e.roleCode IN :codes")
                            .parameter("codes", afterActionConsumer.getItems().stream()
                                    .map(BaseRoleModel::getCode)
                                    .collect(Collectors.toList()))
                            .list();
                    dataManager.remove(roleAssignmentEntities);
                })
                .remove();
    }

    @Install(to = "roleModelsTable.remove", subject = "enabledRule")
    private boolean roleModelsTableRemoveEnabledRule() {
        return isDatabaseRoleSelected();
    }

    private boolean isDatabaseRoleSelected() {
        Set<RowLevelRoleModel> selected = roleModelsTable.getSelectedItems();
        if (selected.size() == 1) {
            RowLevelRoleModel roleModel = selected.iterator().next();
            return RoleSource.DATABASE.equals(roleModel.getSource());
        }

        return false;
    }
}