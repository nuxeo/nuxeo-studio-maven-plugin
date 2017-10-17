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

package org.nuxeo.extractor.runtime;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.extractor.mapper.xmap.Context;

/**
 * Simple Fake {@link Context} that loads all child's class path (Maven compile resolution scope) elements to a custom
 * {@link ClassLoader}. It is required, in order to be able to resolve external Type from dependencies.
 * <p>
 * Runtime will load types from current class loader if {@link ExtractorContext#initCustomClassLoader(java.util.Set)}
 * has never been initialized.
 * </p>
 */
public class ExtractorContext extends Context {
    private static final Log log = LogFactory.getLog(ExtractorContext.class);

    public static ExtractorContext instance = new ExtractorContext();

    private static ClassLoader custom;

    private Set<URI> extResourcesSources = new HashSet<>();

    private ExtractorContext() {
    }

    /**
     * Add a custom class loader based on {@code project} parameter. It loads current classes and project child's
     * classes.
     *
     * @param additionalUrls Additional urls as string to enhance current UrlClassLoader
     * @throws IOException When unable to reach an url passed as parameter
     */
    public static void initCustomClassLoader(Set<String> additionalUrls) throws IOException {
        List<URL> urlElements = new ArrayList<>();

        for (String s : additionalUrls) {
            URI uri = URI.create(s);
            URL url;
            if (StringUtils.isBlank(uri.getScheme())) {
                url = new File(s).toURI().toURL();
            } else {
                // Without using a FS, scheme like jar:file are not easily exportable as URL
                try (FileSystem fs = FileSystems.newFileSystem(uri, new HashMap<>())) {
                    url = fs.getPath("").toUri().toURL();
                }
            }

            urlElements.add(url);
        }

        custom = new URLClassLoader(urlElements.toArray(new URL[0]), getClassloader());
    }

    public static ClassLoader getClassloader() {
        if (custom != null) {
            return custom;
        } else {
            return Thread.currentThread().getContextClassLoader();
        }
    }

    public void addExternalSource(URI source) {
        try {
            initCustomClassLoader(Collections.singleton(source.toString()));
        } catch (IOException e) {
            log.warn(e, e);
        }
        extResourcesSources.add(source);
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        return getClassloader().loadClass(className);
    }

    @Override
    public URL getResource(String name) {
        URL loadedResource = getClassloader().getResource(name);
        if (loadedResource != null) {
            return loadedResource;
        }

        try {
            File file = new File(name);
            if (file.exists()) {
                return file.toURI().toURL();
            }
        } catch (MalformedURLException e) {
            // Should never happen
        }

        return extResourcesSources.stream()
                                  .map(s -> this.getResourceFromFile(s, name))
                                  .filter(Objects::nonNull)
                                  .findFirst()
                                  .orElse(null);
    }

    public URL getResourceFromFile(URI uri, String name) {
        try {
            try (FileSystem fs = FileSystems.newFileSystem(uri, new HashMap<>())) {
                Path path = fs.getPath(name);

                if (Files.exists(path)) {
                    return path.toUri().toURL();
                }
            }
        } catch (IOException e) {
            // Ignore
        }

        return null;
    }

    public void clearExternalSources() {
        extResourcesSources.clear();
    }
}
