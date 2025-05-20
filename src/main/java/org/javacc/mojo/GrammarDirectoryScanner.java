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
import java.util.ArrayList;
import java.util.List;
import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;

/** Scans source directories for JavaCC grammar files. */
class GrammarDirectoryScanner {

  /** The directory scanner used to scan the source directory for files. */
  private final Log log;

  /** The directory scanner used to scan the source directory for files. */
  private final DirectoryScanner ds;

  /**
   * The absolute path to the output directory used to detect stale target files by timestamp
   * checking, may be <code>null</code> if no stale detection should be performed.
   */
  private File outputDirectory;

  // TODO: Once the parameter "packageName" from the javacc mojo has been deleted, remove this
  // field, too.
  /**
   * The package name for the generated parser, may be <code>null</code> to use the package
   * declaration from the grammar file.
   */
  private String parserPackage;

  /**
   * The granularity in milliseconds of the last modification date for testing whether a grammar
   * file needs recompilation because its corresponding target file is stale.
   */
  private int staleMillis;

  /**
   * A set of grammar infos describing the included grammar files, must never be <code>null</code>.
   */
  private final List<GrammarInfo> includedGrammars;

  /**
   * Creates a new grammar directory scanner.
   *
   * @param lg the Log to use
   */
  public GrammarDirectoryScanner(final Log lg) {
    log = lg;
    ds = new DirectoryScanner();
    ds.setFollowSymlinks(true);
    includedGrammars = new ArrayList<GrammarInfo>();
  }

  /**
   * The most recent lastModified timestamp of the dependencies jars.
   *
   * @since 3.8.0
   */
  private long lastTS = 0;

  /**
   * Sets the most recent lastModified timestamp of the dependencies jars.
   *
   * @param ts the most recent lastModified timestamp of the dependencies jars
   * @since 3.8.0
   */
  public void setLastTS(final long ts) {
    lastTS = ts;
  }

  /**
   * Sets the absolute path to the source directory to scan for grammar files.<br>
   * This directory must exist or the scanner will report an error.
   *
   * @param directory The absolute path to the source directory to scan, must not be <code>null
   *     </code>.
   */
  public void setSourceDirectory(final File directory) {
    if (!directory.isAbsolute()) {
      throw new IllegalArgumentException("source directory is not absolute: " + directory);
    }
    ds.setBasedir(directory);
  }

  /**
   * Sets the package name for the generated parser.
   *
   * @param packageName The package name for the generated parser, may be <code>null</code> to use
   *     the package declaration from the grammar file.
   */
  public void setParserPackage(final String packageName) {
    parserPackage = packageName;
  }

  /**
   * Sets the Ant-like inclusion patterns.
   *
   * @param includes The set of Ant-like inclusion patterns, may be <code>null</code> to include all
   *     files.
   */
  public void setIncludes(final String[] includes) {
    ds.setIncludes(includes);
  }

  /**
   * Sets the Ant-like exclusion patterns.
   *
   * @param excludes The set of Ant-like exclusion patterns, may be <code>null</code> to exclude no
   *     files.
   */
  public void setExcludes(final String[] excludes) {
    ds.setExcludes(excludes);
    ds.addDefaultExcludes();
  }

  /**
   * Sets the absolute path to the output directory used to detect stale target files.
   *
   * @param directory The absolute path to the output directory used to detect stale target files by
   *     timestamp checking, may be <code>null</code> if no stale detection should be performed.
   */
  public void setOutputDirectory(final File directory) {
    if (directory != null && !directory.isAbsolute()) {
      throw new IllegalArgumentException("output directory is not absolute: " + directory);
    }
    outputDirectory = directory;
  }

  /**
   * Sets the granularity in milliseconds of the last modification date for stale file detection.
   *
   * @param milliseconds The granularity in milliseconds of the last modification date for testing
   *     whether a grammar file needs recompilation because its corresponding target file is stale.
   */
  public void setStaleMillis(final int milliseconds) {
    staleMillis = milliseconds;
  }

  /**
   * Scans the source directory for grammar files that match at least one inclusion pattern but no
   * exclusion pattern, performing timestamp checking to include grammars that are older than the
   * dependent jars and optionally performing timestamp checking to exclude grammars whose
   * corresponding parser files are up to date.
   *
   * @throws IOException If a grammar file could not be analyzed for metadata.
   */
  public void scan() throws IOException {

    includedGrammars.clear();

    ds.scan();

    for (final String includedFile : ds.getIncludedFiles()) {
      log.debug("includedFile = " + includedFile + ", parserPackage = " + parserPackage);

      final GrammarInfo grammarInfo =
          new GrammarInfo(Suffix.Java, ds.getBasedir(), includedFile, parserPackage);
      final File grammarFile = grammarInfo.getGrammarFile();
      final long grammarTS = grammarFile.lastModified();
      log.debug("LastModified timestamp of grammar " + grammarFile + ": " + grammarTS);

      if (outputDirectory != null) {
        final File[] targetFiles = getTargetFiles(outputDirectory, includedFile, grammarInfo);
        for (final File targetFile : targetFiles) {
          final long targetTS = targetFile.lastModified();
          log.debug("LastModified timestamp of generated " + targetFile + ": " + targetTS);
          if (!targetFile.exists()) {
            // no  generated file
            includedGrammars.add(grammarInfo);
            log.debug("Grammar " + grammarFile + " included as no existing generated parser");
            break;
          }
          if (targetTS + staleMillis < grammarTS) {
            // grammar file more recent than generated file
            includedGrammars.add(grammarInfo);
            log.debug(
                "Grammar "
                    + grammarFile
                    + " included as newer than generated parser "
                    + targetFile);
            break;
          }
          if (targetTS < lastTS) {
            // jar file more recent than target file
            includedGrammars.add(grammarInfo);
            log.debug(
                "Grammar "
                    + grammarFile
                    + " included as generated parser "
                    + targetFile
                    + " older than dependent jar(s)");
            break;
          }
        }
      } else {
        includedGrammars.add(grammarInfo);
      }
    }
  }

  /**
   * Determines the output files corresponding to the specified grammar file.
   *
   * @param targetDirectory The absolute path to the output directory for the target files, must not
   *     be <code>null</code>.
   * @param grammarFile The path to the grammar file, relative to the scanned source directory, must
   *     not be <code>null</code>.
   * @param grammarInfo The grammar info describing the grammar file, must not be <code>null</code>
   * @return A file array with target files, never <code>null</code>.
   */
  protected File[] getTargetFiles(
      final File targetDirectory, final String grammarFile, final GrammarInfo grammarInfo) {
    final File parserFile = new File(targetDirectory, grammarInfo.getParserFile());
    return new File[] {parserFile};
  }

  /**
   * Gets the grammar files that were included by the scanner during the last invocation of {@link
   * #scan()}.
   *
   * @return An array of grammar infos describing the included grammar files, will be empty if no
   *     files were included but is never <code>null</code>.
   */
  public GrammarInfo[] getIncludedGrammars() {
    return (GrammarInfo[]) includedGrammars.toArray(new GrammarInfo[includedGrammars.size()]);
  }
}
