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
import java.net.MalformedURLException;
import java.net.URL;
import junit.framework.TestCase;

/** Tests {@link UrlUtils}. */
public class UrlUtilsTest extends TestCase {

  /**
   * Asserts a match of a given expected file against a given URL and a given resource.
   *
   * @param expectedFile the expected file name
   * @param url the URL string
   * @param resource the resource string
   * @throws MalformedURLException if the URL + resource cannot be converted to a file name
   */
  private void assertMatch(final String expectedFile, final String url, final String resource)
      throws MalformedURLException {
    assertEquals(new File(expectedFile), UrlUtils.getResourceRoot(new URL(url), resource));
  }

  /**
   * Tests {@link UrlUtils#getResourceRoot(URL, String)} against Windows file paths.
   *
   * @throws MalformedURLException if the URL + resource cannot be converted to a file name
   */
  public void testGetResourceRootFileWin() throws MalformedURLException {
    assertMatch("/C:/a dir", "file:/C:/a%20dir/org/Foo.class", "org/Foo.class");
    assertMatch("/C:/a dir", "file://localhost/C:/a%20dir/org/Foo.class", "org/Foo.class");
    assertMatch("/C:/a dir", "file:///C:/a%20dir/org/Foo.class", "org/Foo.class");
    assertMatch("/C:/a dir", "file:/C:/a%20dir/org/Foo.class", "/org/Foo.class");
    assertMatch("/C:/a dir", "file:/C:/a dir/org/Foo.class", "org/Foo.class");
  }

  /**
   * Tests {@link UrlUtils#getResourceRoot(URL, String)} against Windows jar paths.
   *
   * @throws MalformedURLException if the URL + resource cannot be converted to a file name
   */
  public void testGetResourceRootJarFileWin() throws MalformedURLException {
    assertMatch(
        "/C:/a dir/t-1.jar", "jar:file:/C:/a%20dir/t-1.jar!/org/Foo.class", "org/Foo.class");
    assertMatch(
        "/C:/a dir/t-1.jar",
        "jar:file://localhost/C:/a%20dir/t-1.jar!/org/Foo.class",
        "org/Foo.class");
    assertMatch(
        "/C:/a dir/t-1.jar", "jar:file:///C:/a%20dir/t-1.jar!/org/Foo.class", "org/Foo.class");
    assertMatch(
        "/C:/a dir/t-1.jar", "jar:file:/C:/a%20dir/t-1.jar!/org/Foo.class", "/org/Foo.class");
    assertMatch("/C:/a dir/t-1.jar", "jar:file:/C:/a dir/t-1.jar!/org/Foo.class", "org/Foo.class");
  }

  /**
   * Tests {@link UrlUtils#getResourceRoot(URL, String)} against Windows file unc.
   *
   * @throws MalformedURLException if the URL + resource cannot be converted to a file name
   */
  public void testGetResourceRootFileWinUnc() throws MalformedURLException {
    assertMatch("//host/a dir", "file:////host/a%20dir/org/Foo.class", "org/Foo.class");
  }

  /**
   * Tests {@link UrlUtils#getResourceRoot(URL, String)} against Windows jar unc.
   *
   * @throws MalformedURLException if the URL + resource cannot be converted to a file name
   */
  public void testGetResourceRootJarFileWinUnc() throws MalformedURLException {
    assertMatch(
        "//host/a dir/t-1.jar",
        "jar:file:////host/a%20dir/t-1.jar!/org/Foo.class",
        "org/Foo.class");
  }

  /**
   * Tests {@link UrlUtils#getResourceRoot(URL, String)} against Unix file paths.
   *
   * @throws MalformedURLException if the URL + resource cannot be converted to a file name
   */
  public void testGetResourceRootFileUnix() throws MalformedURLException {
    assertMatch("/home/a dir", "file:/home/a%20dir/org/Foo.class", "org/Foo.class");
    assertMatch("/home/a dir", "file://localhost/home/a%20dir/org/Foo.class", "org/Foo.class");
    assertMatch("/home/a dir", "file:///home/a%20dir/org/Foo.class", "org/Foo.class");
    assertMatch("/home/a dir", "file:/home/a%20dir/org/Foo.class", "/org/Foo.class");
    assertMatch("/home/a dir", "file:/home/a dir/org/Foo.class", "org/Foo.class");
  }

