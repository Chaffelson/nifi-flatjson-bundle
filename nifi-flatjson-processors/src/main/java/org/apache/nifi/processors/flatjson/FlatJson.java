/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.nifi.processors.flatjson;

import com.github.wnameless.json.flattener.JsonFlattener;
import com.github.wnameless.json.flattener.PrintMode;
import org.apache.commons.io.Charsets;
import org.apache.nifi.components.PropertyDescriptor;
import org.apache.nifi.flowfile.FlowFile;
import org.apache.nifi.annotation.behavior.ReadsAttribute;
import org.apache.nifi.annotation.behavior.ReadsAttributes;
import org.apache.nifi.annotation.behavior.WritesAttribute;
import org.apache.nifi.annotation.behavior.WritesAttributes;
import org.apache.nifi.annotation.lifecycle.OnScheduled;
import org.apache.nifi.annotation.documentation.CapabilityDescription;
import org.apache.nifi.annotation.documentation.SeeAlso;
import org.apache.nifi.annotation.documentation.Tags;
import org.apache.nifi.processor.exception.ProcessException;
import org.apache.nifi.processor.AbstractProcessor;
import org.apache.nifi.processor.ProcessContext;
import org.apache.nifi.processor.ProcessSession;
import org.apache.nifi.processor.ProcessorInitializationContext;
import org.apache.nifi.processor.Relationship;
import org.apache.nifi.processor.io.StreamCallback;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.nifi.processor.util.StandardValidators;

@Tags({"json"})
@CapabilityDescription("Procedurally Flattens or un-Flattens arbitrary JSON documents")
@ReadsAttributes({@ReadsAttribute(attribute="", description="")})
@WritesAttributes({@WritesAttribute(attribute="", description="")})
public class FlatJson extends AbstractProcessor {

    public static final PropertyDescriptor CHARSET = new PropertyDescriptor.Builder()
            .name("Character Set")
            .description("Specifies the character set of the received data.")
            .required(true)
            .defaultValue("UTF-8")
            .expressionLanguageSupported(true)
            .addValidator(StandardValidators.CHARACTER_SET_VALIDATOR)
            .build();

    public static final Relationship REL_SUCCESS = new Relationship.Builder()
            .name("success")
            .description("Successful Activity")
            .build();

    public static final Relationship REL_FAILURE = new Relationship.Builder()
            .name("failure")
            .description("Failed Activity")
            .build();

    private List<PropertyDescriptor> descriptors;

    private Set<Relationship> relationships;

    @Override
    protected void init(final ProcessorInitializationContext context) {
        final List<PropertyDescriptor> descriptors = new ArrayList<PropertyDescriptor>();
        descriptors.add(CHARSET);
        this.descriptors = Collections.unmodifiableList(descriptors);

        final Set<Relationship> relationships = new HashSet<Relationship>();
        relationships.add(REL_SUCCESS);
        relationships.add(REL_FAILURE);
        this.relationships = Collections.unmodifiableSet(relationships);
    }

    @Override
    public Set<Relationship> getRelationships() {
        return this.relationships;
    }

    @Override
    public final List<PropertyDescriptor> getSupportedPropertyDescriptors() {
        return descriptors;
    }

    @OnScheduled
    public void onScheduled(final ProcessContext context) {

    }

    @Override
    public void onTrigger(final ProcessContext context, final ProcessSession session) throws ProcessException {
        FlowFile flowFile = session.get();
        if (flowFile == null) {
            return;
        }

        try {
            String charset = context.getProperty(CHARSET).evaluateAttributeExpressions().getValue();

            flowFile = session.write(flowFile, new StreamCallback() {
                @Override
                public void process(InputStream inputStream, OutputStream outputStream) throws IOException {
                    String inbound = IOUtils.toString(inputStream, charset);

                    String outbound = new JsonFlattener(inbound).withPrintMode(PrintMode.MINIMAL).flatten();

                    outputStream.write(outbound.getBytes(charset));
                }
            });
            session.transfer(flowFile, REL_SUCCESS);
        } catch (Exception e) {
            getLogger().error("Failed while processing a string", e);
            session.transfer(flowFile, REL_FAILURE);
        }
    }

}
