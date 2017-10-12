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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationImpl.OPERATION_ANNOTATION_TYPE;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationImpl.PARAM_ANNOTATION_TYPE;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.findAnnotation;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.invokeMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import org.dom4j.DocumentException;
import org.junit.Test;
import org.nuxeo.operation.MyOperation;

public class TestComponentParser {
    @Test
    public void testReadAnnotationWithoutImport()
            throws DocumentException, IllegalAccessException, InstantiationException {
        Class<?> type = MyOperation.class;
        initFields(type);
    }

    public void initFields(Class<?> type) {
        assertNotNull(findAnnotation(type, OPERATION_ANNOTATION_TYPE));

        for (Field field : type.getDeclaredFields()) {
            Annotation param = findAnnotation(field, PARAM_ANNOTATION_TYPE);
            if (param == null) {
                continue;
            }

            assertEquals("dummy", invokeMethod(param, "name", String.class));
        }
    }
}
