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

import { css, registerStyles } from '@vaadin/vaadin-themable-mixin/vaadin-themable-mixin.js';

const loginForm = css`
  .additional-fields-container {
    display: flex;
    gap: var(--lumo-space-s);
    justify-content: end;
    padding-top: var(--lumo-space-m);
  }
  .remember-me {
    align-self: center;
  }
  .locale-select {
    width: calc(var(--lumo-size-m) * 3.5);
  }
`;

registerStyles('jmix-login-form', [loginForm], {
    moduleId: 'lumo-jmix-login-form-styles'
});