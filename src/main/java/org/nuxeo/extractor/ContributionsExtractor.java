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

package org.nuxeo.extractor;

import java.io.IOException;

import org.nuxeo.extractor.bundle.ContributionsHolder;
import org.nuxeo.extractor.publisher.Publisher;
import org.nuxeo.extractor.serializer.StudioSerializer;

public class ContributionsExtractor {
    private ExtractorOptions opts;

    public ContributionsExtractor(ExtractorOptions opts) {
        this.opts = opts;
    }

    public void publish() throws IOException {
        // Load contributions from Opts object, depending if a jar is setted or directories
        ContributionsHolder holder = new ContributionsHolder();
        new ContributionsLoader(holder, opts).load();

        String[] targets = "*".equals(opts.getExtract()) ? holder.getManager().getRegisteredTargets()
                : opts.getExtract().split(",|\\s\\s*");
        StudioSerializer serializer = new StudioSerializer(holder, opts);

        //
        Publisher.instance(serializer, opts).publish(targets);
    }
}
