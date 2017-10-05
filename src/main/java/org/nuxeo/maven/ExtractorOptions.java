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

package org.nuxeo.maven;

import static org.nuxeo.common.Environment.NUXEO_HOME;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.nuxeo.maven.bundle.FakeRuntimeService;
import org.nuxeo.runtime.api.Framework;

public class ExtractorOptions {
    // XXX MUST BE MOVED SOMEWHERE ELSE
    static {
        // Fake Nuxeo Runtime initialization
        try {
            System.setProperty(NUXEO_HOME, Files.createTempDirectory("nuxeo").toString());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        Framework.initialize(new FakeRuntimeService());
    }

    public static ExtractorOptions DEFAULT = new ExtractorOptions();

    protected String token = "";

    protected String symbolicName = "";

    protected String connectUrl = "";

    protected String buildDirectory = Paths.get("").toAbsolutePath().toString();

    protected String output = "";

    protected boolean failOnEmpty = false;

    private String extract;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getSymbolicName() {
        return symbolicName;
    }

    public void setSymbolicName(String symbolicName) {
        this.symbolicName = symbolicName;
    }

    public String getConnectUrl() {
        return connectUrl;
    }

    public void setConnectUrl(String connectUrl) {
        this.connectUrl = connectUrl;
    }

    public String getBuildDirectory() {
        return buildDirectory;
    }

    public void setBuildDirectory(String buildDirectory) {
        this.buildDirectory = buildDirectory;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    public boolean isFailOnEmpty() {
        return failOnEmpty;
    }

    public String getExtract() {
        return extract;
    }

    public void setExtract(String extract) {
        this.extract = extract;
    }
}
