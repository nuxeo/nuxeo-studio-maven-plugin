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

public class SimpleField implements Field {

    private boolean isArray;

    private String name;

    private String type;

    public SimpleField(String name, String type, boolean isArray) {
        this.name = name;
        this.isArray = isArray;
        this.type = type;
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
        return false;
    }

    public String getType() {
        return type;
    }

    public String getTypeJson() {
        return type + (isArray ? "[]" : "");
    }

    @Override
    public Field copy(String name, Boolean isArray) {
        return new SimpleField(name, type, isArray);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        SimpleField that = (SimpleField) o;

        if (isArray != that.isArray)
            return false;
        if (name != null ? !name.equals(that.name) : that.name != null)
            return false;
        return type != null ? type.equals(that.type) : that.type == null;
    }

    @Override
    public int hashCode() {
        int result = (isArray ? 1 : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
