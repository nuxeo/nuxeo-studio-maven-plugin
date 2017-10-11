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

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XObject;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Descriptor for a life cycle extension.
 *
 * @see org.nuxeo.ecm.core.lifecycle.impl.LifeCycleServiceImpl
 * @see org.nuxeo.ecm.core.lifecycle.LifeCycle
 * @author Julien Anguenot
 * @author Florent Guillaume
 */
@XObject(value = "lifecycle", order = { "@name" })
public class LifeCycleDescriptor {

    private static final Log log = LogFactory.getLog(LifeCycleDescriptor.class);

    @XNode("@name")
    private String name;

    @XNode("@lifecyclemanager")
    public void setLifeCycleManager(String lifeCycleManager) {
        log.warn("Ignoring deprecated lifecyclemanager attribute '" + lifeCycleManager + "' for lifecycle '" + name
                + "'");
    }

    @XNode("@initial")
    private String initialStateName;

    @XNode("@defaultInitial")
    private String defaultInitialStateName;

    @XNode("description")
    private String description;

    @XNode("states")
    private Element states;

    private List<String> computedStates;

    @XNode("transitions")
    private Element transitions;

    private List<String> computedTransitions;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public List<String> getStates() {
        if (computedStates == null) {
            computedStates = new ArrayList<>();
            NodeList elements = states.getElementsByTagName("state");
            int len = elements.getLength();
            for (int i = 0; i < len; i++) {
                Element item = (Element) elements.item(i);
                computedStates.add(item.getAttribute("name"));
            }
        }

        return computedStates;
    }

    public List<String> getTransitions() {
        if (computedTransitions == null) {
            computedTransitions = new ArrayList<>();

            NodeList transitionsElements = transitions.getElementsByTagName("transitions");
            Element transitionsElement = null;
            if (transitionsElements.getLength() > 0) {
                // NXP-1472 : don't get the first element, but the first one attached to <lifecycle>
                for (int i = 0; i < transitionsElements.getLength(); i++) {
                    transitionsElement = (Element) transitionsElements.item(i);
                    if ("lifecycle".equals(transitionsElement.getParentNode().getNodeName())) {
                        break;
                    }
                }
            } else {
                return computedTransitions;
            }

            NodeList elements = transitionsElement.getElementsByTagName("transition");
            int len = elements.getLength();
            for (int i = 0; i < len; i++) {
                Element element = (Element) elements.item(i);
                computedTransitions.add(element.getAttribute("name"));
            }
        }

        return computedTransitions;
    }

    @Override
    public String toString() {
        return "Lifecycle: " + name;
    }
}
