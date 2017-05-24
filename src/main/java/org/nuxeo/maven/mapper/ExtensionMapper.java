package org.nuxeo.maven.mapper;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.nuxeo.common.xmap.XMap;
import org.nuxeo.runtime.model.Extension;
import org.nuxeo.runtime.model.RuntimeContext;
import org.nuxeo.runtime.model.impl.XMapContext;

public abstract class ExtensionMapper {

    protected Map<String, Class<?>> descriptors = new HashMap<>();

    public ExtensionMapper() {
        registerDescriptors();
    }

    public abstract void registerDescriptors();

    protected abstract boolean accept(String target, String point);

    public void registerDescriptor(String name, Class<?> descriptor) {
        descriptors.put(name, descriptor);
    }

    public boolean accept(Extension ext) {
        return accept(ext.getTargetComponent().getName(), ext.getExtensionPoint());
    }

    public Object[] loadAll(RuntimeContext ctx, Extension extension) {
        XMap xmap = new XMap();
        Arrays.stream(getDescriptors()).forEach(xmap::register);
        return xmap.loadAll(new XMapContext(ctx), extension.getElement());
    }

    public Class<?>[] getDescriptors() {
        return descriptors.values().toArray(new Class[0]);
    }

    public Class<?> getDescriptor(String name) {
        return descriptors.getOrDefault(name, null);
    }
}
