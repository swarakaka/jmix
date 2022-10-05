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

package io.jmix.flowui.component.loginform;

import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.ComponentUtil;
import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.shared.Registration;
import io.jmix.core.LocaleResolver;
import io.jmix.core.MessageTools;
import io.jmix.flowui.kit.component.loginform.JmixLoginForm;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.Locale;

public class StandardLoginForm extends JmixLoginForm implements ApplicationContextAware, InitializingBean {

    protected ApplicationContext applicationContext;
    protected MessageTools messageTools;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        autowireDependencies();
        initComponent();
    }

    protected void initComponent() {
        setRememberMeChangedHandler(this::onRememberMeChangedEvent);
        setLocaleChangedHandler(this::onLocaleChangedEvent);
    }

    protected void autowireDependencies() {
        messageTools = applicationContext.getBean(MessageTools.class);
    }

    /**
     * Selects default locale from {@link MessageTools#getDefaultLocale()}.
     */
    public void setSelectedDefaultLocale() {
        setSelectedLocale(messageTools.getDefaultLocale());
    }

    /**
     * Selects provided locale if locale options contain it.
     *
     * @param locale locale to select
     */
    @Override
    public void setSelectedLocale(Locale locale) {
        super.setSelectedLocale(locale);

        if (isLocaleChanged(locale)) {
            handleLocaleChanged(false, locale);
        }
    }

    /**
     * Sets whether "Remember Me" option should be checked or not.
     *
     * @param rememberMe rememberMe option
     */
    @Override
    public void setRememberMe(boolean rememberMe) {
        super.setRememberMe(rememberMe);

        if (isRememberMeChanged(rememberMe)) {
            handleRememberMeChanged(false, rememberMe);
        }
    }

    /**
     * Adds listener to handle changes in "Remember Me" option.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    public Registration addRememberMeChangedListener(ComponentEventListener<RememberMeChangedEvent> listener) {
        return ComponentUtil.addListener(this, RememberMeChangedEvent.class, listener);
    }

    /**
     * Adds listener to handle locale selection changes.
     *
     * @param listener listener to add
     * @return a registration object for removing an event listener
     */
    public Registration addLocaleChangedListener(ComponentEventListener<LocaleChangedEvent> listener) {
        return ComponentUtil.addListener(this, LocaleChangedEvent.class, listener);
    }

    protected void onRememberMeChangedEvent(JmixRememberMeChangedEvent event) {
        handleRememberMeChanged(event.isFromClient(), event.isChecked());
    }

    protected void handleRememberMeChanged(boolean isFromClient, boolean newValue) {
        rememberMe = newValue;

        RememberMeChangedEvent changedEvent =
                new RememberMeChangedEvent(this, isFromClient, rememberMe);

        getEventBus().fireEvent(changedEvent);
    }

    protected void onLocaleChangedEvent(JmixLocaleChangedEvent event) {
        Locale locale = LocaleResolver.resolve(event.getLocaleString());

        handleLocaleChanged(event.isFromClient(), locale);
    }

    protected void handleLocaleChanged(boolean isFromClient, Locale newLocale) {
        Locale oldValue = selectedLocale;
        selectedLocale = newLocale;

        setupLocale(selectedLocale);

        fireLocaleChangedEvent(oldValue, selectedLocale, isFromClient);
    }

    protected void fireLocaleChangedEvent(Locale oldValue, Locale value, Boolean isFromClient) {
        LocaleChangedEvent changedEvent =
                new LocaleChangedEvent(this, isFromClient, oldValue, value);

        getEventBus().fireEvent(changedEvent);
    }

    protected void setupLocale(Locale locale) {
        VaadinSession.getCurrent().setLocale(locale);
    }

    @Override
    protected String localeToString(Locale locale) {
        return LocaleResolver.localeToString(locale);
    }

    @Override
    protected String getLocalizedItemLabel(Locale locale) {
        return messageTools.getLocaleDisplayName(locale);
    }

    /**
     * An event that is fired when "Remember Me" becomes checked and unchecked.
     */
    public static class RememberMeChangedEvent extends ComponentEvent<StandardLoginForm> {

        protected boolean checked;

        public RememberMeChangedEvent(StandardLoginForm source, boolean fromClient, boolean checked) {
            super(source, fromClient);
            this.checked = checked;
        }

        /**
         * @return {@code true} if "Remember Me" option is checked
         */
        public boolean isChecked() {
            return checked;
        }
    }

    /**
     * An event that is fired when the user selects another locale.
     */
    public static class LocaleChangedEvent extends ComponentEvent<StandardLoginForm> {

        protected Locale oldValue;
        protected Locale value;

        public LocaleChangedEvent(StandardLoginForm source, boolean fromClient, Locale oldValue, Locale value) {
            super(source, fromClient);
            this.oldValue = oldValue;
            this.value = value;
        }

        /**
         * @return previous value
         */
        public Locale getOldValue() {
            return oldValue;
        }

        /**
         * @return current value
         */
        public Locale getValue() {
            return value;
        }
    }
}