  /**
   * Tests {@link UrlUtils#getResourceRoot(URL, String)} against Unix jar paths.
   *
   * @throws MalformedURLException if the URL + resource cannot be converted to a file name
   */
  public void testGetResourceRootJarFileUnix() throws MalformedURLException {
    assertMatch(
        "/home/a dir/t-1.jar", "jar:file:/home/a%20dir/t-1.jar!/org/Foo.class", "org/Foo.class");
    assertMatch(
        "/home/a dir/t-1.jar",
        "jar:file://localhost/home/a%20dir/t-1.jar!/org/Foo.class",
        "org/Foo.class");
    assertMatch(
        "/home/a dir/t-1.jar", "jar:file:///home/a%20dir/t-1.jar!/org/Foo.class", "org/Foo.class");
    assertMatch(
        "/home/a dir/t-1.jar", "jar:file:/home/a%20dir/t-1.jar!/org/Foo.class", "/org/Foo.class");
    assertMatch(
        "/home/a dir/t-1.jar", "jar:file:/home/a dir/t-1.jar!/org/Foo.class", "org/Foo.class");
  }

  /** Tests {@link UrlUtils#getResourceRoot(URL, String)} against null args. */
  public void testGetResourceRootNullSafe() {
    assertNull(UrlUtils.getResourceRoot(null, ""));
  }

  /**
   * Tests {@link UrlUtils#getResourceRoot(URL, String)} against unknown protocol.
   *
   * @throws MalformedURLException if the URL + resource cannot be converted to a file name
   */
  public void testGetResourceRootUnknownProtocol() throws MalformedURLException {
    try {
      UrlUtils.getResourceRoot(new URL("http://www.foo.bar/index.html"), "index.html");
      fail("Missing runtime exception");
    } catch (final RuntimeException e) {
      assertTrue(true);
    }
  }

  /** Tests {@link UrlUtils#decodeUrl(String)} against different normal args. */
  public void testDecodeUrl() {
    assertEquals("", UrlUtils.decodeUrl(""));
    assertEquals("foo", UrlUtils.decodeUrl("foo"));
    assertEquals("+", UrlUtils.decodeUrl("+"));
    assertEquals("% ", UrlUtils.decodeUrl("%25%20"));
    assertEquals("%20", UrlUtils.decodeUrl("%2520"));
    assertEquals(
        "jar:file:/C:/dir/sub dir/1.0/foo-1.0.jar!/org/Bar.class",
        UrlUtils.decodeUrl("jar:file:/C:/dir/sub%20dir/1.0/foo-1.0.jar!/org/Bar.class"));
  }

  /** Tests {@link UrlUtils#decodeUrl(String)} against different unfrequent args. */
  public void testDecodeUrlLenient() {
    assertEquals(" ", UrlUtils.decodeUrl(" "));
    assertEquals("\u00E4\u00F6\u00FC\u00DF", UrlUtils.decodeUrl("\u00E4\u00F6\u00FC\u00DF"));
    assertEquals("%", UrlUtils.decodeUrl("%"));
    assertEquals("%2", UrlUtils.decodeUrl("%2"));
    assertEquals("%2G", UrlUtils.decodeUrl("%2G"));
  }

  /** Tests {@link UrlUtils#decodeUrl(String)} against null arg. */
  public void testDecodeUrlNullSafe() {
    assertNull(UrlUtils.decodeUrl(null));
  }

  /** Tests {@link UrlUtils#decodeUrl(String)} against more complex arg. */
  public void testDecodeUrlEncodingUtf8() {
    assertEquals("\u00E4\u00F6\u00FC\u00DF", UrlUtils.decodeUrl("%C3%A4%C3%B6%C3%BC%C3%9F"));
  }
}
