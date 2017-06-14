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

package org.nuxeo.maven.mapper.impl;

import org.nuxeo.ecm.core.event.impl.EventListenerDescriptor;
import org.nuxeo.maven.mapper.ExtensionMapper;

public class EventMapper extends ExtensionMapper {
    @Override
    public void registerDescriptors() {
        registerDescriptor("events", EventListenerDescriptor.class);
    }

    @Override
    protected boolean accept(String target, String point) {
        return "org.nuxeo.ecm.core.event.EventServiceComponent".equalsIgnoreCase(target) && "listener".equals(point);
    }
}
