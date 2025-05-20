/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.javacc.mojo;

import java.io.File;
import junit.framework.TestCase;

/** Tests {@link JTB} facade. */
public class JTBTest extends TestCase {

  /** Tests {@link JTB#JTB()} and {@link JTB#toString()}. */
  public void testToStringNullSafe() {

    final JTB tool = new JTB();
    final String string = tool.toString();
    assertNotNull(string);
    assertTrue(string.indexOf("null") < 0);
  }

  /** Tests {@link JTB} setters. */
  public void testSettersNullSafe() {

    final JTB tool = new JTB();
    tool.setInputFile(null);
    tool.setOutputDirectory(null);
    tool.setNodeDirectory(null);
    tool.setVisitorDirectory(null);
    tool.setDescriptiveFieldNames(null);
    tool.setJavadocFriendlyComments(null);
    tool.setNodePackageName(null);
    tool.setNodeParentClass(null);
    tool.setPackageName(null);
    tool.setParentPointers(null);
    tool.setPrinter(null);
    tool.setScheme(null);
    tool.setSpecialTokens(null);
    tool.setSupressErrorChecking(null);
    tool.setVisitorPackageName(null);
    tool.setLog(null);
  }

  /** Tests {@link JTB#getOutputFile()}. */
  public void testGetOutputFile() {

    final File input = new File("Test.jtb").getAbsoluteFile();
    final File outdir = new File("dir").getAbsoluteFile();

    final JTB tool = new JTB();
    tool.setInputFile(input);
    tool.setOutputDirectory(outdir);
    final File output = tool.getOutputFile();

    assertEquals(new File(outdir, "Test.jj"), output);
  }
}
