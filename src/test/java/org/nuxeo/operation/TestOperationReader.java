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

package org.nuxeo.operation;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.nuxeo.extractor.serializer.adapter.automation.Constants;
import org.nuxeo.extractor.serializer.adapter.automation.OperationDocumentation;
import org.nuxeo.extractor.serializer.adapter.automation.OperationReader;

public class TestOperationReader {

    @Test
    public void testOperationAnnotationWithDefaultValues() {
        OperationReader or = new OperationReader(MyOperation.class.getName());
        assertThat(or).isNotNull();
        or.readOperationAnnotation();

        assertThat(or.getId()).isEqualTo(MyOperation.ID);
        assertThat(or.getCategory()).isEqualTo("Others");
        assertThat(or.getLabel()).isEqualTo(MyOperation.ID);
        assertThat(or.getRequires()).isNull();
        assertThat(or.getDescription()).isEqualTo("");
        assertThat(or.getSince()).isEqualTo("");
        assertThat(or.getDeprecatedSince()).isEqualTo("");
        assertThat(or.getAddToStudio()).isTrue();
        assertThat(or.getAliases()).isEmpty();
    }

    @Test
    public void testOperationAnnotationWithValues() {
        OperationReader or = new OperationReader(MyFullOperation.class.getName());
        assertThat(or).isNotNull();
        or.readOperationAnnotation();

        assertThat(or.getId()).isEqualTo(MyFullOperation.ID);
        assertThat(or.getCategory()).isEqualTo("foo");
        assertThat(or.getLabel()).isEqualTo("bar");
        assertThat(or.getRequires()).isEqualTo("something");
        assertThat(or.getDescription()).isEqualTo("description");
        assertThat(or.getSince()).isEqualTo("9.3");
        assertThat(or.getDeprecatedSince()).isEqualTo("9.10");
        assertThat(or.getAddToStudio()).isFalse();
        assertThat(or.getAliases()).hasSize(2);
    }

    @Test
    public void testOperationFieldAnnotationWithDefaultValue() {
        OperationReader or = new OperationReader(MyOperation.class.getName());
        assertThat(or).isNotNull();
        or.readOperationParams();

        assertThat(or.getParameters()).hasSize(1);
        OperationDocumentation.Param param = or.getParameters().get(0);

        assertThat(param.name).isEqualTo("dummy");
        assertThat(param.type).isEqualTo(Constants.T_STRING);
        assertThat(param.isRequired()).isTrue();
        assertThat(param.description).isEqualTo("");
        assertThat(param.values).isEmpty();
        assertThat(param.order).isEqualTo(0);
    }

    @Test
    public void testOperationFieldAnnotationWithFullValues() {
        OperationReader or = new OperationReader(MyFullOperation.class.getName());
        assertThat(or).isNotNull();
        or.readOperationParams();

        assertThat(or.getParameters()).hasSize(1);
        OperationDocumentation.Param param = or.getParameters().get(0);

        assertThat(param.name).isEqualTo("parameters");
        assertThat(param.type).isEqualTo(Constants.T_STRING + "[]");
        assertThat(param.isRequired()).isFalse();
        assertThat(param.description).isEqualTo("description");
        assertThat(param.values).hasSize(2);
        assertThat(param.order).isEqualTo(2);
    }

    @Test
    public void testOperationMethod() {
        OperationReader or = new OperationReader(MyOperation.class.getName());
        assertThat(or).isNotNull();
        or.readOperationMethods();

        List<String> methods = or.getMethods();
        assertThat(methods).hasSize(1);

        String method = methods.get(0);
        assertThat(method).isEqualTo("document:document");
    }

    @Test
    public void testOperationMethodWithMoreAttributes() {
        OperationReader or = new OperationReader(MyFullOperation.class.getName());
        assertThat(or).isNotNull();
        or.readOperationMethods();

        List<String> methods = or.getMethods();
        assertThat(methods).hasSize(2);

        assertThat(methods).containsOnlyOnce("documents:document", "void:documents");
    }
}
