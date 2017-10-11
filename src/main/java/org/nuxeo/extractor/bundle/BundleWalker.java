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

package org.nuxeo.extractor.bundle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.jar.Manifest;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.extractor.runtime.ExtractorRuntimeContext;
import org.nuxeo.runtime.model.RegistrationInfo;
import org.nuxeo.runtime.model.impl.ComponentDescriptorReader;

public class BundleWalker {

    private static Log log = LogFactory.getLog(BundleWalker.class);

    private Path basePath;

    private ComponentDescriptorReader reader;

    public BundleWalker() {
        reader = new ComponentDescriptorReader();
    }

    public BundleWalker(String basePath) {
        this(new File(basePath));
    }

    public BundleWalker(File basePath) {
        this();
        setBasePath(basePath);
    }

    public BundleWalker(Path basePath) {
        this();
        this.basePath = basePath;
    }

    private Path findFile(String filePath) {
        try {
            return Files.walk(basePath).filter(s -> s.endsWith(filePath)).findFirst().orElse(null);
        } catch (IOException e) {
            log.debug(e);
            log.warn(filePath + ":" + e.getMessage());
            return null;
        }
    }

    public Stream<Path> getComponents() throws IOException {
        Path manifestPath = getManifest();
        if (manifestPath == null) {
            log.info(String.format("%s do no contains MANIFEST.MF file", basePath.toAbsolutePath().toString()));
            return Stream.empty();
        }

        Manifest manifest;
        try (InputStream fis = Files.newInputStream(manifestPath)) {
            manifest = new Manifest(fis);
        }

        String components = manifest.getMainAttributes().getValue("Nuxeo-Component");
        if (StringUtils.isBlank(components)) {
            return Stream.empty();
        }

        return Arrays.stream(components.split("[, \t\n\r\f]+"))
                     .filter(StringUtils::isNotBlank)
                     .map(this::findFile)
                     .filter(Objects::nonNull);
    }

    public Path getManifest() {
        return findFile("META-INF/MANIFEST.MF");
    }

    public Stream<RegistrationInfo> getRegistrationInfos() throws IOException {
        return getComponents().map(this::read).filter(Objects::nonNull);
    }

    public RegistrationInfo read(Path component) {
        try (InputStream is = Files.newInputStream(component)) {
            return reader.read(ExtractorRuntimeContext.instance, is);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void setBasePath(File basePath) {
        if (basePath != null) {
            this.basePath = basePath.toPath();
        } else {
            this.basePath = null;
        }
    }
}
