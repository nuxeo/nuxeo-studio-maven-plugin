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

package org.nuxeo.extractor.mapper.impl;

import java.util.Arrays;
import java.util.List;

import org.nuxeo.extractor.mapper.ExtensionMapper;
import org.nuxeo.extractor.mapper.descriptors.OperationChainDescriptor;
import org.nuxeo.extractor.mapper.descriptors.OperationDescriptor;

public class AutomationMapper extends ExtensionMapper {

    protected static final List<String> EXTENSIONS = Arrays.asList("operations", "chains");

    @Override
    public void registerDescriptors() {
        registerDescriptor("operations", OperationChainDescriptor.class);
        registerDescriptor("operations", OperationDescriptor.class);
    }

    @Override
    protected boolean accept(String target, String point) {
        return target.equals("org.nuxeo.ecm.core.operation.OperationServiceComponent") && EXTENSIONS.contains(point);
    }
}
