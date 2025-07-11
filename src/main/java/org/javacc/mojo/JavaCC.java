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

/** Provides a facade for the mojos to invoke JavaCC. */
class JavaCC extends ToolFacade {

  /** The input grammar. */
  private File inputFile;

  /** The option OUTPUT_DIRECTORY. */
  private File outputDirectory;

  /** The option GRAMMAR_ENCODING. */
  private String grammarEncoding;

  //  /** The option JDK_VERSION. */
  //  private String jdkVersion;

  /** The option STATIC. */
  private Boolean isStatic;

  /** The option LOOK_AHEAD. */
  private Integer lookAhead;

  /** The option CHOICE_AMBIGUITY_CHECK. */
  private Integer choiceAmbiguityCheck;

  /** The option OTHER_AMBIGUITY_CHECK. */
  private Integer otherAmbiguityCheck;

  /** The option DEBUG_PARSER. */
  private Boolean debugParser;

  /** The option DEBUG_LOOK_AHEAD. */
  private Boolean debugLookAhead;

  /** The option DEBUG_TOKEN_MANAGER. */
  private Boolean debugTokenManager;

  /** The option ERROR_REPORTING. */
  private Boolean errorReporting;

  /** The option JAVA_UNICODE_ESCAPE. */
  private Boolean javaUnicodeEscape;

  /** The option UNICODE_INPUT. */
  private Boolean unicodeInput;

  /** The option IGNORE_CASE. */
  private Boolean ignoreCase;

  /** The option COMMON_TOKEN_ACTION. */
  private Boolean commonTokenAction;

  /** The option USER_TOKEN_MANAGER. */
  private Boolean userTokenManager;

  /** The option USER_CHAR_STREAM. */
  private Boolean userCharStream;

  /** The option BUILD_PARSER. */
  private Boolean buildParser;

  /** The option BUILD_TOKEN_MANAGER. */
  private Boolean buildTokenManager;

  /** The option TOKEN_MANAGER_USES_PARSER. */
  private Boolean tokenManagerUsesParser;

  /** The option TOKEN_EXTENDS. */
  private String tokenExtends;

  /** The option TOKEN_FACTORY. */
  private String tokenFactory;

  /** The option SANITY_CHECK. */
  private Boolean sanityCheck;

  /** The option FORCE_LA_CHECK. */
  private Boolean forceLaCheck;

  /** The option CACHE_TOKENS. */
  private Boolean cacheTokens;

  /** The option KEEP_LINE_COLUMN. */
  private Boolean keepLineColumn;

  /** The option SUPPORT_CLASS_VISIBILITY_PUBLIC. */
  private Boolean supportClassVisibilityPublic;

  /** The option CODE_GENERATOR. */
  private String codeGenerator;

  /** The option OUTPUT_LANGUAGE. */
  private String outputLanguage;

  /**
   * /** Sets the absolute path to the grammar file to pass into JavaCC for compilation.
   *
   * @param value The absolute path to the grammar file to pass into JavaCC for compilation.
   */
  public void setInputFile(final File value) {
    if (value != null && !value.isAbsolute()) {
      throw new IllegalArgumentException("path is not absolute: " + value);
    }
    inputFile = value;
  }

