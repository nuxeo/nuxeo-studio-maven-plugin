package org.nuxeo.maven.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.nuxeo.maven.runtime.MojoRuntime;
import org.nuxeo.runtime.model.Extension;

/**
 * Mappers Manager holding all registered {@link ExtensionMapper} and can load an {@link Extension} to his Descriptor
 * class using {@link org.nuxeo.common.xmap.XMap} loader.
 */
public class MappersManager {
    private List<ExtensionMapper> mappers;

    public MappersManager() {
        mappers = new ArrayList<>();
    }

    public void register(ExtensionMapper transformer) {
        mappers.add(transformer);
    }

    public boolean accept(Extension ext) {
        return mappers.stream().anyMatch(s -> s.accept(ext));
    }

    public Class<?> getDescriptor(String name) {
        return mappers.stream().map(s -> s.getDescriptor(name)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public Object[] load(Extension ext) {
        ExtensionMapper mapper = mappers.stream().filter(s -> s.accept(ext)).findFirst().orElse(null);

        if (mapper != null) {
            return mapper.loadAll(MojoRuntime.instance, ext);
        } else {
            return null;
        }
    }
}
