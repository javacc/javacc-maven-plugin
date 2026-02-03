/*
 * Copyright (c) 2025-2026, Marc Mazas <mazas.marc@gmail.com>.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the names of the copyright holders nor the names of its
 *       contributors may be used to endorse or promote products derived from
 *       this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.javacc.mojo;

import java.io.File;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.FileUtils;

/**
 * This bean holds some output related information about a JavaCC grammar file.<br>
 * It assists in determining the exact output location for the generated parser file.
 *
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 */
class GrammarInfo {
  
  /** The logger. */
  private final Log log;
  
  /**
   * The absolute path to the base directory in which the grammar file resides; must not be null.
   */
  private final File sourceDirectory;
  
  /**
   * The path to the grammar file, relative to its source directory (e.g. <code>grammars/MyParser.jj
   * </code>); must not be null.
   */
  final String grammarFile;
  
  /**
   * The declared package for the generated parser (e.g. <code>org.javacc.mypkg</code>) if any (and
   * if the language supports this feature), or the empty string if none, or null if not needed.
   */
  private String parserPackage = null;
  
  /**
   * The path to the sub directory of the generated parser (e.g. if the language supports the
   * "package" or "namespace" feature), relative to an output directory that will be registered as a
   * source root directory (e.g. <code>org/javacc/mypkg</code>), terminated by the file separator,
   * or the empty string if none, or null if not needed.
   */
  private String parserSubDirectory = null;
  
  /** The simple name of the generated parser (e.g. <code>MyParser</code>); must not be null. */
  private String parserName = "";
  
  /** The name of the main generated file, of null if not needed. */
  private String mainGeneratedFile = null;
  
  /**
   * The regex to find the package name.<br>
   * Allows invalid package names (like <code>a-b.0.*</code>), but allows valid ones with non ASCII
   * characters (like <code>org.jcc.ßπ6</code> or <code>org.jcc.ß\u03c06</code>).<br>
   * It constrains that there is a line starting with the word <code>package</code> but it does not
   * ensure that it is not inside a block comment like:
   *
   * <pre>
   * &#47;&#42;
   *  * package a.b.c;
   * &#42;&#47;
   * package d.e.f;
   * </pre>
   */
  private static final String packageDeclaration = "^package\\s+([^\\.;]+(\\.[^\\.;]+)*)\\s*;";
  
  /**
   * The regex to find the parser name.<br>
   * It constrains that there is a line starting with the word <code>PARSER_BEGIN</code> but it does
   * not ensure that it is not inside a block comment like:
   *
   * <pre>
   * &#47;&#42;
   *  * PARSER_BEGIN(p);
   * &#42;&#47;
   * PARSER_BEGIN(q);
   * </pre>
   */
  private static final String parserBegin = "^PARSER_BEGIN\\s*\\(\\s*([^\\s\\)]+)\\s*\\)";
  
  /** JavaCC file extension. */
  public static final String JJ_EXT = ".jj";
  
  /**
   * Creates a new info from the specified grammar file.
   *
   * @param lg - the logger
   * @param lang - the language to generate the parser for
   * @param enc - the grammar file encoding
   * @param sourceDir - the absolute path (not checked) to the base directory in which the grammar
   *          file resides, must not be <code>null</code>
   * @param inputFile - the path to the grammar file (relative to the source directory - not
   *          checked), must not be <code>null</code>
   * @throws GrammarException if reading the grammar file failed, or if no parser name can be
   *           retrieved in the grammar
   */
  public GrammarInfo(final Log lg, final Language lang, final String enc, final File sourceDir,
      final String inputFile) throws GrammarException {
    
    log = lg;
    sourceDirectory = sourceDir;
    final File inFile = new File(inputFile);
    grammarFile = inFile.getPath();
    String grammar;
    try {
      grammar = FileUtils.fileRead(getAbsoluteGrammarFile(), enc);
    }
    catch (final IOException e) {
      throw new GrammarException(
          "Error reading input file '" + inputFile + "' / '" + getAbsoluteGrammarFile() + "'", e);
    }
    
    if (lang == null) {
      log.debug("no language set, probably for a reporting goal");
      return;
    }
    
    // TODO find a better way to isolate language dependent code
    if (lang.usesPackage) {
      parserPackage = findPackageName(grammar);
      parserSubDirectory = parserPackage.replace('.', File.separatorChar);
      if (parserSubDirectory.length() > 0) {
        parserSubDirectory += File.separator;
      }
    } else if (lang.usesPath) {
      parserPackage = "";
      parserSubDirectory = inFile.getParent() == null ? "" : inFile.getParent() + File.separator;
    } else {
      // note-jacoco: need to wait for an ad-hoc language to set a test case for here
      parserPackage = "";
      parserSubDirectory = "";
    }
    log.debug("parserPackage = '" + parserPackage + "', parserSubDirectory = '" + parserSubDirectory
        + "'");
    
    parserName = findParserName(grammar);
    if (parserName.length() <= 0) {
      throw new GrammarException(
          "No parser name found in PARSER_BEGIN(...) statement for grammar '" + inputFile + "'");
    }
    log.debug("parserName = '" + parserName + "'");
    
    if (grammarFile.endsWith(JJ_EXT)) {
      mainGeneratedFile = parserSubDirectory + parserName + lang.extension;
    } else {
      mainGeneratedFile = grammarFile.substring(0, grammarFile.length() - 4) + JJ_EXT;
      if (!grammarFile.startsWith(parserSubDirectory)) {
        mainGeneratedFile = parserSubDirectory + mainGeneratedFile;
      }
    }
    
    log.debug("mainGeneratedFile = '" + mainGeneratedFile + "'");
  }
  
