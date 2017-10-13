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

package org.nuxeo.extractor.serializer.adapter.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Schema {
    protected String name;

    protected String prefix;

    protected List<Field> fields = new ArrayList<>();

    public Schema(String name, String prefix) {
        this.name = name;
        this.prefix = prefix;
    }

    public void addFields(Collection<Field> fields) {
        this.fields.addAll(fields);
    }

    public String getName() {
        return name;
    }

    public String getPrefix() {
        return prefix;
    }

    public List<Field> getFields() {
        return fields;
    }
}
