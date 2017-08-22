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

import static org.nuxeo.ecm.automation.core.Constants.T_BLOB;
import static org.nuxeo.ecm.automation.core.Constants.T_BLOBS;
import static org.nuxeo.ecm.automation.core.Constants.T_DOCUMENT;
import static org.nuxeo.ecm.automation.core.Constants.T_DOCUMENTS;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.nuxeo.ecm.automation.OperationChain;
import org.nuxeo.ecm.automation.OperationDocumentation;
import org.nuxeo.ecm.automation.OperationException;
import org.nuxeo.ecm.automation.core.OperationChainContribution;
import org.nuxeo.ecm.automation.core.impl.ChainTypeImpl;

public class OperationChainAdapter implements SerializerAdapter<OperationChainContribution, OperationDocumentation> {
    @Override
    public OperationDocumentation adapt(OperationChainContribution src) {
        try {
            ChainTypeImpl impl = new CustomChainTypeImpl(src.toOperationChain(null), src);
            return impl.getDocumentation();
        } catch (OperationException e) {
            throw new RuntimeException(e);
        }
    }

    public static class CustomChainTypeImpl extends ChainTypeImpl {
        private static final List<String> DEFAULT_SIGNATURE;

        static {
            DEFAULT_SIGNATURE = new ArrayList<>();
            List<String> possibleValues = Arrays.asList(T_DOCUMENTS, T_DOCUMENT, T_BLOB, T_BLOBS, "void");
            possibleValues.forEach(inputType -> possibleValues.forEach(outputType -> {
                DEFAULT_SIGNATURE.add(inputType);
                DEFAULT_SIGNATURE.add(outputType);
            }));
        }

        public CustomChainTypeImpl(OperationChain chain, OperationChainContribution contribution) {
            super(null, chain, contribution);
        }

        @Override
        protected ArrayList<String> getSignature(OperationChainContribution.Operation[] operations)
                throws OperationException {
            // XXX Operation Signature requires to have access to the class; as signature is computed by introspection.
            // For now, assuming all possible signatures. *sik*
            return new ArrayList<>(DEFAULT_SIGNATURE);
        }
    }
}
