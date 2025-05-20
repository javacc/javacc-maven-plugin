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

/** Tests {@link JJTree} facade. */
public class JJTreeTest extends TestCase {

  /** Tests {@link JJTree#JJTree()} and {@link JJTree#toString()}. */
  public void testToStringNullSafe() {
    final JJTree tool = new JJTree();
    final String string = tool.toString();
    assertNotNull(string);
    assertTrue(string.indexOf("null") < 0);
  }

  /** Tests {@link JJTree} setters. */
  public void testSettersNullSafe() {
    final JJTree tool = new JJTree();
    tool.setInputFile(null);
    tool.setOutputDirectory(null);
    //        tool.setJdkVersion( null );
    tool.setStatic(null);
    tool.setBuildNodeFiles(null);
    tool.setMulti(null);
    tool.setNodeDefaultVoid(null);
    tool.setNodeFactory(null);
    tool.setNodePackage(null);
    tool.setNodePrefix(null);
    tool.setNodeScopeHook(null);
    tool.setNodeUsesParser(null);
    tool.setVisitor(null);
    tool.setVisitorException(null);
    tool.setLog(null);
  }

  /** Tests {@link JJTree#getOutputFile()}. */
  public void testGetOutputFile() {
    final File input = new File("Test.jjt").getAbsoluteFile();
    final File outdir = new File("dir").getAbsoluteFile();

    final JJTree tool = new JJTree();
    tool.setInputFile(input);
    tool.setOutputDirectory(outdir);
    final File output = tool.getOutputFile();

    assertEquals(new File(outdir, "Test.jj"), output);
  }
}
