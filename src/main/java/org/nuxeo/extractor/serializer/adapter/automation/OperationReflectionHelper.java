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

import static javassist.bytecode.AnnotationsAttribute.invisibleTag;
import static javassist.bytecode.AnnotationsAttribute.visibleTag;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.SignatureAttribute;
import javassist.bytecode.annotation.ArrayMemberValue;
import javassist.bytecode.annotation.BooleanMemberValue;
import javassist.bytecode.annotation.ClassMemberValue;
import javassist.bytecode.annotation.IntegerMemberValue;
import javassist.bytecode.annotation.StringMemberValue;

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

    public static String getOperationMethodTypes(String signature, boolean isIterable) {
        SignatureAttribute.MethodSignature methodSignature;
        try {
            methodSignature = SignatureAttribute.toMethodSignature(signature);
        } catch (BadBytecode e) {
            throw new RuntimeException(e);
        }

        String consume;
        if (methodSignature.getParameterTypes().length > 0) {
            consume = convertJvmType(methodSignature.getParameterTypes()[0].jvmTypeName(), isIterable);
        } else {
            consume = Void.TYPE.getSimpleName();
        }

        String produce = convertJvmType(methodSignature.getReturnType().jvmTypeName(), false);

        return String.format("%s:%s", consume, produce);
    }

    public static String getParamDocumentationType(String signature, boolean isIterable) {
        SignatureAttribute.ObjectType sign;
        try {
            sign = SignatureAttribute.toFieldSignature(signature);
        } catch (BadBytecode e) {
            throw new RuntimeException(e);
        }
        return convertJvmType(sign.jvmTypeName(), isIterable);
    }

    protected static String convertJvmType(String signature, boolean isIterable) {
        String paramType;
        if ("org.nuxeo.ecm.core.api.DocumentModel".equals(signature)
                || "org.nuxeo.ecm.core.api.DocumentRef".equals(signature)
                || "org.nuxeo.ecm.core.api.PathRef".equals(signature)
                || "org.nuxeo.ecm.core.api.IdRef".equals(signature)) {
            paramType = isIterable ? Constants.T_DOCUMENTS : Constants.T_DOCUMENT;
        } else if ("org.nuxeo.ecm.core.api.DocumentModelList".equals(signature)
                || "org.nuxeo.ecm.core.api.DocumentRefList".equals(signature)) {
            paramType = Constants.T_DOCUMENTS;
        } else if ("org.nuxeo.ecm.automation.core.util.BlobList".equals(signature)) {
            paramType = Constants.T_BLOBS;
        } else if ("org.nuxeo.ecm.core.api.Blob".equals(signature)) {
            paramType = isIterable ? Constants.T_BLOBS : Constants.T_BLOB;
        } else if ("java.net.URL".equals(signature)) {
            paramType = Constants.T_RESOURCE;
        } else if ("java.util.Calendar".equals(signature)) {
            paramType = Constants.T_DATE;
        } else {
            paramType = signature.substring(signature.lastIndexOf(".") + 1).toLowerCase();
        }
        return paramType;
    }

    public static String getParamDocumentationType(String signature) {
        return getParamDocumentationType(signature, false);
    }

    public static String readAnnoMemberString(javassist.bytecode.annotation.Annotation anno, String name) {
        if (!contains(anno, name)) {
            return null;
        }
        return ((StringMemberValue) anno.getMemberValue(name)).getValue();
    }

    public static Boolean readAnnoMemberBoolean(javassist.bytecode.annotation.Annotation anno, String name) {
        if (!contains(anno, name)) {
            return null;
        }
        return ((BooleanMemberValue) anno.getMemberValue(name)).getValue();
    }

    public static Integer readAnnoMemberInteger(javassist.bytecode.annotation.Annotation anno, String name) {
        if (!contains(anno, name)) {
            return null;
        }
        return ((IntegerMemberValue) anno.getMemberValue(name)).getValue();
    }

    public static String readAnnoMemberClass(javassist.bytecode.annotation.Annotation anno, String name) {
        if (!contains(anno, name)) {
            return null;
        }

        return ((ClassMemberValue) anno.getMemberValue(name)).getValue();
    }

    public static String[] readAnnoMemberStringArray(javassist.bytecode.annotation.Annotation anno, String name) {
        if (!contains(anno, name)) {
            return null;
        }
        return Arrays.stream(((ArrayMemberValue) anno.getMemberValue(name)).getValue())
                     .map(s -> ((StringMemberValue) s).getValue())
                     .collect(Collectors.toList())
                     .toArray(new String[0]);
    }

    protected static boolean contains(javassist.bytecode.annotation.Annotation anno, String name) {
        Set names = anno.getMemberNames();
        return names == null ? false : names.contains(name);
    }

    public static javassist.bytecode.annotation.Annotation findAnnotation(CtClass ctClass, String name) {
        // First lookup on visible annotations
        ClassFile classFile = ctClass.getClassFile();
        AnnotationsAttribute annoAttr = (AnnotationsAttribute) classFile.getAttribute(visibleTag);
        if (annoAttr != null) {
            javassist.bytecode.annotation.Annotation anno = annoAttr.getAnnotation(name);
            if (anno != null) {
                return anno;
            }
        }

        // Second lookup on in-visible annotations
        annoAttr = (AnnotationsAttribute) classFile.getAttribute(invisibleTag);
        if (annoAttr != null) {
            javassist.bytecode.annotation.Annotation anno = annoAttr.getAnnotation(name);
            if (anno != null) {
                return anno;
            }
        }

        return null;
    }

    public static javassist.bytecode.annotation.Annotation findAnnotation(CtField field, String name) {
        FieldInfo fi = field.getFieldInfo();
        // First lookup on visible annotations
        AnnotationsAttribute annoAttr = (AnnotationsAttribute) fi.getAttribute(visibleTag);
        if (annoAttr != null) {
            javassist.bytecode.annotation.Annotation anno = annoAttr.getAnnotation(name);
            if (anno != null) {
                return anno;
            }
        }

        // Second lookup on in-visible annotations
        annoAttr = (AnnotationsAttribute) fi.getAttribute(invisibleTag);
        if (annoAttr != null) {
            javassist.bytecode.annotation.Annotation anno = annoAttr.getAnnotation(name);
            if (anno != null) {
                return anno;
            }
        }

        return null;
    }

    public static javassist.bytecode.annotation.Annotation findAnnotation(CtMethod method, String name) {
        MethodInfo fi = method.getMethodInfo();
        // First lookup on visible annotations
        AnnotationsAttribute annoAttr = (AnnotationsAttribute) fi.getAttribute(visibleTag);
        if (annoAttr != null) {
            javassist.bytecode.annotation.Annotation anno = annoAttr.getAnnotation(name);
            if (anno != null) {
                return anno;
            }
        }

        // Second lookup on in-visible annotations
        annoAttr = (AnnotationsAttribute) fi.getAttribute(invisibleTag);
        if (annoAttr != null) {
            javassist.bytecode.annotation.Annotation anno = annoAttr.getAnnotation(name);
            if (anno != null) {
                return anno;
            }
        }

        return null;
    }

    public static <T> T valueOrDefault(T value, T defaultValue) {
        return value == null ? defaultValue : value;
    }
}
