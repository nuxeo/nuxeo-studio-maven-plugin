package org.nuxeo.maven.serializer;

import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.nuxeo.ecm.automation.core.OperationContribution;
import org.nuxeo.ecm.core.schema.FacetDescriptor;
import org.nuxeo.maven.bundle.ContributionsHolder;

public class StudioSerializer {

    private static final List<Class<?>> asArray = Arrays.asList(FacetDescriptor.class, OperationContribution.class);

    private ContributionsHolder holder;

    public StudioSerializer(ContributionsHolder holder) {
        this.holder = holder;
    }

    public void serializeInto(OutputStream os, String[] targets) {
        Map<String, String> serialized = new HashMap<>();
        Arrays.stream(targets).forEach(t -> serialized.put(t, this.serializeAll(t)));

        JacksonSerializer.instance.newGlobalStudioObject(os, serialized);
    }

    public String serializeAll(String name) {
        return serializeAll(holder.getMapper().getDescriptor(name));
    }

    public String serializeAll(Class<?> descriptor) {
        final String delimiter = getDelimiter();
        final String prefix = getPrefix(descriptor);
        final String suffix = getSuffix(descriptor);

        return holder.getContributions(descriptor)
                     .stream()
                     .map(this::serialize)
                     .collect(Collectors.joining(delimiter, prefix, suffix));
    }

    protected String serialize(Object obj) {
        return JacksonSerializer.instance.serialize(obj);
    }

    public String getDelimiter() {
        return ",";
    }

    public String getPrefix(Class<?> descriptor) {
        return asArray.contains(descriptor) ? "[" : "{";
    }

    public String getSuffix(Class<?> descriptor) {
        return asArray.contains(descriptor) ? "]" : "}";
    }
}
