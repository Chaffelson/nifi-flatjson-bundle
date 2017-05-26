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

import org.apache.nifi.util.MockFlowFile;
import org.apache.nifi.util.TestRunner;
import org.apache.nifi.util.TestRunners;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.matchers.Equals;

import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;


public class FlatJsonTest {

    private TestRunner testRunner;

    @Before
    public void init() {
        testRunner = TestRunners.newTestRunner(FlatJson.class);
    }

    @Test
    public void testFlatJson() throws Exception {
        final TestRunner runner = TestRunners.newTestRunner(FlatJson.class);

        runner.enqueue(Paths.get("src/test/resources/SampleFile.json"));
        runner.run();

        runner.assertAllFlowFilesTransferred(FlatJson.REL_SUCCESS, 1);
        MockFlowFile whatever = runner.getFlowFilesForRelationship(FlatJson.REL_SUCCESS).get(0);
        String flowFileContent = new String(whatever.toByteArray(), StandardCharsets.UTF_8);
        assertEquals("{\"a.b\":1,\"a.c\":null,\"a.d[0]\":false,\"a.d[1]\":true,\"e\":\"f\",\"g\":2.3}", flowFileContent);
    }

}
