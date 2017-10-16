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
package org.nuxeo.extractor.serializer.adapter.automation;

import java.io.Serializable;
import java.util.Arrays;

import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.extractor.mapper.descriptors.OperationChainDescriptor;

public class OperationDocumentation implements Comparable<OperationDocumentation>, Serializable {

    private static final long serialVersionUID = 1L;

    public String id;

    public String[] aliases;

    public String[] signature;

    public String category;

    public String label;

    public String requires;

    public String since;

    public String description;

    public Param[] params;

    /**
     * The operations listing in case of a chain.
     */
    public OperationChainDescriptor.Operation[] operations;

    public OperationDocumentation(String id) {
        this.id = id;
    }

    @XObject("param")
    public static class Param implements Serializable, Comparable<Param> {
        private static final long serialVersionUID = 1L;

        @XNode("@name")
        public String name;

        @XNode("@description")
        public String description;

        @XNode("@type")
        public String type; // the data type

        // is this useful (?)
        public String widget; // the widget type

        // is this useful (?)
        @XNodeList(value = "value", type = String[].class, componentType = String.class)
        public String[] values; // the default values

        // is this useful (?)
        @XNode("@order")
        public int order;

        // is this useful (?)
        @XNode("@required")
        public boolean required;

        public Param() {
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public String getType() {
            return type;
        }

        public String[] getValues() {
            return values;
        }

        public boolean isRequired() {
            return required;
        }

        public int getOrder() {
            return order;
        }

        @Override
        public String toString() {
            return name + " [" + type + "] " + (required ? "required" : "optional");
        }

        @Override
        public int compareTo(Param o) {
            if (order != 0 && o.order != 0) {
                if (order < o.order) {
                    return -1;
                } else if (order > o.order) {
                    return 1;
                }
            }
            if (required && !o.required) {
                return -1;
            }
            if (o.required && !required) {
                return 1;
            }
            return name.compareTo(o.name);
        }
    }

    @Override
    public int compareTo(OperationDocumentation o) {
        String s1 = label == null ? id : label;
        String s2 = o.label == null ? o.id : o.label;
        return s1.compareTo(s2);
    }

    public String getDescription() {
        return description;
    }

    public String getSince() {
        return since;
    }

    public String getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getRequires() {
        return requires;
    }

    public Param[] getParams() {
        return params;
    }

    public String[] getAliases() {
        return aliases;
    }

    public void setAliases(String[] aliases) {
        this.aliases = aliases;
    }

    public OperationChainDescriptor.Operation[] getOperations() {
        return operations;
    }

    @Override
    public String toString() {
        return category + " > " + label + " [" + id + ": " + Arrays.asList(signature) + "] (" + params + ")\n"
                + description;
    }
}
