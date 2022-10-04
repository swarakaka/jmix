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

import '@vaadin/text-field/src/vaadin-text-field.js';
import '@vaadin/password-field/src/vaadin-password-field.js';
import '@vaadin/checkbox/src/vaadin-checkbox.js';
import '@vaadin/combo-box/src/vaadin-combo-box.js';
import '@vaadin/login/src/vaadin-login-form-wrapper.js';

import {html} from '@polymer/polymer/polymer-element.js';
import {LoginForm} from '@vaadin/vaadin-login/src/vaadin-login-form.js';

class JmixLoginForm extends LoginForm {
    static get template() {
        return html`
            <style>
                [part='vaadin-login-native-form'] * {
                    width: 100%;
                }
            </style>
            <vaadin-login-form-wrapper
                    theme$="[[_theme]]"
                    part="vaadin-login-native-form-wrapper"
                    action="{{action}}"
                    disabled="{{disabled}}"
                    error="{{error}}"
                    no-forgot-password="{{noForgotPassword}}"
                    i18n="{{i18n}}"
                    on-login="_retargetEvent"
                    on-forgot-password="_retargetEvent"
            >
                <form part="vaadin-login-native-form" method="POST" action$="[[action]]" slot="form">
                    <input id="csrf" type="hidden"/>
                    <vaadin-text-field
                            name="username"
                            label="[[i18n.form.username]]"
                            id="vaadinLoginUsername"
                            required
                            on-keydown="_handleInputKeydown"
                            autocapitalize="none"
                            autocorrect="off"
                            spellcheck="false"
                            value="[[username]]"
                    >
                        <input type="text" slot="input" on-keyup="_handleInputKeyup"/>
                    </vaadin-text-field>

                    <vaadin-password-field
                            name="password"
                            label="[[i18n.form.password]]"
                            id="vaadinLoginPassword"
                            required
                            on-keydown="_handleInputKeydown"
                            spellcheck="false"
                            autocomplete="current-password"
                            value="[[password]]"
                    >
                        <input type="password" slot="input" on-keyup="_handleInputKeyup"/>
                    </vaadin-password-field>

                    <div id="additionalFields" class="additional-fields-container">
                        <vaadin-checkbox id="rememberMeCheckbox"
                                         label="[[i18n.form.rememberMe]]"
                                         class="remember-me"></vaadin-checkbox>
                        <vaadin-combo-box id="localeSelectComboBox"
                                          class="locale-select">
                        </vaadin-combo-box>
                    </div>

                    <vaadin-button part="vaadin-login-submit" theme="primary contained" on-click="submit"
                                   disabled$="[[disabled]]"
                    >[[i18n.form.submit]]
                    </vaadin-button
                    >
                </form>
                </jmix-login-form-wrapper>
        `;
    }

    static get is() {
        return 'jmix-login-form';
    }

    static get properties() {
        return {
            username: {
                type: String,
                value: null,
                notify: true
            },
            password: {
                type: String,
                value: null,
                notify: true
            },
            rememberMeVisibility: {
                type: Boolean,
                value: true
            },
            localesVisibility: {
                type: Boolean,
                value: true,
            },
            locales: {
                type: Object,
                value: []
            },
            /* CAUTION! Copied from LoginMixin */
            i18n: {
                type: Object,
                value: function () {
                    return {
                        form: {
                            title: 'Log in',
                            username: 'Username',
                            password: 'Password',
                            submit: 'Log in',
                            forgotPassword: 'Forgot password',
                            rememberMe: "Remember me"
                        },
                        errorMessage: {
                            title: 'Incorrect username or password',
                            message: 'Check that you have entered the correct username and password and try again.'
                        }
                    };
                },
                notify: true
            },
        }
    }

    static get observers() {
        return [
            '_onVisibilityPropertiesChanged(rememberMeVisibility, localesVisibility)',
            `_onLocalesPropertyChanged(locales)`
        ]
    }

    ready() {
        super.ready();
        this.$.localeSelectComboBox.addEventListener('value-changed', (e) => this._localeValueChanged(e));
        this.$.rememberMeCheckbox.addEventListener('checked-changed', (e) => this._onRememberMeValueChange(e));

        this.$.localeSelectComboBox.jmixUserOriginated = true
        this.$.rememberMeCheckbox.jmixUserOriginated = true
    }

    _onVisibilityPropertiesChanged(rememberMeVisibility, localesVisibility) {
        this.$.additionalFields.hidden = !rememberMeVisibility && !localesVisibility;
        this.$.rememberMeCheckbox.hidden = !rememberMeVisibility;
        this.$.localeSelectComboBox.hidden = !localesVisibility;
    }

    _onLocalesPropertyChanged(locales) {
        let items = [];
        for (const locale of locales) {
            items.push(locale.localizedName);
        }
        this.$.localeSelectComboBox.items = items;
    }

    selectLocale(localeString) {
        let valueToSelect;
        for (const locale of this.locales) {
            if (locale.localeString === localeString) {
                valueToSelect = locale.localizedName;
            }
        }

        const currentValue = this.$.localeSelectComboBox.value;

        if (valueToSelect
            && currentValue !== valueToSelect) {
            this.$.localeSelectComboBox.jmixUserOriginated = false;
            this.$.localeSelectComboBox.value = valueToSelect;
        }
    }

    setRememberMe(rememberMe) {
        if (this.$.rememberMeCheckbox.checked !== rememberMe) {
            this.$.rememberMeCheckbox.jmixUserOriginated = false;
            this.$.rememberMeCheckbox.checked = rememberMe;
        }
    }

    _onRememberMeValueChange(e) {
        if (this.$.rememberMeCheckbox.jmixUserOriginated) {
            const customEvent = new CustomEvent('rememberMeChanged', {detail: {checked: e.detail.value}});
            this.dispatchEvent(customEvent);
        }
        this.$.rememberMeCheckbox.jmixUserOriginated = true;
    }

    _localeValueChanged(e) {
        const selectedValue = e.detail.value;
        const localeString = this._getLocaleString(selectedValue);

        if (this.$.localeSelectComboBox.jmixUserOriginated) {
            const customEvent = new CustomEvent('localeChanged', {detail: {localeString: localeString}});
            this.dispatchEvent(customEvent);
        }
        this.$.localeSelectComboBox.jmixUserOriginated = true;
    }

    _getLocaleString(localizedName) {
        for (const locale of this.locales) {
            if (locale.localizedName === localizedName) {
                return locale.localeString;
            }
        }
        return null;
    }
}

customElements.define(JmixLoginForm.is, JmixLoginForm);

export {JmixLoginForm};
