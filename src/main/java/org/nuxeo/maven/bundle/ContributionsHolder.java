package org.nuxeo.maven.bundle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.nuxeo.maven.mapper.MappersManager;
import org.nuxeo.maven.mapper.impl.AutomationMapper;
import org.nuxeo.maven.mapper.impl.DoctypeMapper;
import org.nuxeo.maven.mapper.impl.PermissionsMapper;
import org.nuxeo.runtime.model.RegistrationInfo;

/**
 * Contributions Holder is aim to map and track all visited contributions.
 * </p>
 * This Holder is based on a {@link MappersManager} to map all known contributions to his Descriptor object.
 */
public class ContributionsHolder {
    protected final Map<String, List<Object>> contributions = new HashMap<>();

    protected final MappersManager mapper;

    public ContributionsHolder() {
        mapper = new MappersManager();
        mapper.register(new DoctypeMapper());
        mapper.register(new PermissionsMapper());
        mapper.register(new AutomationMapper());
    }

    public MappersManager getMapper() {
        return mapper;
    }

    public void load(RegistrationInfo ri) {
        Arrays.stream(ri.getExtensions())
              .map(mapper::load)
              .filter(Objects::nonNull)
              .forEach(a -> Arrays.stream(a).forEach(c -> {
                  List<Object> sortedContributions = contributions.computeIfAbsent(c.getClass().getName(),
                          s -> new ArrayList<>());
                  sortedContributions.add(c);
              }));
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> getContributions(Class<T> descriptor) {
        return (List<T>) contributions.getOrDefault(descriptor.getName(), Collections.emptyList());
    }
}
