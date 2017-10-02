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

package org.nuxeo.maven.serializer.adapter;

import org.nuxeo.ecm.core.schema.SchemaBindingDescriptor;
import org.nuxeo.ecm.core.schema.SchemaManagerImpl;
import org.nuxeo.ecm.core.schema.XSDLoader;
import org.nuxeo.ecm.core.schema.types.SchemaImpl;
import org.nuxeo.ecm.core.schema.types.TypeException;
import org.nuxeo.ecm.core.schema.types.resolver.ObjectResolverService;
import org.nuxeo.maven.runtime.MojoRuntime;
import org.xml.sax.SAXException;

public class SchemaAdapter implements SerializerAdapter<SchemaBindingDescriptor, SchemaImpl> {
    @Override
    public SchemaImpl adapt(SchemaBindingDescriptor descriptor) {
        return loadSchema(descriptor);
    }

    protected static SchemaImpl loadSchema(SchemaBindingDescriptor descriptor) {
        try {
            return new CustomXSDLoader(MojoRuntime.schemaManager, descriptor).loadSchema();
        } catch (SAXException | TypeException e) {
            throw new RuntimeException("Unable to adapt schema binding: " + descriptor.name, e);
        }
    }

    /**
     * Custom XSD Loader that do not depends on Nuxeo Framework
     */
    public static class CustomXSDLoader extends XSDLoader {
        public CustomXSDLoader(SchemaManagerImpl schemaManager, SchemaBindingDescriptor sd) {
            super(schemaManager, sd);
            sd.context = MojoRuntime.instance;
        }

        @Override
        protected ObjectResolverService getObjectResolverService() {
            return (s, map) -> null;
        }

        public SchemaImpl loadSchema() throws TypeException, SAXException {
            return (SchemaImpl) loadSchema(sd.name, sd.prefix, MojoRuntime.instance.getLocalResource(sd.src));
        }
    }
}
