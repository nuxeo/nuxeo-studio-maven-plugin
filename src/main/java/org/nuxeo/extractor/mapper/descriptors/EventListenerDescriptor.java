/*
 * (C) Copyright 2017 Nuxeo SA (http://nuxeo.com/) and others.
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
 *
 * Contributors:
 *     Arnaud Kervern
 */
package org.nuxeo.extractor.mapper.descriptors;

import java.util.HashSet;
import java.util.Set;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;

/**
 * XObject descriptor to declare event listeners
 */
@XObject("listener")
public class EventListenerDescriptor {

    @XNode("@name")
    protected String name;

    /**
     * The event listener class.
     */
    @XNode("@class")
    protected String className;

    /**
     * A script reference: URL, file path, or bundle entry. Runtime variable are expanded. To specify a bundle entry use
     * the URL schema "bundle:"
     */
    @XNode("@script")
    protected String script;

    /**
     * Applies only for scripts.
     */
    @XNode("@postCommit")
    protected boolean isPostCommit;

    /**
     * Applies only for post commit listener
     */
    @XNode("@async")
    protected Boolean isAsync;

    @XNode("@transactionTimeOut")
    protected Integer transactionTimeOut;

    /**
     * The priority to be used to order listeners.
     */
    @XNode("@priority")
    protected Integer priority;

    @XNode("@enabled")
    protected boolean isEnabled = true;

    @XNode("@retryCount")
    protected Integer retryCount;

    @XNode("@singlethread")
    protected boolean singleThreaded = false;

    protected Set<String> events;

    public int getPriority() {
        return priority == null ? 0 : priority.intValue();
    }

    public boolean isEnabled() {
        return isEnabled;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public Set<String> getEvents() {
        return events;
    }

    @XNodeList(value = "event", componentType = String.class, type = HashSet.class, nullByDefault = true)
    public void setEvents(Set<String> events) {
        this.events = events.isEmpty() ? null : events;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    @Override
    public String toString() {
        return "Event: " + name;
    }
}