  /** The compiled regex pattern to find an escaped unicode character. */
  static final Pattern pattUni = Pattern.compile("\\\\u[0-9a-fA-F]{4}");
  
  /**
   * Converts an input string with escaped unicode characters in a string without escaped unicode
   * characters.
   *
   * @param input - an input string
   * @return the original string or a new string, always without escaped unicode characters
   */
  public static String removeEscapedUnicodeCharacters(final String input) {
    final Matcher matcher = pattUni.matcher(input);
    if (!matcher.find()) {
      return input;
    }
    final StringBuffer uncodedString = new StringBuffer(input.length());
    do {
      final String unicodeSequence = matcher.group();
      final char unicodeChar = (char) Integer.parseInt(unicodeSequence.substring(2), 16);
      matcher.appendReplacement(uncodedString, Character.toString(unicodeChar));
    } while (matcher.find());
    matcher.appendTail(uncodedString);
    return uncodedString.toString();
  }
  
  /**
   * Creates a new GrammarInfo from the current one changing its grammar file to the corresponding
   * <code>.jj</code> one and its source directory to a given one.
   *
   * @param sourceDir - the source directory
   * @return - the new GramarInfo
   */
  public GrammarInfo deriveJJ(final File sourceDir) {
    String jjFile = grammarFile.replace(parserSubDirectory, "");
    jjFile = jjFile.substring(0, jjFile.lastIndexOf('.')) + JJ_EXT;
    log.debug("grammarFile = '" + grammarFile + "', sourceDir = '" + sourceDir
        + "', parserSubDirectory = '" + parserSubDirectory + "', jjFile = '" + jjFile + "'");
    return new GrammarInfo(this, sourceDir, jjFile);
  }
  
  /**
   * Constructor for cloning an instance and change some fields.
   *
   * @param gi - the instance to clone
   * @param sourceDir - the new source directory field
   * @param gramFile - the new grammar file field
   */
  private GrammarInfo(final GrammarInfo gi, final File sourceDir, final String gramFile) {
    log = gi.log;
    sourceDirectory = sourceDir;
    grammarFile = gi.parserSubDirectory + gramFile;
    parserPackage = gi.parserPackage;
    parserSubDirectory = gi.parserSubDirectory;
    parserName = gi.parserName;
    mainGeneratedFile = gi.mainGeneratedFile;
  }
  
  /**
   * Extracts the declared package name from the specified grammar file.
   *
   * @param grammar - the contents of the grammar file, must not be <code>null</code>
   * @return the declared package name or an empty string if not found
   */
  private String findPackageName(final String grammar) {
    final Matcher matcher = Pattern.compile(packageDeclaration, Pattern.MULTILINE).matcher(grammar);
    return matcher.find() ? removeEscapedUnicodeCharacters(matcher.group(1)) : "";
  }
  
  /**
   * Extracts the simple parser name from the specified grammar file.
   *
   * @param grammar - the contents of the grammar file, must not be <code>null</code>
   * @return The parser name or an empty string if not found
   */
  private String findParserName(final String grammar) {
    final Matcher matcher = Pattern.compile(parserBegin, Pattern.MULTILINE).matcher(grammar);
    return matcher.find() ? removeEscapedUnicodeCharacters(matcher.group(1)) : "";
  }
  
  /**
   * Gets the absolute path to the base directory in which the grammar file resides.<br>
   * Note that this is not necessarily the parent directory of the grammar file.
   *
   * @return the absolute path to the base directory in which the grammar file resides, never <code>
   *     null</code>
   */
  public File getSourceDirectory() {
    return sourceDirectory;
  }
  
  /**
   * Gets the path to the grammar file (relative to its source directory).
   *
   * @return the path to the grammar file (relative to its source directory), never <code>null
   *     </code>
   */
  public String getGrammarFile() {
    return grammarFile;
  }
  
  /**
   * Gets the absolute path to the grammar file.
   *
   * @return the absolute path to the grammar file, never <code>null</code>
   */
  public File getAbsoluteGrammarFile() {
    return new File(sourceDirectory, grammarFile);
  }
  
  /**
   * Gets the declared package for the generated parser (e.g. <code>org.javacc.mypkg</code>).
   *
   * @return the declared package for the generated parser or an empty string if no package
   *         declaration was found, never <code>null</code>
   */
  public String getParserPackage() {
    return parserPackage;
  }
  
  /**
   * Gets the path to the sub directory of the generated parser (relative to an output directory
   * that will be registered as a source root directory, e.g. <code>org/javacc/mypkg</code>),
   * terminated by the file separator, or the empty string.
   *
   * @return the path to the sub directory of the generated parser or an empty string, never <code>
   *     null</code>
   */
  public String getParserSubDirectory() {
    return parserSubDirectory;
  }
  
  /**
   * Gets the simple name of the generated parser (e.g. <code>MyParser</code>)
   *
   * @return the simple name of the generated parser, never <code>null</code>
   */
  public String getParserName() {
    return parserName;
  }
  
  /**
   * Gets the name of the main generated file.
   *
   * @return the name of the main generated file, can be <code>null</code>
   */
  public String getMainGeneratedFile() {
    return mainGeneratedFile;
  }
  
  /**
   * Sets the name of the main generated file.
   *
   * @param name - the name of the main generated file, never <code>null</code>
   */
  public void setMainGeneratedFile(final String name) {
    mainGeneratedFile = name;
  }
  
  /**
   * Gets a string representation of this bean (for debugging).
   *
   * @return a string representation of this bean
   */
  @Override
  public String toString() {
    return getAbsoluteGrammarFile() + " -> " + getMainGeneratedFile();
  }
}
