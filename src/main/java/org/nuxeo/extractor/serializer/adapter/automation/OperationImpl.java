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

import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.findAnnotation;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.findMethodParameterType;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.findMethodReturn;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.invokeMethod;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

public class OperationImpl {

    final public static String PARAM_ANNOTATION_TYPE = "org.nuxeo.ecm.automation.core.annotations.Param";

    final public static String OPERATION_ANNOTATION_TYPE = "org.nuxeo.ecm.automation.core.annotations.Operation";

    final public static String METHOD_ANNOTATION_TYPE = "org.nuxeo.ecm.automation.core.annotations.OperationMethod";

    /**
     * The operation ID - used for lookups.
     */
    protected final String id;

    /**
     * The operation ID Aliases array.
     */
    protected final String[] aliases;

    /**
     * The operation type
     */
    protected final Class<?> type;

    /**
     * Injectable parameters. a map between the parameter name and the Field object
     */
    protected final Map<String, Field> params;

    /**
     * Invocable methods
     */
    protected List<String> methods;

    public OperationImpl(Class<?> type) {
        Annotation anno = findAnnotation(type, OPERATION_ANNOTATION_TYPE);
        if (anno == null) {
            throw new IllegalArgumentException(
                    "Invalid operation class: " + type + ". No @Operation annotation found on class.");
        }
        this.type = type;
        String id = invokeMethod(anno, "id", String.class);
        this.id = StringUtils.isEmpty(id) ? type.getName() : id;
        aliases = invokeMethod(anno, "aliases", String[].class);
        params = new HashMap<>();
        methods = new ArrayList<>();
        initMethods();
        initFields();
    }

    protected void initMethods() {
        for (Method method : type.getMethods()) {
            Annotation anno = findAnnotation(method, METHOD_ANNOTATION_TYPE);
            if (anno == null) { // skip method
                continue;
            }

            Class<?> consume = findMethodParameterType(method);
            Class<?> produce = findMethodReturn(method);
            boolean isIterable = !invokeMethod(anno, "collector", Class.class).getName().equals(
                    "org.nuxeo.ecm.automation.OutputCollector");

            methods.add(String.format("%s:%s", //
                    OperationReflectionHelper.getParamDocumentationType(consume, isIterable),
                    OperationReflectionHelper.getParamDocumentationType(produce)));
        }

        // method order depends on the JDK, make it deterministic
        Collections.sort(methods);
    }

    protected void initFields() {
        for (Field field : type.getDeclaredFields()) {
            Annotation annotation = findAnnotation(field, PARAM_ANNOTATION_TYPE);
            if (annotation != null) {
                field.setAccessible(true);
                params.put(invokeMethod(annotation, "name", String.class), field);
            }
        }
    }

    public OperationDocumentation getDocumentation() {
        Annotation operationAnno = findAnnotation(type, OPERATION_ANNOTATION_TYPE);

        OperationDocumentation doc = new OperationDocumentation(invokeMethod(operationAnno, "id", String.class));
        doc.label = invokeMethod(operationAnno, "label", String.class);
        doc.requires = invokeMethod(operationAnno, "requires", String.class);
        doc.category = invokeMethod(operationAnno, "category", String.class);
        doc.since = invokeMethod(operationAnno, "since", String.class);
        doc.deprecatedSince = invokeMethod(operationAnno, "deprecatedSince", String.class);
        doc.addToStudio = invokeMethod(operationAnno, "addToStudio", Boolean.class);
        doc.setAliases(invokeMethod(operationAnno, "aliases", String[].class));
        doc.implementationClass = type.getName();
        if (doc.requires.length() == 0) {
            doc.requires = null;
        }
        if (doc.label.length() == 0) {
            doc.label = doc.id;
        }
        doc.description = invokeMethod(operationAnno, "description", String.class);
        // load parameters information
        List<OperationDocumentation.Param> paramsAccumulator = new LinkedList<>();
        for (Field field : params.values()) {
            Annotation annotation = findAnnotation(field, PARAM_ANNOTATION_TYPE);

            OperationDocumentation.Param param = new OperationDocumentation.Param();
            param.name = invokeMethod(annotation, "name", String.class);
            param.description = invokeMethod(annotation, "description", String.class);
            param.type = OperationReflectionHelper.getParamDocumentationType(field.getType());
            param.widget = invokeMethod(annotation, "widget", String.class);
            if (param.widget.length() == 0) {
                param.widget = null;
            }
            param.order = invokeMethod(annotation, "order", Integer.class);
            param.values = invokeMethod(annotation, "values", String[].class);
            param.required = invokeMethod(annotation, "required", Boolean.class);
            paramsAccumulator.add(param);
        }
        Collections.sort(paramsAccumulator);
        doc.params = paramsAccumulator.toArray(new OperationDocumentation.Param[paramsAccumulator.size()]);

        // load signature
        ArrayList<String> result = new ArrayList<String>(methods.size() * 2);
        for (String m : methods) {
            String[] split = m.split(":");
            result.add(split[0]);
            result.add(split[1]);
        }
        doc.signature = result.toArray(new String[result.size()]);
        return doc;
    }
}
