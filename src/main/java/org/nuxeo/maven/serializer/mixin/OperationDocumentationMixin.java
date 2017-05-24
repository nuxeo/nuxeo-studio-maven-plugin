package org.nuxeo.maven.serializer.mixin;

import org.nuxeo.ecm.automation.core.OperationChainContribution;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class OperationDocumentationMixin {
    @JsonIgnore
    public String deprecatedSince;

    @JsonIgnore
    public boolean addToStudio;

    @JsonIgnore
    public String url;

    @JsonIgnore
    public String implementationClass;

    @JsonIgnore
    public abstract boolean isChain();

    @JsonIgnore
    public abstract OperationChainContribution.Operation[] getOperations();
}
