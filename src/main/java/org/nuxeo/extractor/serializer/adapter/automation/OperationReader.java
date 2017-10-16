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
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.getOperationMethodTypes;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.getParamDocumentationType;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.readAnnoMemberBoolean;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.readAnnoMemberClass;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.readAnnoMemberInteger;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.readAnnoMemberString;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.readAnnoMemberStringArray;
import static org.nuxeo.extractor.serializer.adapter.automation.OperationReflectionHelper.valueOrDefault;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.nuxeo.extractor.runtime.ExtractorContext;
import org.nuxeo.extractor.serializer.adapter.automation.OperationDocumentation.Param;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import javassist.NotFoundException;
import javassist.bytecode.annotation.Annotation;

public class OperationReader {

    final public static String PARAM_ANNOTATION_TYPE = "org.nuxeo.ecm.automation.core.annotations.Param";

    final public static String OPERATION_ANNOTATION_TYPE = "org.nuxeo.ecm.automation.core.annotations.Operation";

    final public static String METHOD_ANNOTATION_TYPE = "org.nuxeo.ecm.automation.core.annotations.OperationMethod";

    private final CtClass ctClass;

    private String id;

    private String category;

    private String requires;

    private String since;

    private String label;

    private String description;

    private String deprecatedSince;

    private Boolean addToStudio;

    private String[] aliases;

    private List<Param> parameters;

    private List<String> methods;

    public OperationReader(String className) {
        try {
            ClassPool aDefault = ClassPool.getDefault();
            aDefault.insertClassPath(new LoaderClassPath(ExtractorContext.getClassloader()));

            ctClass = aDefault.get(className);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public OperationReader readAll() {
        // Compute all fields
        readOperationAnnotation();
        readOperationParams();
        readOperationMethods();
        return this;
    }

    public static OperationReader read(String className) {
        return new OperationReader(className).readAll();
    }

    public void readOperationAnnotation() {
        Annotation anno = findAnnotation(ctClass, OPERATION_ANNOTATION_TYPE);
        if (anno == null) {
            throw new RuntimeException("Unable to find Operation annotation in class " + ctClass.getName());
        }

        id = readAnnoMemberString(anno, "id");
        category = readAnnoMemberString(anno, "category");
        label = readAnnoMemberString(anno, "label");
        requires = readAnnoMemberString(anno, "requires");
        description = readAnnoMemberString(anno, "description");
        since = readAnnoMemberString(anno, "since");
        deprecatedSince = readAnnoMemberString(anno, "deprecatedSince");
        addToStudio = readAnnoMemberBoolean(anno, "addToStudio");
        aliases = readAnnoMemberStringArray(anno, "aliases");
    }

    public void readOperationParams() {
        parameters = new ArrayList<>();

        CtField[] fields = ctClass.getFields();
        Arrays.stream(fields) //
              .map(field -> {
                  Annotation anno = findAnnotation(field, PARAM_ANNOTATION_TYPE);
                  if (anno == null) {
                      return null;
                  }

                  Param param = new Param();
                  param.name = readAnnoMemberString(anno, "name");

                  String description = readAnnoMemberString(anno, "description");
                  param.description = valueOrDefault(description, "");
                  Boolean isRequired = readAnnoMemberBoolean(anno, "required");
                  param.required = valueOrDefault(isRequired, true);
                  String[] values = readAnnoMemberStringArray(anno, "values");
                  param.values = valueOrDefault(values, new String[] {});
                  Integer order = readAnnoMemberInteger(anno, "order");
                  param.order = valueOrDefault(order, 0);
                  param.type = getParamDocumentationType(field.getSignature());

                  return param;
              })
              .filter(Objects::nonNull)
              .forEach(parameters::add);

        Collections.sort(parameters);
    }

    public void readOperationMethods() {
        methods = new ArrayList<>();

        CtMethod[] ctMethods = ctClass.getMethods();
        Arrays.stream(ctMethods) //
              .map(m -> {
                  Annotation anno = findAnnotation(m, METHOD_ANNOTATION_TYPE);
                  if (anno == null) {
                      return null;
                  }

                  boolean isIterable = readAnnoMemberClass(anno, "collector") != null;
                  return getOperationMethodTypes(m.getSignature(), isIterable);
              })
              .filter(StringUtils::isNotBlank)
              .forEach(methods::add);

        Collections.sort(methods);
    }

    public String getId() {
        return valueOrDefault(id, "");
    }

    public String getLabel() {
        return valueOrDefault(label, getId());
    }

    public String getRequires() {
        return valueOrDefault(requires, null);
    }

    public String getCategory() {
        return valueOrDefault(category, "Others");
    }

    public String getSince() {
        return valueOrDefault(since, "");
    }

    public String getDescription() {
        return valueOrDefault(description, "");
    }

    public String getDeprecatedSince() {
        return valueOrDefault(deprecatedSince, "");
    }

    public Boolean getAddToStudio() {
        return valueOrDefault(addToStudio, true);
    }

    public String[] getAliases() {
        return valueOrDefault(aliases, new String[] {});
    }

    /**
     * Return Operation Chain signature format: {@code "parameter:return"}
     *
     * @return a list of methods signatures
     */
    public List<String> getMethods() {
        return new ArrayList<>(methods);
    }

    public String[] getSplittedSignatures() {
        List<String> signature = new ArrayList<>(methods.size() * 2);
        for (String method : methods) {
            Collections.addAll(signature, method.split(":"));
        }

        return signature.toArray(new String[0]);
    }

    public List<Param> getParameters() {
        return new ArrayList<>(parameters);
    }

}
