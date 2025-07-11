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
import org.codehaus.plexus.util.cli.CommandLineUtils.StringStreamConsumer;
import junit.framework.TestCase;

/** Tests {@link ForkedJvm}. */
public class ForkedJvmTest extends TestCase {

  /** Tests {@link ForkedJvm#ForkedJvm()} and {@link ForkedJvm#toString()}. */
  public void testToStringNullSafe() {
    final ForkedJvm jvm = new ForkedJvm();
    final String string = jvm.toString();
    assertNotNull(string);
    assertTrue(string.indexOf("null") < 0);
  }

  /** Tests {@link ForkedJvm} setters. */
  public void testSettersNullSafe() {
    final ForkedJvm jvm = new ForkedJvm();
    jvm.addArgument((File) null);
    jvm.addArgument((String) null);
    jvm.addArguments(null);
    jvm.addArguments(new String[] {null});
    jvm.addClassPathEntry((Class<?>) null);
    jvm.addClassPathEntry((File) null);
    jvm.addClassPathEntry((String) null);
    jvm.setMainClass((Class<?>) null);
    jvm.setMainClass((String) null);
    jvm.setWorkingDirectory(null);
    jvm.setSystemOut(null);
    jvm.setSystemErr(null);
  }

  /** Tests {@link ForkedJvm#setMainClass(Class)} and {@link ForkedJvm#setMainClass(String)}. */
  public void testSetMainClass() {
    final ForkedJvm jvm1 = new ForkedJvm();
    jvm1.setMainClass(MainStub.class);
    final String cmd1 = jvm1.toString();
    assertTrue(cmd1.indexOf(MainStub.class.getName()) >= 0);

    final ForkedJvm jvm2 = new ForkedJvm();
    jvm2.setMainClass(MainStub.class.getName());
    final String cmd2 = jvm2.toString();
    assertTrue(cmd2.indexOf(MainStub.class.getName()) >= 0);

    assertEquals(cmd1, cmd2);
  }

  /**
   * Tests {@link ForkedJvm#run()}.
   *
   * @throws Exception IOException if no <code>user.home</code> directory or CommandLineException if
   *     the JVM could not be forked.
   */
  public void testFork() throws Exception {
    final File workDir = new File(System.getProperty("user.home")).getCanonicalFile();
    final File file = new File("test").getAbsoluteFile();
    final String nonce = Integer.toString(hashCode());

    final StringStreamConsumer stdout = new StringStreamConsumer();
    final StringStreamConsumer stderr = new StringStreamConsumer();

    final ForkedJvm jvm = new ForkedJvm();
    jvm.setWorkingDirectory(workDir);
    jvm.setSystemOut(stdout);
    jvm.setSystemErr(stderr);
    jvm.setMainClass(MainStub.class);
    jvm.addArgument(nonce);
    jvm.addArguments(new String[] {"arg1", "arg2"});
    jvm.addArgument(file);
    System.out.println("Forking: " + jvm);
    final int exitcode = jvm.run();
    final String out = stdout.getOutput();
    final String err = stderr.getOutput();
    final String[] args = out.split("(\r\n)|(\r)|(\n)");

    assertEquals(27, exitcode);

    assertEquals(workDir, new File(err.trim()));

    assertEquals(4, args.length);
    assertEquals(nonce, args[0]);
    assertEquals("arg1", args[1]);
    assertEquals("arg2", args[2]);
    assertEquals(file, new File(args[3]));
  }
}
