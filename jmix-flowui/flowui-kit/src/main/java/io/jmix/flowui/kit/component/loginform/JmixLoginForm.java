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

package io.jmix.flowui.kit.component.loginform;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.login.LoginForm;
import com.vaadin.flow.internal.JsonSerializer;
import com.vaadin.flow.shared.Registration;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Tag("jmix-login-form")
@JsModule("./src/login-form/jmix-login-form.js")
public class JmixLoginForm extends LoginForm {

    private static final String REMEMBER_ME_CHANGED_EVENT = "rememberMeChanged";
    private static final String LOCALE_CHANGED_EVENT = "localeChanged";

    private static final String USERNAME_PROPERTY = "username";
    private static final String PASSWORD_PROPERTY = "password";
    private static final String REMEMBER_ME_VISIBILITY_PROPERTY = "rememberMeVisibility";
    private static final String LOCALES_VISIBILITY_PROPERTY = "localesVisibility";

    protected Map<String, Locale> locales;
    protected Locale selectedLocale = null;
    protected boolean rememberMe = false;

    protected Registration rememberMeChangedRegistration;
    protected Registration localeChangedRegistration;

    /**
     * @return entered username
     */
    @Synchronize(USERNAME_PROPERTY)
    public String getUsername() {
        return getElement().getProperty(USERNAME_PROPERTY);
    }

    /**
     * Sets username to the field.
     *
     * @param username username to set
     */
    public void setUsername(String username) {
        getElement().setProperty(USERNAME_PROPERTY, username);
    }

    /**
     * @return entered password
     */
    @Synchronize(PASSWORD_PROPERTY)
    public String getPassword() {
        return getElement().getProperty(PASSWORD_PROPERTY);
    }

    /**
     * Sets password to the field.
     *
     * @param password password to set
     */
    public void setPassword(String password) {
        getElement().setProperty(PASSWORD_PROPERTY, password);
    }

    /**
     * @return {@code true} if "Remember Me" component is visible
     */
    @Synchronize(REMEMBER_ME_VISIBILITY_PROPERTY)
    public boolean isRememberMeVisible() {
        return getElement().getProperty(REMEMBER_ME_VISIBILITY_PROPERTY, true);
    }

    /**
     * Sets visibility of "Remember Me" component.
     *
     * @param visible whether component should be visible
     */
    public void setRememberMeVisible(boolean visible) {
        getElement().setProperty(REMEMBER_ME_VISIBILITY_PROPERTY, visible);
    }

    /**
     * @return {@code true} if component with locales is visible
     */
    @Synchronize(LOCALES_VISIBILITY_PROPERTY)
    public boolean isLocalesSelectVisible() {
        return getElement().getProperty(LOCALES_VISIBILITY_PROPERTY, true);
    }

    /**
     * Sets visibility of component with locales
     *
     * @param visible whether component should be visible
     */
    public void setLocalesSelectVisible(boolean visible) {
        getElement().setProperty(LOCALES_VISIBILITY_PROPERTY, visible);
    }

    public void setLocaleOptions(Map<String, Locale> locales) {
        this.locales = new HashMap<>(locales);

        List<LocaleItem> localeItems = locales.entrySet().stream()
                .map(entry -> new LocaleItem(localeToString(entry.getValue()), entry.getKey()))
                .collect(Collectors.toList());

        getElement().setPropertyJson("locales", JsonSerializer.toJson(localeItems));
    }

    /**
     * @return selected locale
     */
    public Locale getSelectedLocale() {
        return selectedLocale;
    }

    public void selectLocale(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("Locale cannot be null");
        }

        if (isLocaleChanged(locale)) {
            getElement().callJsFunction("selectLocale", localeToString(locale));
        }
    }

    /**
     * @return {@code true} if "Remember Me" option is checked
     */
    public boolean isRememberMe() {
        return rememberMe;
    }

    public void setRememberMe(boolean rememberMe) {
        if (isRememberMeChanged(rememberMe)) {
            getElement().callJsFunction("setRememberMe", rememberMe);
        }
    }

    protected void setRememberMeChangedHandler(ComponentEventListener<JmixRememberMeChangedEvent> listener) {
        if (rememberMeChangedRegistration != null) {
            rememberMeChangedRegistration.remove();
            rememberMeChangedRegistration = null;
        }
        if (listener != null) {
            rememberMeChangedRegistration = ComponentUtil.addListener(this, JmixRememberMeChangedEvent.class, listener);
        }
    }

    protected void setLocaleChangedHandler(ComponentEventListener<JmixLocaleChangedEvent> listener) {
        if (localeChangedRegistration != null) {
            localeChangedRegistration.remove();
            localeChangedRegistration = null;
        }
        if (listener != null) {
            localeChangedRegistration = ComponentUtil.addListener(this, JmixLocaleChangedEvent.class, listener);
        }
    }

    protected boolean isRememberMeChanged(boolean rememberMe) {
        return this.rememberMe != rememberMe;
    }

    protected boolean isLocaleChanged(Locale locale) {
        return locales.containsValue(locale) && !locale.equals(selectedLocale);
    }

    protected String localeToString(Locale locale) {
        return locale.toLanguageTag();
    }

    @DomEvent(REMEMBER_ME_CHANGED_EVENT)
    protected static class JmixRememberMeChangedEvent extends ComponentEvent<JmixLoginForm> {

        protected final Boolean checked;

        public JmixRememberMeChangedEvent(JmixLoginForm source, boolean fromClient,
                                          @EventData("event.detail.checked") Boolean checked) {
            super(source, fromClient);
            this.checked = checked;
        }

        public Boolean isChecked() {
            return checked;
        }
    }

    @DomEvent(LOCALE_CHANGED_EVENT)
    protected static class JmixLocaleChangedEvent extends ComponentEvent<JmixLoginForm> {

        protected final String localeString;

        public JmixLocaleChangedEvent(JmixLoginForm source, boolean fromClient,
                                      @EventData("event.detail.localeString") String localeString) {
            super(source, fromClient);
            this.localeString = localeString;
        }

        public String getLocaleString() {
            return localeString;
        }
    }
}
