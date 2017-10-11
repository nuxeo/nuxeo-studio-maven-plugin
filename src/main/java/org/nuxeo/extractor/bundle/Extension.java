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

package org.nuxeo.extractor.bundle;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.dom4j.Node;

public class Extension {
    protected final String nodeXml;

    protected final String targetComponent;

    protected final String extensionPoint;

    public Extension(Node node) {
        targetComponent = node.valueOf("@target");
        extensionPoint = node.valueOf("@point");
        this.nodeXml = node.asXML();
    }

    public String getTargetComponent() {
        return targetComponent;
    }

    public String getExtensionPoint() {
        return extensionPoint;
    }

    public InputStream getInputStream() {
        return new ByteArrayInputStream(nodeXml.getBytes());
    }
}
