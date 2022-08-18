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
import io.jmix.flowui.view.*;
import io.jmix.securityflowui.model.ResourceRoleModel;
import io.jmix.securityflowui.model.RowLevelRoleModel;

@Route(value = "rowlevelrolemodels/:code", layout = DefaultMainViewParent.class)
@UiController("sec_RowLevelRoleModel.detail")
@UiDescriptor("row-level-role-model-detail-view.xml")
@EditedEntityContainer("roleModelDc")
public class RowLevelRoleModelDetailView extends StandardDetailView<RowLevelRoleModel> {
}
