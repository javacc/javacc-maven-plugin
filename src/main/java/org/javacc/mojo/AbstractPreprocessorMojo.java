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
import java.net.URL;
import java.util.Arrays;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Provides common services for all mojos that preprocess JavaCC grammar files.<br>
 * Note that these mojos have been deprecated (but may be un-deprecated at some time).
 *
 * @version 3.8: updated for JavaCC-8+ & JTB 5+; added dependent jar timestamp checking, additional
 *     classpath entry
 */
public abstract class AbstractPreprocessorMojo extends AbstractMojo {
  /**
   * The current Maven project.
   *
   * @component
   */
  private MavenProject project;

  /**
   * Gets the absolute path to the directory where the grammar files are located.
   *
   * @return The absolute path to the directory where the grammar files are located, never <code>
   *     null</code>.
   */
  protected abstract File getSourceDirectory();

  /**
   * Gets a set of Ant-like inclusion patterns used to select files from the source directory for
   * processing.
   *
   * @return A set of Ant-like inclusion patterns used to select files from the source directory for
   *     processing, can be <code>null</code> if all files should be included.
   */
  protected abstract String[] getIncludes();

  /**
   * Gets a set of Ant-like exclusion patterns used to unselect files from the source directory for
   * processing.
   *
   * @return A set of Ant-like inclusion patterns used to unselect files from the source directory
   *     for processing, can be <code>null</code> if no files should be excluded.
   */
  protected abstract String[] getExcludes();

  /**
   * Gets the absolute path to the directory where the generated Java files for the parser will be
   * stored.
   *
   * @return The absolute path to the directory where the generated Java files for the parser will
   *     be stored, never <code>null</code>.
   */
  protected abstract File getOutputDirectory();

  /**
   * Gets the absolute path to the directory where the processed input files will be stored for
   * later detection of stale sources.
   *
   * @return The absolute path to the directory where the processed input files will be stored for
   *     later detection of stale sources, never <code>null</code>.
   */
  protected abstract File getTimestampDirectory();

  /**
   * Gets the granularity in milliseconds of the last modification date for testing whether a source
   * needs recompilation.
   *
   * @return The granularity in milliseconds of the last modification date for testing whether a
   *     source needs recompilation.
   */
  protected abstract int getStaleMillis();

  /**
   * Execute the tool.
   *
   * @throws MojoExecutionException If the invocation of the tool failed.
   * @throws MojoFailureException If the tool reported a non-zero exit code.
   */
  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {
    getLog().warn("This goal has been deprecated. Please update your plugin configuration.");

    final GrammarInfo[] grammarInfos = scanForGrammars();

    if (grammarInfos == null) {
      getLog().info("Skipping non-existing source directory: " + getSourceDirectory());
      return;
    } else if (grammarInfos.length <= 0) {
      getLog().info("Skipping - all parsers are up to date");
    } else {
      if (!getTimestampDirectory().exists()) {
        getTimestampDirectory().mkdirs();
      }

      for (int i = 0; i < grammarInfos.length; i++) {
        processGrammar(grammarInfos[i]);
      }
      getLog()
          .info(
              "Processed "
                  + grammarInfos.length
                  + " grammar"
                  + (grammarInfos.length != 1 ? "s" : ""));
    }

    addCompileSourceRoot();
  }

  /**
   * Passes the specified grammar file through the tool.
   *
   * @param grammarInfo The grammar info describing the grammar file to process, must not be <code>
   *     null</code>.
   * @throws MojoExecutionException If the invocation of the tool failed.
   * @throws MojoFailureException If the tool reported a non-zero exit code.
   */
  protected abstract void processGrammar(GrammarInfo grammarInfo)
      throws MojoExecutionException, MojoFailureException;

  /**
   * Scans the configured source directory for grammar files which need processing.
   *
   * @return An array of grammar infos describing the found grammar files or <code>null</code> if
   *     the source directory does not exist.
   * @throws MojoExecutionException If the source directory could not be scanned.
   */
  private GrammarInfo[] scanForGrammars() throws MojoExecutionException {
    if (!getSourceDirectory().isDirectory()) {
      return null;
    }

    GrammarInfo[] grammarInfos;

    getLog().debug("Scanning for grammars: " + getSourceDirectory());
    try {
      final GrammarDirectoryScanner lgds = new LegacyGrammarDirectoryScanner(getLog());
      lgds.setSourceDirectory(getSourceDirectory());
      lgds.setIncludes(getIncludes());
      lgds.setExcludes(getExcludes());
      lgds.setOutputDirectory(getTimestampDirectory());
      lgds.setStaleMillis(getStaleMillis());
      lgds.setLastTS(computeLastTS());
      lgds.scan();
      grammarInfos = lgds.getIncludedGrammars();
    } catch (final Exception e) {
      throw new MojoExecutionException("Failed to scan for grammars: " + getSourceDirectory(), e);
    }
    getLog().debug("Found grammars: " + Arrays.asList(grammarInfos));

    return grammarInfos;
  }

  /**
   * Computes the most recent lastModified timestamp of the dependencies jars.
   *
   * @return the most recent lastModified timestamp of the dependencies jars
   * @since 3.8.0
   */
  protected abstract long computeLastTS();

  /**
   * Updates the most recent lastModified timestamp of the dependencies jars with the jar of a given
   * resource.
   *
   * @param name the name of a resource included in a jar
   * @param inLastTS the current timestamp value
   * @return the updated timestamp value: the one of the jar containing the given resource if more
   *     recent than the current one, otherwise the current one
   * @since 3.8.0
   */
  protected long updateTS(final String name, final long inLastTS) {
    long outLastTS = inLastTS;
    if (name != null) {
      final URL url = Thread.currentThread().getContextClassLoader().getResource(name);
      getLog().debug("Found url " + url + " containing resource " + name);
      if (url != null) {
        // urlName =
        // jar:file:/C:/Users/.../java/8.1.0-SNAPSHOT/java-8.1.0-SNAPSHOT.jar!/META-INF/maven/org.javacc.generator/java
        final String urlName = url.getPath();
        // jarName =
        // file:/C:/Users/.../java/8.1.0-SNAPSHOT/java-8.1.0-SNAPSHOT.jar!/META-INF/maven/org.javacc.generator/java
        // must remove leading "file:" and trailing "!..."
        final String jarName = urlName.substring(6, urlName.indexOf('!'));
        final long ts = new File(jarName).lastModified();
        getLog().debug("LastModified timestamp of " + jarName + ": " + ts);
        if (ts > outLastTS) {
          outLastTS = ts;
        }
      } else {
        getLog().warn("No dependent jar found for resource " + name);
      }
    }
    return outLastTS;
  }

  /**
   * Creates the timestamp file for the specified grammar file.
   *
   * @param grammarInfo The grammar info describing the grammar file to process, must not be <code>
   *     null</code>.
   */
  protected void createTimestamp(final GrammarInfo grammarInfo) {
    final File jjFile = grammarInfo.getGrammarFile();
    final File timestampFile =
        new File(getTimestampDirectory(), grammarInfo.getRelativeGrammarFile());
    try {
      FileUtils.copyFile(jjFile, timestampFile);
    } catch (final Exception e) {
      getLog().warn("Failed to create copy for timestamp check: " + jjFile, e);
    }
  }

  /** Registers the configured output directory as a compile source root for the current project. */
  protected void addCompileSourceRoot() {
    if (project != null) {
      getLog().debug("Adding compile source root: " + getOutputDirectory());
      project.addCompileSourceRoot(getOutputDirectory().getAbsolutePath());
    }
  }
}
