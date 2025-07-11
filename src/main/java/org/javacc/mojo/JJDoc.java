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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

/** Provides a facade for the mojos to invoke JJDoc. */
class JJDoc extends ToolFacade {

  /** The input grammar. */
  private File inputFile;

  /** The option OUTPUT_FILE. */
  private File outputFile;

  /** The option GRAMMAR_ENCODING. */
  private String grammarEncoding;

  /** The option CSS. */
  private String cssHref;

  /** The option TEXT. */
  private Boolean text;

  /** The option BNF. */
  private Boolean bnf;

  /** The option ONE_TABLE. */
  private Boolean oneTable;

  /**
   * Sets the absolute path to the grammar file to pass into JJDoc for documentation.
   *
   * @param value The absolute path to the grammar file to pass into JJDoc for documentation.
   */
  public void setInputFile(final File value) {
    if (value != null && !value.isAbsolute()) {
      throw new IllegalArgumentException("path is not absolute: " + value);
    }
    inputFile = value;
  }

  /**
   * Sets the absolute path to the output file.
   *
   * @param value The absolute path to the HTML/text file to generate.
   */
  public void setOutputFile(final File value) {
    if (value != null && !value.isAbsolute()) {
      throw new IllegalArgumentException("path is not absolute: " + value);
    }
    outputFile = value;
  }

  /**
   * Sets the option GRAMMAR_ENCODING.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setGrammarEncoding(final String value) {
    grammarEncoding = value;
  }

  /**
   * Sets the option CSS, i.e the hypertext reference to a CSS file for the generated HTML output.
   *
   * @param value The option value, may be <code>null</code> to use the default style.
   */
  public void setCssHref(final String value) {
    cssHref = value;
  }

  /**
   * Sets the option TEXT.
   *
   * @param value The option value, may be <code>null</code> to use the default value.
   */
  public void setText(final Boolean value) {
    text = value;
  }

  /**
   * Sets the option BNF.
   *
   * @param value The option value, may be <code>null</code> to use the default value.
   */
  public void setBnf(final Boolean value) {
    bnf = value;
  }

  /**
   * Sets the option value ONE_TABLE.
   *
   * @param value The option value, may be <code>null</code> to use the default value.
   */
  public void setOneTable(final Boolean value) {
    oneTable = value;
  }

  /** {@inheritDoc} */
  @Override
  protected int execute() throws Exception {
    final String[] args = generateArguments();

    final File outputDirectory = (outputFile != null) ? outputFile.getParentFile() : null;
    if (outputDirectory != null && !outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }

    // fork jjdoc because of calls to System.exit()
    final ForkedJvm jvm = new ForkedJvm();
    jvm.setMainClass(org.javacc.jjdoc.JJDocMain.class);
    jvm.addArguments(args);
    jvm.setSystemOut(new MojoLogStreamConsumer(false));
    jvm.setSystemErr(new MojoLogStreamConsumer(true));
    if (getLog().isDebugEnabled()) {
      getLog().debug("Forking: " + jvm);
    }
    return jvm.run();
  }

  /**
   * Assembles the command line arguments for the invocation of JJDoc according to the
   * configuration.
   *
   * @return A string array that represents the arguments to use for JJDoc.
   */
  private String[] generateArguments() {
    final List<String> argsList = new ArrayList<String>();

    if (StringUtils.isNotEmpty(grammarEncoding)) {
      argsList.add("-GRAMMAR_ENCODING=" + grammarEncoding);
    }

    if (text != null) {
      argsList.add("-TEXT=" + text);
    }

    if (bnf != null) {
      argsList.add("-BNF=" + bnf);
    }

    if (oneTable != null) {
      argsList.add("-ONE_TABLE=" + oneTable);
    }

    if (outputFile != null) {
      argsList.add("-OUTPUT_FILE=" + outputFile.getAbsolutePath());
    }

    if (StringUtils.isNotEmpty(cssHref)) {
      argsList.add("-CSS=" + cssHref);
    }

    if (inputFile != null) {
      argsList.add(inputFile.getAbsolutePath());
    }

    return (String[]) argsList.toArray(new String[argsList.size()]);
  }

  /**
   * Gets a string representation of the command line arguments.
   *
   * @return A string representation of the command line arguments.
   */
  @Override
  public String toString() {
    return Arrays.asList(generateArguments()).toString();
  }

  /** Consume and log command line output from the JJDoc process. */
  class MojoLogStreamConsumer implements StreamConsumer {

    /** The line prefix used by JJDoc to report errors. */
    private static final String ERROR_PREFIX = "Error: ";

    /** The line prefix used by JJDoc to report warnings. */
    private static final String WARN_PREFIX = "Warning: ";

    /**
     * Determines if the stream consumer is being used for <code>System.out</code> or <code>
     * System.err</code>.
     */
    private final boolean logAtErrorLevel;

    /**
     * Single param constructor.
     *
     * @param error If set to <code>true</code>, all consumed lines will be logged at the error
     *     level.
     */
    public MojoLogStreamConsumer(final boolean error) {
      logAtErrorLevel = error;
    }

    /**
     * Consume a line of text.
     *
     * @param line The line to consume.
     */
    @Override
    public void consumeLine(final String line) {
      if (line.startsWith(ERROR_PREFIX)) {
        getLog().error(line.substring(ERROR_PREFIX.length()));
      } else if (line.startsWith(WARN_PREFIX)) {
        getLog().warn(line.substring(WARN_PREFIX.length()));
      } else if (logAtErrorLevel) {
        getLog().error(line);
      } else {
        getLog().debug(line);
      }
    }
  }
}
