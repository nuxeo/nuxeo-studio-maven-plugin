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

import static org.nuxeo.extractor.serializer.adapter.automation.Constants.CAT_CHAIN;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.nuxeo.common.xmap.annotation.XContent;
import org.nuxeo.common.xmap.annotation.XNode;
import org.nuxeo.common.xmap.annotation.XNodeList;
import org.nuxeo.common.xmap.annotation.XNodeMap;
import org.nuxeo.common.xmap.annotation.XObject;
import org.nuxeo.extractor.serializer.adapter.automation.OperationDocumentation;

/**
 * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
 */
@XObject("chain")
public class OperationChainDescriptor {

    @XNode("@id")
    protected String id;

    @XNode("@replace")
    protected boolean replace = true;

    @XNode("description")
    protected String description;

    @XNodeList(value = "operation", type = Operation[].class, componentType = Operation.class)
    protected Operation[] ops = new Operation[0];

    @XNode("public")
    protected boolean isPublic = true;

    @XNodeList(value = "param", type = OperationDocumentation.Param[].class, componentType = OperationDocumentation.Param.class)
    protected OperationDocumentation.Param[] params = new OperationDocumentation.Param[0];

    @XNodeList(value = "aliases/alias", type = String[].class, componentType = String.class)
    protected String[] aliases;

    @XObject("operation")
    public static class Operation {
        @XNode("@id")
        protected String id;

        @XNodeList(value = "param", type = ArrayList.class, componentType = Param.class)
        protected List<Param> params = new ArrayList<>();

        public String getId() {
            return id;
        }

        public List<Param> getParams() {
            return params;
        }
    }

    @XObject("param")
    public static class Param {
        @XNode("@name")
        protected String name;

        // string, boolean, date, integer, float, uid, path, expression,
        // template, resource
        @XNode("@type")
        protected String type = "string";

        // why not XNode here? XContent requires to unescape XML entities, see
        // below
        @XContent
        protected String value;

        // Optional map for properties type values
        @XNodeMap(value = "property", key = "@key", type = HashMap.class, componentType = String.class, nullByDefault = true)
        protected Map<String, String> map;

        public String getName() {
            return name;
        }

        public String getValue() {
            return value;
        }

        public String getType() {
            return type;
        }

        public Map<String, String> getMap() {
            return map;
        }
    }

    public OperationDocumentation.Param[] getParams() {
        return params;
    }

    public String getId() {
        return id;
    }

    public Operation[] getOps() {
        return ops;
    }

    public String getLabel() {
        return id;
    }

    public String getRequires() {
        return "";
    }

    public String getCategory() {
        return CAT_CHAIN;
    }

    public String getSince() {
        return "";
    }

    public String getDescription() {
        return description;
    }

    public String[] getAliases() {
        return aliases;
    }

}
