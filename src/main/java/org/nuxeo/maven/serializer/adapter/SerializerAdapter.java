package org.nuxeo.maven.serializer.adapter;

public interface SerializerAdapter<T, V> {
    V adapt(T src);
}
