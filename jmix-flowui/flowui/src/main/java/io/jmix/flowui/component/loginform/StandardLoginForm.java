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
import java.util.Map;

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

        Map<String, Locale> availableLocalesMap = messageTools.getAvailableLocalesMap();
        setLocaleOptions(availableLocalesMap);

        selectLocale(VaadinSession.getCurrent().getLocale());
    }

    protected void autowireDependencies() {
        messageTools = applicationContext.getBean(MessageTools.class);
    }

    public void selectDefaultLocale() {
        selectLocale(messageTools.getDefaultLocale());
    }

    public Registration addRememberMeChangedListener(ComponentEventListener<RememberMeChangedEvent> listener) {
        return ComponentUtil.addListener(this, RememberMeChangedEvent.class, listener);
    }

    public Registration addLocaleChangedListener(ComponentEventListener<LocaleChangedEvent> listener) {
        return ComponentUtil.addListener(this, LocaleChangedEvent.class, listener);
    }

    protected void onRememberMeChangedEvent(JmixRememberMeChangedEvent event) {
        rememberMe = event.isChecked();

        // todo rp from client?

        RememberMeChangedEvent changedEvent =
                new RememberMeChangedEvent(this, event.isFromClient(), event.isChecked());

        getEventBus().fireEvent(changedEvent);
    }

    protected void onLocaleChangedEvent(JmixLocaleChangedEvent event) {
        Locale locale = LocaleResolver.resolve(event.getLocaleString());

        Locale oldValue = selectedLocale;
        selectedLocale = locale;

        setupLocale(selectedLocale);

        LocaleChangedEvent changedEvent =
                new LocaleChangedEvent(this, event.isJmixFromClient(), oldValue, selectedLocale);

        getEventBus().fireEvent(changedEvent);
    }

    protected void setupLocale(Locale locale) {
        VaadinSession.getCurrent().setLocale(locale);
    }

    @Override
    protected String resolveLocale(Locale locale) {
        return LocaleResolver.localeToString(locale);
    }

    public static class RememberMeChangedEvent extends ComponentEvent<StandardLoginForm> {

        protected Boolean checked;

        public RememberMeChangedEvent(StandardLoginForm source, boolean fromClient, Boolean checked) {
            super(source, fromClient);
            this.checked = checked;
        }

        public Boolean isChecked() {
            return checked;
        }
    }

    public static class LocaleChangedEvent extends ComponentEvent<StandardLoginForm> {

        protected Locale oldValue;
        protected Locale value;

        public LocaleChangedEvent(StandardLoginForm source, boolean fromClient, Locale oldValue, Locale value) {
            super(source, fromClient);
            this.oldValue = oldValue;
            this.value = value;
        }

        public Locale getOldValue() {
            return oldValue;
        }

        public Locale getValue() {
            return value;
        }
    }
}
