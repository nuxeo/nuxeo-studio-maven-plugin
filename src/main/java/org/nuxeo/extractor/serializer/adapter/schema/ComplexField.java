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
import java.util.Collections;
import java.util.List;

public class ComplexField implements Field {

    private String name;

    private boolean isArray;

    protected List<Field> fields = new ArrayList<>();

    public ComplexField(String name, boolean isArray) {
        this.name = name;
        this.isArray = isArray;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public boolean isArray() {
        return isArray;
    }

    @Override
    public boolean isComplex() {
        return true;
    }

    public void addField(Field... fields) {
        Collections.addAll(this.fields, fields);
    }

    public List<Field> getFields() {
        return new ArrayList<>(fields);
    }

    @Override
    public Field copy(String name, Boolean isArray) {
        ComplexField o = new ComplexField(name, isArray);
        o.fields.addAll(fields);
        return o;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        ComplexField that = (ComplexField) o;

        if (isArray != that.isArray)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        return fields != null ? fields.equals(that.fields) : that.fields == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (isArray ? 1 : 0);
        result = 31 * result + (fields != null ? fields.hashCode() : 0);
        return result;
    }
}
