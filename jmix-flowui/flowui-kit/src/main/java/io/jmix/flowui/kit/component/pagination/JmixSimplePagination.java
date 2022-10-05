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

package io.jmix.flowui.kit.component.pagination;

import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Span;

public class JmixSimplePagination extends AbstractPagination {

    public static final String SIMPLE_PAGINATION_CLASS_NAME = "jmix-simple-pagination";

    public static final String STATUS_SPAN_CLASS_NAME = "-status";
    public static final String TOTAL_COUNT_SPAN_CLASS_NAME = "-total-count";

    protected Div container;
    protected Span statusSpan;
    protected Span totalCountSpan;

    protected boolean autoLoad = false;

    public JmixSimplePagination() {
        super(SIMPLE_PAGINATION_CLASS_NAME);
    }

    @Override
    protected Div initContent() {
        Div content = super.initContent();
        content.addClassName(SIMPLE_PAGINATION_CLASS_NAME);
        return content;
    }

    @Override
    protected Component createInnerComponent() {
        container = createContainer();
        statusSpan = createStatusSpan();
        totalCountSpan = createTotalCountSpan();

        container.add(statusSpan, totalCountSpan);

        return container;
    }

    /**
     *
     * @return
     */
    public boolean isAutoLoad() {
        return autoLoad;
    }

    /**
     *
     * @param autoLoad
     */
    public void setAutoLoad(boolean autoLoad) {
        this.autoLoad = autoLoad;
    }

    protected Span createStatusSpan() {
        Span statusSpan = new Span();
        statusSpan.addClassName(STATUS_SPAN_CLASS_NAME);
        return statusSpan;
    }

    protected Span createTotalCountSpan() {
        Span totalCountSpan = new Span();
        totalCountSpan.addClassName(TOTAL_COUNT_SPAN_CLASS_NAME);
        return totalCountSpan;
    }

    protected Div createContainer() {
        return new Div();
    }
}
