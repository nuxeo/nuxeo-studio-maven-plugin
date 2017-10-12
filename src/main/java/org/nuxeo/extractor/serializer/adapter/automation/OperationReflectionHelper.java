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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Arrays;
import java.util.Calendar;

public class OperationReflectionHelper {

    public static Annotation findAnnotation(AnnotatedElement elt, String annotationType) {
        return Arrays.stream(elt.getDeclaredAnnotations())
                     .filter(s -> s.annotationType().getName().equals(annotationType))
                     .findFirst()
                     .orElse(null);
    }

    public static <V> V invokeMethod(Annotation annotation, String methodName, Class<V> clazz) {
        Method invokable = Arrays.stream(annotation.annotationType().getMethods())
                                 .filter(s -> s.getName().equals(methodName))
                                 .findFirst()
                                 .orElse(null);

        try {
            return clazz.cast(invokable.invoke(annotation));
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static Class<?> findMethodReturn(Method method) {
        return method.getReturnType();
    }

    public static Class<?> findMethodParameterType(Method method) {
        Class<?>[] p = method.getParameterTypes();
        if (p.length > 1) {
            throw new IllegalArgumentException("Operation method must accept at most one argument: " + method);
        }

        return p.length == 0 ? Void.TYPE : p[0];
    }

    public static String getParamDocumentationType(Class<?> type, boolean isIterable) {
        String paramType;

        try {
            if (Class.forName("org.nuxeo.ecm.core.api.DocumentModel").isAssignableFrom(type)
                    || Class.forName("org.nuxeo.ecm.core.api.DocumentRef").isAssignableFrom(type)) {
                paramType = isIterable ? Constants.T_DOCUMENTS : Constants.T_DOCUMENT;
            } else if (Class.forName("org.nuxeo.ecm.core.api.DocumentModelList").isAssignableFrom(type)
                    || Class.forName("org.nuxeo.ecm.core.api.DocumentRefList").isAssignableFrom(type)) {
                paramType = Constants.T_DOCUMENTS;
            } else if (Class.forName("org.nuxeo.ecm.automation.core.util.BlobList").isAssignableFrom(type)) {
                paramType = Constants.T_BLOBS;
            } else if (Class.forName("org.nuxeo.ecm.core.api.Blob").isAssignableFrom(type)) {
                paramType = isIterable ? Constants.T_BLOBS : Constants.T_BLOB;
            } else if (URL.class.isAssignableFrom(type)) {
                paramType = Constants.T_RESOURCE;
            } else if (Calendar.class.isAssignableFrom(type)) {
                paramType = Constants.T_DATE;
            } else {
                paramType = type.getSimpleName().toLowerCase();
            }
            return paramType;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getParamDocumentationType(Class<?> type) {
        return getParamDocumentationType(type, false);
    }
}