  /**
   * Sets the absolute path to the output directory.
   *
   * @param value The absolute path to the output directory for the generated parser file. If this
   *     directory does not exist yet, it is created. Note that this path should already include the
   *     desired package hierarchy because JavaCC will not append the required sub directories
   *     automatically.
   */
  public void setOutputDirectory(final File value) {
    if (value != null && !value.isAbsolute()) {
      throw new IllegalArgumentException("path is not absolute: " + value);
    }
    outputDirectory = value;
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

  //  /**
  //   * Sets the option JDK_VERSION.
  //   *
  //   * @param value The option value, may be <code>null</code> to use the value provided in the
  //   *     grammar or the default.
  //   */
  //  public void setJdkVersion(final String value) {
  //    jdkVersion = value;
  //  }

  /**
   * Sets the option STATIC.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setStatic(final Boolean value) {
    isStatic = value;
  }

  /**
   * Sets the option LOOK_AHEAD.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setLookAhead(final Integer value) {
    lookAhead = value;
  }

  /**
   * Sets the option CHOICE_AMBIGUITY_CHECK.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setChoiceAmbiguityCheck(final Integer value) {
    choiceAmbiguityCheck = value;
  }

  /**
   * Sets the option OTHER_AMBIGUITY_CHECK.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setOtherAmbiguityCheck(final Integer value) {
    otherAmbiguityCheck = value;
  }

  /**
   * Sets the option DEBUG_PARSER.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setDebugParser(final Boolean value) {
    debugParser = value;
  }

  /**
   * Sets the option DEBUG_LOOK_AHEAD.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setDebugLookAhead(final Boolean value) {
    debugLookAhead = value;
  }

  /**
   * Sets the option DEBUG_TOKEN_MANAGER.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setDebugTokenManager(final Boolean value) {
    debugTokenManager = value;
  }

  /**
   * Sets the option ERROR_REPORTING.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setErrorReporting(final Boolean value) {
    errorReporting = value;
  }

  /**
   * Sets the option JAVA_UNICODE_ESCAPE.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setJavaUnicodeEscape(final Boolean value) {
    javaUnicodeEscape = value;
  }

  /**
   * Sets the option UNICODE_INPUT.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setUnicodeInput(final Boolean value) {
    unicodeInput = value;
  }

  /**
   * Sets the option IGNORE_CASE.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setIgnoreCase(final Boolean value) {
    ignoreCase = value;
  }

  /**
   * Sets the option COMMON_TOKEN_ACTION.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setCommonTokenAction(final Boolean value) {
    commonTokenAction = value;
  }

  /**
   * Sets the option USER_TOKEN_MANAGER.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setUserTokenManager(final Boolean value) {
    userTokenManager = value;
  }

  /**
   * Sets the option USER_CHAR_STREAM.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setUserCharStream(final Boolean value) {
    userCharStream = value;
  }

  /**
   * Sets the option BUILD_PARSER.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setBuildParser(final Boolean value) {
    buildParser = value;
  }

  /**
   * Sets the option BUILD_TOKEN_MANAGER.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setBuildTokenManager(final Boolean value) {
    buildTokenManager = value;
  }

  /**
   * Sets the option TOKEN_MANAGER_USES_PARSER.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setTokenManagerUsesParser(final Boolean value) {
    tokenManagerUsesParser = value;
  }

  /**
   * Sets the option TOKEN_EXTENDS.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setTokenExtends(final String value) {
    tokenExtends = value;
  }

  /**
   * Sets the option TOKEN_FACTORY.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setTokenFactory(final String value) {
    tokenFactory = value;
  }

  /**
   * Sets the option SANITY_CHECK.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setSanityCheck(final Boolean value) {
    sanityCheck = value;
  }

  /**
   * Sets the option FORCE_LA_CHECK.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setForceLaCheck(final Boolean value) {
    forceLaCheck = value;
  }

  /**
   * Sets the option CACHE_TOKENS.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setCacheTokens(final Boolean value) {
    cacheTokens = value;
  }

  /**
   * Sets the option KEEP_LINE_COLUMN.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setKeepLineColumn(final Boolean value) {
    keepLineColumn = value;
  }

  /**
   * Sets the option SUPPORT_CLASS_VISIBILITY_PUBLIC.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setSupportClassVisibilityPublic(final Boolean value) {
    supportClassVisibilityPublic = value;
  }

  /**
   * Sets the option CODE_GENERATOR.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setCodeGenerator(final String value) {
    codeGenerator = value;
  }

  /**
   * Sets the option OUTPUT_LANGUAGE.
   *
   * @param value The option value, may be <code>null</code> to use the value provided in the
   *     grammar or the default.
   */
  public void setOutputLanguage(final String value) {
    outputLanguage = value;
  }

  /** {@inheritDoc} */
  @Override
  protected int execute() throws Exception {
    final String[] args = generateArguments();

    if (outputDirectory != null && !outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }
    if (getLog().isDebugEnabled()) {
      getLog().debug(toString());
    }

    return org.javacc.parser.Main.mainProgram(args);
  }

  /**
   * Assembles the command line arguments for the invocation of JavaCC according to the
   * configuration.<br>
   * <br>
   * <strong>Note:</strong> To prevent conflicts with JavaCC options that might be set directly in
   * the grammar file, only those parameters that have been explicitly set are passed on the command
   * line.
   *
   * @return A string array that represents the command line arguments to use for JavaCC.
   */
  private String[] generateArguments() {
    final List<String> argsList = new ArrayList<String>();

    if (StringUtils.isNotEmpty(grammarEncoding)) {
      argsList.add("-GRAMMAR_ENCODING=" + grammarEncoding);
    }

    //    if (StringUtils.isNotEmpty(jdkVersion)) {
    //      argsList.add("-JDK_VERSION=" + jdkVersion);
    //    }

    if (lookAhead != null) {
      argsList.add("-LOOKAHEAD=" + lookAhead);
    }

    if (choiceAmbiguityCheck != null) {
      argsList.add("-CHOICE_AMBIGUITY_CHECK=" + choiceAmbiguityCheck);
    }

    if (otherAmbiguityCheck != null) {
      argsList.add("-OTHER_AMBIGUITY_CHECK=" + otherAmbiguityCheck);
    }

    if (isStatic != null) {
      argsList.add("-STATIC=" + isStatic);
    }

    if (debugParser != null) {
      argsList.add("-DEBUG_PARSER=" + debugParser);
    }

    if (debugLookAhead != null) {
      argsList.add("-DEBUG_LOOKAHEAD=" + debugLookAhead);
    }

    if (debugTokenManager != null) {
      argsList.add("-DEBUG_TOKEN_MANAGER=" + debugTokenManager);
    }

    if (errorReporting != null) {
      argsList.add("-ERROR_REPORTING=" + errorReporting);
    }

    if (javaUnicodeEscape != null) {
      argsList.add("-JAVA_UNICODE_ESCAPE=" + javaUnicodeEscape);
    }

    if (unicodeInput != null) {
      argsList.add("-UNICODE_INPUT=" + unicodeInput);
    }

    if (ignoreCase != null) {
      argsList.add("-IGNORE_CASE=" + ignoreCase);
    }

    if (commonTokenAction != null) {
      argsList.add("-COMMON_TOKEN_ACTION=" + commonTokenAction);
    }

    if (userTokenManager != null) {
      argsList.add("-USER_TOKEN_MANAGER=" + userTokenManager);
    }

    if (userCharStream != null) {
      argsList.add("-USER_CHAR_STREAM=" + userCharStream);
    }

    if (buildParser != null) {
      argsList.add("-BUILD_PARSER=" + buildParser);
    }

    if (buildTokenManager != null) {
      argsList.add("-BUILD_TOKEN_MANAGER=" + buildTokenManager);
    }

    if (tokenManagerUsesParser != null) {
      argsList.add("-TOKEN_MANAGER_USES_PARSER=" + tokenManagerUsesParser);
    }

    if (StringUtils.isNotEmpty(tokenExtends)) {
      argsList.add("-TOKEN_EXTENDS=" + tokenExtends);
    }

    if (StringUtils.isNotEmpty(tokenFactory)) {
      argsList.add("-TOKEN_FACTORY=" + tokenFactory);
    }

    if (sanityCheck != null) {
      argsList.add("-SANITY_CHECK=" + sanityCheck);
    }

    if (forceLaCheck != null) {
      argsList.add("-FORCE_LA_CHECK=" + forceLaCheck);
    }

    if (cacheTokens != null) {
      argsList.add("-CACHE_TOKENS=" + cacheTokens);
    }

    if (keepLineColumn != null) {
      argsList.add("-KEEP_LINE_COLUMN=" + keepLineColumn);
    }

    if (supportClassVisibilityPublic != null) {
      argsList.add("-SUPPORT_CLASS_VISIBILITY_PUBLIC=" + supportClassVisibilityPublic);
    }

    if (StringUtils.isNotEmpty(codeGenerator)) {
      argsList.add("-CODE_GENERATOR=" + codeGenerator);
    }

    if (StringUtils.isNotEmpty(outputLanguage)) {
      argsList.add("-OUTPUT_LANGUAGE=" + outputLanguage);
    }

    if (outputDirectory != null) {
      argsList.add("-OUTPUT_DIRECTORY=" + outputDirectory.getAbsolutePath());
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
}
