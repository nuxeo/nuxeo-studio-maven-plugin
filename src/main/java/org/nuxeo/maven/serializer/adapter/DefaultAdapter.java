package org.nuxeo.maven.serializer.adapter;

public class DefaultAdapter implements SerializerAdapter<Object, Object> {
    @Override
    public Object adapt(Object src) {
        return src;
    }
}
