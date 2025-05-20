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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import junit.framework.TestCase;

/** Tests {@link GrammarInfo}. */
public class GrammarInfoTest extends TestCase {

  /** Tests {@link GrammarInfo#GrammarInfo(File, String)} on non existing file. */
  public void testInvalidFile() {
    try {
      new GrammarInfo(new File("").getAbsoluteFile(), "bad");
      fail("Missing IO exception");
    } catch (final IOException e) {
      // expected
    }
  }

  /**
   * Tests {@link GrammarInfo#GrammarInfo(File, String)} on existing file.
   *
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  public void testGetGrammarFile() throws IOException, URISyntaxException {
    final File grammarFile = getGrammar("Parser1.jj");
    final GrammarInfo info = new GrammarInfo(grammarFile.getParentFile(), grammarFile.getName());
    assertEquals(grammarFile, info.getGrammarFile());
  }

  /**
   * Tests {@link GrammarInfo#getRelativeGrammarFile()}.
   *
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  public void testGetRelativeGrammarFile() throws IOException, URISyntaxException {
    final File grammarFile = getGrammar("Parser1.jj");
    final GrammarInfo info = new GrammarInfo(grammarFile.getParentFile(), grammarFile.getName());
    assertEquals(grammarFile.getName(), info.getRelativeGrammarFile());
  }

  /**
   * Tests {@link GrammarInfo#GrammarInfo(Suffix, File, String, String)} and {@link
   * GrammarInfo#getParserPackage()} with an overwritten package name.
   *
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  public void testGetPackageNameDeclaredPackageOverwrite() throws IOException, URISyntaxException {
    final File grammarFile = getGrammar("Parser1.jj");
    final GrammarInfo info =
        new GrammarInfo(
            Suffix.Java, grammarFile.getParentFile(), grammarFile.getName(), "org.test");
    assertEquals("org.test", info.getParserPackage());
  }

  /**
   * Tests {@link GrammarInfo#getParserPackage()} with a declared package name.
   *
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  public void testGetPackageNameDeclaredPackage() throws IOException, URISyntaxException {
    final GrammarInfo info = newGrammarInfo("Parser1.jj");
    assertEquals("org.codehaus.mojo.javacc.test", info.getParserPackage());
  }

  /**
   * Tests {@link GrammarInfo#getParserPackage()} with a default package name.
   *
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  public void testGetPackageNameDefaultPackage() throws IOException, URISyntaxException {
    final GrammarInfo info = newGrammarInfo("Parser2.jj");
    assertEquals("", info.getParserPackage());
  }

  /**
   * Tests {@link GrammarInfo#GrammarInfo(File, String)} with a declared directory name.
   *
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  public void testGetPackageDirectoryDeclaredPackage() throws IOException, URISyntaxException {
    final GrammarInfo info = newGrammarInfo("Parser1.jj");
    assertEquals(new File("org/codehaus/mojo/javacc/test").getPath(), info.getParserDirectory());
  }

  /**
   * Tests {@link GrammarInfo#GrammarInfo(File, String)} with a default directory name.
   *
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  public void testGetPackageDirectoryDefaultPackage() throws IOException, URISyntaxException {
    final GrammarInfo info = newGrammarInfo("Parser2.jj");
    assertEquals(new File("").getPath(), info.getParserDirectory());
  }

  /**
   * Tests {@link GrammarInfo#getParserName()}.
   *
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  public void testGetParserName() throws IOException, URISyntaxException {
    final GrammarInfo info = newGrammarInfo("Parser1.jj");
    assertEquals("BasicParser", info.getParserName());
  }

  /**
   * Tests {@link GrammarInfo#getParserFile()} with the declared package name.
   *
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  public void testGetParserFileDeclaredPackage() throws IOException, URISyntaxException {
    final GrammarInfo info = newGrammarInfo("Parser1.jj");
    assertEquals(
        new File("org/codehaus/mojo/javacc/test/BasicParser.java").getPath(), info.getParserFile());
  }

  /**
   * Tests {@link GrammarInfo#getParserFile()} with no declared package name.
   *
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  public void testGetParserFileDefaultPackage() throws IOException, URISyntaxException {
    final GrammarInfo info = newGrammarInfo("Parser2.jj");
    assertEquals(new File("SimpleParser.java").getPath(), info.getParserFile());
  }

  /**
   * Tests {@link GrammarInfo#resolvePackageName(String)} against different arguments patterns with
   * the declared package name.
   *
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  public void testResolvePackageNameDeclaredPackage() throws IOException, URISyntaxException {
    final GrammarInfo info = newGrammarInfo("Parser1.jj");
    assertEquals("org.codehaus.mojo.javacc.test.node", info.resolvePackageName("*.node"));
    assertEquals("org.codehaus.mojo.javacc.testnode", info.resolvePackageName("*node"));
    assertEquals("node", info.resolvePackageName("node"));
  }

  /**
   * Tests {@link GrammarInfo#resolvePackageName(String)} against different arguments patterns with
   * no declared package name.
   *
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  public void testResolvePackageNameDefaultPackage() throws IOException, URISyntaxException {
    final GrammarInfo info = newGrammarInfo("Parser2.jj");
    assertEquals("node", info.resolvePackageName("*.node"));
    assertEquals("node", info.resolvePackageName("*node"));
    assertEquals("node", info.resolvePackageName("node"));
  }

  /**
   * Creates a new {@link GrammarInfo} instance.
   *
   * @param resource the resource
   * @return an new instance of {@link GrammarInfo}
   * @throws IOException on non existing file
   * @throws URISyntaxException if the resource cannot be converted to a File
   */
  private GrammarInfo newGrammarInfo(final String resource) throws IOException, URISyntaxException {
    final File grammarFile = getGrammar(resource);
    final File sourceDir = grammarFile.getParentFile();
    return new GrammarInfo(sourceDir, grammarFile.getName());
  }

  /**
   * Gets the File from a given resource.
   *
   * @param resource the resource
   * @return the File
   * @throws URISyntaxException if malformed URI
   */
  private File getGrammar(final String resource) throws URISyntaxException {
    return new File(new URI(getClass().getResource('/' + resource).toString()));
  }
}
