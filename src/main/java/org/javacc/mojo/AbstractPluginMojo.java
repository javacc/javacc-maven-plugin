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
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.FileUtils;

/**
 * Super class, managing common plugin-level parameters and methods, of the concrete mojos that
 * generate parser and tree code files from the grammar files.
 *
 * <p>
 * Each subclass manages a goal that
 *
 * <ul>
 * <li>manages the corresponding processor(s) command line arguments (options) as maven parameters,
 * with the help of corresponding beans, and
 * <li>triggers execution of one or more processors
 * </ul>
 *
 * <p>
 * One can set a specific log level for all classes of this plugin (without setting the global
 * <code>-X</code> argument) through the (standard maven SLF4J Simple configuration) property<br>
 * <code>-Dorg.slf4j.simpleLogger.log.org.javacc.mojo=debug</code>.<br>
 *
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 * @see AbstractProcessor
 * @see AbstractArgumentsBean
 * @see AbstractPluginReport
 */
public abstract class AbstractPluginMojo extends AbstractMojo {
  
  /** The current Maven project. */
  @Parameter(defaultValue = "${project}", readonly = true, required = true) //
  protected MavenProject project;
  
  /**
   * The set of Ant-like exclusion patterns used to prevent certain files from being processed.<br>
   * By default, this set is empty such that no files are excluded.
   */
  @Parameter //
  protected String[] excludes;
  
  /**
   * The fail on plugin error flag.<br>
   * It governs how the plugin will handle the errors it encountered on general configuration (i.e.
   * parameters not related to grammars).<br>
   * Possible values are <code>true</code> and <code>false</code>.<br>
   * On the first error: if set to <code>false</code>, the error message is displayed but the plugin
   * will not report an error for the current execution (i.e. it will continue with the next
   * execution);<br>
   * if set to <code>true</code> the error message is displayed and the plugin will report an error
   * for the build.
   */
  @Parameter(property = "javacc.failOnPluginError", defaultValue = "true") //
  protected Boolean failOnPluginError;
  
  /**
   * The fail on grammar error parameter.<br>
   * It governs how the plugin will handle the errors it encountered while trying to read a grammar
   * file to retrieve the parser name (and the parser package for languages that use it) (in an
   * execution the plugin may process zero, one or many grammars).<br>
   * Possible values are <code>first</code>, <code>last</code> and <code>ignore</code>.<br>
   * If set to <code>first</code>, the error message is displayed, the plugin will stop processing
   * other grammars and will report an error for the build;<br>
   * if set to <code>last</code>, the error message is displayed, the plugin will continue
   * processing other grammars and at the end it will report an error for the build;<br>
   * if set to <code>ignore</code> the error message(s) is(are) displayed but the plugin will not
   * report an error for the current execution (i.e. it will continue with the next execution).
   */
  @Parameter(property = "javacc.failOnGrammarError", defaultValue = "first") //
  protected String failOnGrammarError;
  
  /**
   * The fail on processor error parameter.<br>
   * It governs how the plugin will handle the errors returned by the processor invocations and the
   * plugin post-processor copy operations (in an execution the plugin may process zero, one or many
   * grammars and invoke one or more processors for each).<br>
   * Possible values are <code>first</code>, <code>last</code> and <code>ignore</code>.<br>
   * If set to <code>first</code>, the error message is displayed, the plugin will stop processing
   * other grammars and will report an error for the build;<br>
   * if set to <code>last</code>, the error message is displayed, the plugin will continue
   * processing other grammars and at the end it will report an error for the build;<br>
   * if set to <code>ignore</code> the error message(s) is(are) displayed but the plugin will not
   * report an error for the current execution (i.e. it will continue with the next execution).
   */
  @Parameter(property = "javacc.failOnProcessorError", defaultValue = "first") //
  protected String failOnProcessorError;
  
  /**
   * The set of Ant-like inclusion patterns used to select files from the source directory for
   * processing.<br>
   * By default, the patterns are:<br>
   * <code>**&#47;*.jj</code> for the <code>javacc</code> goal,<br>
   * <code>**&#47;*.jjt</code> for the <code>jjtree</code> and <code>jjtree-javacc</code> goals,<br>
   * <code>**&#47;*.jtb</code> for the <code>jtb</code> and <code>jtb-javacc</code> goals.
   */
  @Parameter //
  protected String[] includes = null;
  
  /**
   * The keep the intermediate directory(ies) flag.<br>
   * If set to true, the intermediate directory(ies) will not be deleted, which may sometimes be
   * handy for plugin or processor debug purposes.
   */
  @Parameter(property = "javacc.keepIntermediateDirectory", defaultValue = "false") //
  protected Boolean keepIntermediateDirectory;
  
  /**
   * The skip processing flag.<br>
   * If true, no goal will not be executed.
   */
  @Parameter(property = "javacc.skip", defaultValue = "false") //
  protected Boolean skip;
  
  /**
   * The directory where the grammar files are located.<br>
   * It must exist and be a directory, otherwise a plugin error will be raised.<br>
   * This directory will be recursively scanned for input files to pass to JavaCC.<br>
   * If one wants a pom to process more than one source directory, he must configure multiple
   * executions with different source directories.<br>
   * Note: we could have implemented a list of source directories (as in
   * {@link AbstractPluginReport}, but most of the time different source directories will need
   * different configurations, so the user would still have to configure multiple executions.<br>
   * The parameters <code>includes</code> and <code>excludes</code> can be used to select a subset
   * of the files.<br>
   * If not an absolute path, maven internals considers it is relative to <code>${basedir}</code>
   * and converts it accordingly to an absolute path (i.e. <code>src/main/javacc</code> will be
   * considered as <code>${basedir}/src/main/javacc</code>, but <code>/src/main/javacc</code> will
   * be considered as an absolute path, usually leading to an error.<br>
   * The default value, adequate for a user written grammar, but not for a generated (by a
   * preprocessor) grammar, is:<br>
   * <code>${basedir}/src/main/javacc</code> for JavaCC,<br>
   * <code>${basedir}/src/main/jjtree</code> for JJTree,<br>
   * <code>${basedir}/src/main/jtb</code> for JTB.
   */
  @Parameter(property = "javacc.sourceDirectory") //
  protected File sourceDirectory = null;
  
  /**
   * The delta in milliseconds of the last modification timestamps for testing whether a grammar
   * file needs regeneration.<br>
   * If set to a negative value, no comparison will be performed and grammars will always be passed
   * to the processor.<br>
   * Otherwise a grammar file will be passed to the processor if the sum of the main generated file
   * timestamp plus this delta is lower than the grammar file timestamp or than the more recent of
   * the dependent jars timestamps.
   */
  @Parameter(property = "javacc.timestampDeltaMs", defaultValue = "0") //
  protected Long timestampDeltaMs;
  
  /**
   * The set of compile source roots whose contents are not generated as part of the goal by the
   * current processor, i.e. those that usually reside somewhere below "${basedir}/src" in the
   * project structure.<br>
   * Files in these source roots are owned by the user and must not be overwritten with generated
   * files.
   */
  protected List<File> nonGeneratedCompileSourceRoots;
  
  /**
   * Abstract getter.
   *
   * @return the grammar file encoding option value, or the default encoding if no option, never
   *         <code>null</code>
   */
  protected abstract String getGrammarFileEncoding();
  
  /**
   * Abstract getter.
   *
   * @return The output language option value, or the default language if no option, never <code>
   *     null</code>
   */
  protected abstract Language getLanguage();
  
  @Override
  public void execute() throws MojoExecutionException {
    
    if (skip) {
      getLog().info("Skipping processing as requested");
      return;
    }
    
    try {
      checkOptions();
      initProcessor();
    }
    catch (final PluginException e) {
      // failOnPluginError == 'true' will throw a MojoExecutionException
      handlePluginException(e);
      // failOnPluginError == 'false'
      return;
    }
    
    List<GrammarInfo> grammarInfos = null;
    try {
      grammarInfos = scanForGrammars();
    }
    catch (final GrammarException e) {
      // 'ignore' for failOnGrammarError should not lead to throwing a GrammarException
      // 'first' and 'last' for failOnGrammarError must lead to throw a MojoExecutionException
      getLog().error(e.getMessage() + " while failOnGrammarError is '" + failOnGrammarError + "'");
      throw new MojoExecutionException(e.getMessage(), e.getCause());
    }
    
    if (grammarInfos == null //
        || grammarInfos.size() == 0) {
      
      getLog().info("Nothing to generate in source directory '" + sourceDirectory + "': "
          + (grammarInfos == null ? "no grammars" : "all generated parsers are up to date"));
      // here we still need to add the compile source roots so we do not return
      
    } else {
      
      try {
        determineNonGeneratedCompileSourceRoots();
      }
      catch (final PluginException e) {
        // note-jacoco: quite impossible to set a test case for here
        // failOnPluginError == 'true' will throw a MojoExecutionException
        handlePluginException(e);
        // failOnPluginError == 'false'
        return;
      }
      
      int nb = 0;
      for (final GrammarInfo gi : grammarInfos) {
        try {
          processGrammar(gi);
          nb++;
        }
        catch (final ProcessorException e) {
          handleProcessorException(e);
        }
      }
      getLog().info("Processed " + nb + " grammar(s) successfully and " + (grammarInfos.size() - nb)
          + " with errors");
      
      if (processorErrorToIgnore) {
        // failOnProcessorError == 'ignore'
        return;
      } else if (processorErrorFailure) {
        // failOnProcessorError == 'last'
        throw new MojoExecutionException(
            "Returning a build error as encountered one or more processor exceptions");
      }
    }
    
    for (final File outputDirectory : getGoalOutputDirectories()) {
      addCompileSourceRoot(outputDirectory);
    }
  }
  
  /** True to tell the caller to ignore the grammar processing error. */
  private boolean processorErrorToIgnore = false;
  
  /**
   * True to tell {@link #execute()} to return a build failure on grammar processing 'last' error.
   */
  private boolean processorErrorFailure = false;
  
  /**
   * Transforms the plugin configuration exception in a build error if the plugin is configured to
   * fail on plugin configuration errors, otherwise just logs the error.
   *
   * @param e - the plugin configuration exception
   * @throws MojoExecutionException if the plugin is configured to fail on plugin configuration
   *           error
   */
  private void handlePluginException(final PluginException e) throws MojoExecutionException {
    if (failOnPluginError) {
      getLog().error(e.getMessage());
      throw new MojoExecutionException(e.getMessage(), e.getCause());
    } else {
      getLog().error(e.getMessage());
      getLog().info("Continuing to next execution as failOnPluginError is set to 'false'");
    }
  }
  
  /**
   * Transforms the grammar processing exception in a build error if the plugin is configured to
   * fail on 'first' grammar processing error, otherwise logs the error and sets flags for 'last'
   * and 'ignore' values.
   *
   * @param e - the grammar processing exception
   * @throws MojoExecutionException if the plugin is configured to fail on grammar processing error
   */
  private void handleProcessorException(final ProcessorException e) throws MojoExecutionException {
    if ("first".equals(failOnProcessorError)) {
      getLog().error(e.getMessage());
      throw new MojoExecutionException(e.getMessage(), e.getCause());
    } else if ("last".equals(failOnProcessorError)) {
      processorErrorFailure = true;
      getLog().error(e.getMessage());
      getLog().info("Continuing current execution as failOnProcessorError is set to 'last'");
    } else { // 'ignore'
      processorErrorToIgnore = true;
      getLog().error(e.getMessage());
      getLog().info("Continuing to next execution as failOnProcessorError is set to 'ignore'");
    }
  }
  
  /**
   * Checks valid values for different options.
   *
   * @throws PluginException if sourceDirectory does not exist or is not a directory
   */
  void checkOptions() throws PluginException {
    
    /* sourceDirectory */
    // here it can be null; if so it will be initialized to a default value by each processor;
    // and if not null, maven internals seems to always convert relative paths to an absolute ones
    getLog().debug("sourceDirectory is '" + sourceDirectory + "'");
    if (sourceDirectory != null) {
      if (!sourceDirectory.exists()) {
        throw new PluginException("sourceDirectory '" + sourceDirectory + "' does not exist");
      } else if (!sourceDirectory.isDirectory()) {
        throw new PluginException("sourceDirectory '" + sourceDirectory + "' is not a directory");
      }
    }
    
    /* timestampDeltaMs */
    if (timestampDeltaMs < 0L) {
      getLog().info("negative timestampDeltaMs '" + timestampDeltaMs
          + "', so grammars will always be processed");
    } else {
      getLog().debug("timestampDeltaMs is '" + timestampDeltaMs + "'");
    }
    
    /* failOnPluginError */
    getLog().debug("failOnPluginError is '" + failOnPluginError + "'");
    
    /* failOnGrammarError */
    switch (failOnGrammarError.toLowerCase()) {
    case "first":
    case "last":
    case "ignore":
      failOnGrammarError = failOnGrammarError.toLowerCase();
      getLog().debug("failOnGrammarError is '" + failOnGrammarError + "'");
      break;
    default:
      getLog().warn("invalid value '" + failOnGrammarError
          + "' for failOnGrammarError parameter; must be 'first', 'last' or 'ignore';"
          + " kept to default 'first'");
      failOnGrammarError = "first";
      break;
    }
    
    /* failOnProcessorError */
    switch (failOnProcessorError.toLowerCase()) {
    case "first":
    case "last":
    case "ignore":
      failOnProcessorError = failOnProcessorError.toLowerCase();
      getLog().debug("failOnProcessorError is '" + failOnProcessorError + "'");
      break;
    default:
      getLog().warn("invalid value '" + failOnProcessorError
          + "' for failOnProcessorError parameter; must be 'first', 'last' or 'ignore';"
          + " kept to default 'first'");
      failOnProcessorError = "first";
      break;
    }
  }
  
  /**
   * Checks for consistent options "grammar file encoding" and "language" between the preprocessor
   * and the javacc processor.
   *
   * @param b1 - the preprocessor arguments bean
   * @param b2 - the javacc arguments bean
   * @param preproc - the preprocessor name
   * @throws PluginException if inconsistent option(s)
   */
  void checkConsistentSpecificOptions(final AbstractArgumentsBean b1,
      final AbstractArgumentsBean b2, final String preproc) throws PluginException {
    
    boolean hasErr = false;
    
    final String jjtGFE = b1.grammarFileEncodingOptionValue;
    final String jjGFE = b2.grammarFileEncodingOptionValue;
    final String defGFE = AbstractArgumentsBean.defaultGrammarFileEncoding;
    // negated condition of consistent explicit and default option values
    if (!(jjtGFE != null && (jjGFE == null || jjtGFE.equals(jjGFE))
        || jjtGFE == null && (jjGFE == null || jjGFE.equals(defGFE)))) {
      hasErr = true;
      getLog().warn("Grammar file encodings are inconsistent: " + preproc + ": '"
          + (jjtGFE != null ? jjtGFE + "'" : defGFE + "' (default)") + ", javacc: '"
          + (jjGFE != null ? jjGFE + "'" : defGFE + "' (default)"));
    }
    
    final Language jjtLang = b1.languageOptionValue;
    final Language jjLang = b2.languageOptionValue;
    final Language defLang = AbstractArgumentsBean.defaultLanguage;
    // negated condition of consistent explicit and default option values
    if (!(jjtLang != null && (jjLang == null || jjtLang.equals(jjLang))
        || jjtLang == null && (jjLang == null || jjLang.equals(defLang)))) {
      hasErr = true;
      getLog().warn("Languages are inconsistent: " + preproc + ": '"
          + (jjtLang != null ? jjtLang + "'" : defLang + "' (default)") + ", javacc: '"
          + (jjLang != null ? jjLang + "'" : defLang + "' (default)"));
    }
    
    if (hasErr) {
      throw new PluginException("Inconsistent option(s)");
    }
  }
  
  /**
   * Initializes (some default values specific to the processor(s) and those ones).
   *
   * @throws PluginException if invalid option value
   */
  protected abstract void initProcessor() throws PluginException;
  
  /**
   * Scans the configured source directory for grammar files which need processing.
   *
   * @return a list of grammar infos describing the found grammar files, may be <code>null</code> if
   *         no grammar in the directory, and may be empty if no stale grammar found
   * @throws GrammarException if some grammar file could not be read or parsed
   */
  List<GrammarInfo> scanForGrammars() throws GrammarException {
    getLog().debug("Scanning for grammars in '" + sourceDirectory + "'");
    final GrammarDirectoryScanner gds = //
        new GrammarDirectoryScanner(getLog(), getLanguage(), getGrammarFileEncoding());
    gds.dsSetExcludes(excludes);
    gds.dsSetIncludes(includes);
    gds.dsSetBasedir(sourceDirectory);
    gds.setJarsLastTS(computeLastTS());
    gds.setOutputDirectories(getProcessorOutputDirectories());
    gds.setTimestampDeltaMs(timestampDeltaMs);
    final List<GrammarInfo> grammarInfos = gds.scanForGrammars(failOnGrammarError);
    getLog().debug("Found grammars: "
        + (grammarInfos == null ? "none" : Arrays.toString(grammarInfos.toArray())));
    return grammarInfos;
  }
  
  /**
   * Computes the most recent lastModified timestamp of the dependencies jars.
   *
   * @return the most recent lastModified timestamp of the dependencies jars
   */
  long computeLastTS() {
    long lastTS = 0L;
    // core
    lastTS = updateTS("META-INF/maven/org.javacc/core", lastTS);
    // generator
    final Language lang = getLanguage();
    if (lang == null) {
      // may be normal if running a tool's version < 8
      // note-jacoco: no test case set for here
      getLog().info("No code generator configured, check if normal (i.e. running version < 8)");
    } else {
      lastTS = updateTS("META-INF/maven/org.javacc.generator/" + lang.subDir, lastTS);
      // custom template(s)
      lastTS = updateTS("templates/" + lang.subDir, lastTS);
    }
    return lastTS;
  }
  
  /**
   * Updates the most recent lastModified timestamp of the dependencies jars with the jar of a given
   * resource.
   *
   * @param name - the name of a resource included in a jar
   * @param inLastTS - the current timestamp value
   * @return the updated timestamp value: the one of the jar containing the given resource if more
   *         recent than the current one, otherwise the current one
   */
  long updateTS(final String name, final long inLastTS) {
    long outLastTS = inLastTS;
    if (name != null) {
      final URL url = Thread.currentThread().getContextClassLoader().getResource(name);
      getLog().debug("Found url '" + url + "' containing resource '" + name + "'");
      if (url != null) {
        // urlName =
        // jar:file:/C:/Users/.../java/8.1.0-SNAPSHOT/java-8.1.0-SNAPSHOT.jar!/META-INF/maven/org.javacc.generator/java
        final String urlName = url.getPath();
        // jarName =
        // file:/C:/Users/.../java/8.1.0-SNAPSHOT/java-8.1.0-SNAPSHOT.jar!/META-INF/maven/org.javacc.generator/java
        // must remove leading "file:" and trailing "!..."
        final String jarName = urlName.substring(6, urlName.indexOf('!'));
        final long ts = new File(jarName).lastModified();
        getLog().debug("LastModified timestamp of '" + jarName + "' is: " + ts);
        if (ts == 0L) {
          // note-jacoco: not found a way to set a test case for here
          getLog().warn("Non existing or badly extracted jar '" + jarName + "' from existing url '"
              + urlName + "'");
        } else {
          if (ts > outLastTS) {
            outLastTS = ts;
          }
        }
      } else {
        // note-jacoco: no test case set for here
        getLog()
            .warn("No dependent jar found for resource '" + name + "'; check plugin configuration");
      }
    }
    return outLastTS;
  }
  
  /**
   * Abstract getter.
   *
   * @return the array of absolute paths to the directories where the (current) processor will
   *         output its generated files, never <code>null</code>
   */
  protected abstract File[] getProcessorOutputDirectories();
  
  /**
   * Abstract getter.
   *
   * @return the array of the goal mojo's processor(s) output directories that will be registered as
   *         compile source roots in the project, never <code>null</code>
   */
  protected abstract File[] getGoalOutputDirectories();
  
  /**
   * Registers the specified directory as a compile source root for the current project.
   *
   * @param directory - the absolute path to the directory to add, must not be <code>null</code>
   */
  void addCompileSourceRoot(final File directory) {
    // project looks never null
    project.addCompileSourceRoot(directory.getAbsolutePath());
    getLog().debug(
        "Added (output) directory '" + directory.getAbsolutePath() + "' as a compile source root");
  }
  
  /**
   * Determines those compile source roots of the project that do not reside below the project's
   * build directories.<br>
   * These compile source roots are assumed to contain hand-crafted sources that must not be
   * overwritten with generated files.<br>
   * In most cases, this is simply "${project.build.sourceDirectory}".
   *
   * @throws PluginException if the compile source roots could not be determined (because of an
   *           error in getting a canonical path)
   */
  void determineNonGeneratedCompileSourceRoots() throws PluginException {
    nonGeneratedCompileSourceRoots = new ArrayList<File>();
    try {
      final String targetPrefix = new File(project.getBuild().getDirectory()).getCanonicalPath()
          + File.separator;
      getLog().debug("sourceDirectory = '" + sourceDirectory + "'");
      for (final String csRoot : project.getCompileSourceRoots()) {
        final File compileSourceRoot = new File(csRoot);
        // maven internals makes compileSourceRoot absolute
        final String compileSourceRootPath = compileSourceRoot.getCanonicalPath();
        if (compileSourceRoot.getAbsolutePath().equals(sourceDirectory.getAbsolutePath()) //
            || !compileSourceRootPath.startsWith(targetPrefix)) {
          nonGeneratedCompileSourceRoots.add(compileSourceRoot);
          getLog().debug("compile source root: '" + compileSourceRoot + "' is a non generated one");
        } else {
          getLog().debug("compile source root: '" + compileSourceRoot + "' is a generated one");
        }
      }
    }
    catch (final IOException | SecurityException e) {
      // note-jacoco: quite impossible to set a test case for here
      throw new PluginException("Failed to determine non-generated source roots", e);
    }
  }
  
  /**
   * Tells the subclass to process the specified grammar file (it may execute one or more
   * processors).
   *
   * @param grammarInfo - the grammar info describing the grammar file to process, must not be
   *          <code>null</code>
   * @throws ProcessorException if the invocation of the processor(s) failed or if the processor(s)
   *           reported a non-zero exit code or if the generated files could not be copied
   */
  protected abstract void processGrammar(GrammarInfo grammarInfo) throws ProcessorException;
  
  /**
   * Runs a processor on a specified grammar file.
   *
   * @param grammarInfo - the grammar info describing the grammar file to process, must not be
   *          <code>null</code>
   * @param intermediateDirectories - the array of intermediate output directories where the
   *          processor will write the generated files (instead of the configured output
   *          directories, see {@link #copyGrammarOutput(File, File, String, boolean)})
   * @param copyGenFiles - true to copy the generated files, false otherwise
   * @throws ProcessorException if the invocation of the processor failed or if the processor
   *           reported a non-zero exit code or if the generated files could not be copied
   */
  void runProcessorOnGrammar(final GrammarInfo grammarInfo, final File[] intermediateDirectories,
      final boolean copyGenFiles) throws ProcessorException {
    final File gramFile = grammarInfo.getAbsoluteGrammarFile();
    final File gramDirectory = gramFile.getParentFile();
    // the output directories may be sub directories of the given intermediate directories
    final File[] intermedOutputDirectories = new File[intermediateDirectories.length];
    for (int i = 0; i < intermediateDirectories.length; i++) {
      intermedOutputDirectories[i] = (new File(intermediateDirectories[i],
          grammarInfo.getParserSubDirectory())).getAbsoluteFile();
    }
    
    // run the processor to generate files
    runProcessor(gramFile, intermediateDirectories);
    
    // copy generated files
    for (int i = 0; i < intermediateDirectories.length; i++) {
      getLog().debug("1st copyGrammarOutput: i=" + i + ", gi.subd='"
          + grammarInfo.getParserSubDirectory() + "':");
      copyGrammarOutput(intermediateDirectories[i], getProcessorOutputDirectories()[i],
          grammarInfo.getParserSubDirectory(),
          // "*",
          copyGenFiles);
    }
    
    getLog().debug("2nd copyGrammarOutput: copyGenFiles=" + copyGenFiles + ", gi.srcd='"
        + grammarInfo.getSourceDirectory() + "', ingcsr='"
        + isNonGeneratedCompileSourceRoot(grammarInfo.getSourceDirectory()) + "':");
    if (copyGenFiles && //
        !isNonGeneratedCompileSourceRoot(grammarInfo.getSourceDirectory())) {
      // if asked to copy the generated files and if the grammar does not reside in a declared
      // source root, copy source files which are beside the grammar
      copyGrammarOutput( //
          gramDirectory, //
          getProcessorOutputDirectories()[0], //
          grammarInfo.getParserSubDirectory(), //
          // "*",
          false);
    } else {
      // but if the grammar does reside in a declared source root,
      // do not copy them (otherwise we would get duplicated classes)
    }
    
    if (!keepIntermediateDirectory) {
      deleteIntermediateDirectories(intermediateDirectories);
    } else {
      getLog().info("Intermediate directory(ies) '" + displayDirectories(intermediateDirectories)
          + "' not deleted as requested");
    }
  }
  
  /**
   * @param directories - an array of directories
   * @return the string of the comma separated list of directories
   */
  protected static String displayDirectories(final File[] directories) {
    if (directories == null) {
      return null;
    }
    String msg = "";
    for (int i = 0; i < directories.length; i++) {
      try {
        msg += directories[i].getCanonicalPath();
      }
      catch (final IOException e) {
        msg += "IOException on element of intermediateDirectories";
      }
      if (i < directories.length - 1) {
        msg += ", ";
      }
    }
    return msg;
  }
  
  /**
   * Runs a processor on a given grammar file generating files into the given output directories.
   *
   * @param grammar - the grammar file
   * @param outputDirectories - the output directories
   * @throws ProcessorException if the invocation of the processor failed or if the processor
   *           reported a non-zero exit code
   */
  protected abstract void runProcessor(final File grammar, final File[] outputDirectories)
      throws ProcessorException;
  
  /**
   * Scans a given origin directory and its subdirectories for generated files and copies them under
   * the given destination directory, taking in account the parser directory deriving from the
   * parser package if any.<br>
   * It is intended that the resulting destination directory will be a compile source root.<br>
   * An output file is only copied if it doesn't already exist in a compile source root.<br>
   * This prevents duplicate class errors during compilation in case the user provided customized
   * files in a compile source directory like <code>src/main/java</code> or similar.
   *
   * @param origin - the (absolute) path to the directory to scan for the to-be-copied generated
   *          output files, must not be <code>null</code>
   * @param destination - the (absolute) path to the destination directory into which the output
   *          files should be copied, must not be <code>null</code>
   * @param subDirectory - the name of the destination sub directory for the output files, must not
   *          be <code>null</code>, and must be empty or terminated by a file separator
   * @param copyAnnotatedFile - true to also copy the jj file (as generated / annotated), false
   *          otherwise
   * @throws ProcessorException if the generated files could not be copied
   */
  void copyGrammarOutput(final File origin, final File destination, final String subDirectory,
      final boolean copyAnnotatedFile) throws ProcessorException {
    
    final List<File> tbcFiles;
    String ext = "";
    final Language lang = getLanguage();
    try {
      ext = "**/*" + lang.extension;
      tbcFiles = FileUtils.getFiles(origin, ext, null);
      if (copyAnnotatedFile) {
        ext = "**/*.jj";
        tbcFiles.addAll(FileUtils.getFiles(origin, ext, null));
      }
      if (lang.otherExtensions != null) {
        ext = "**/*" + lang.otherExtensions;
        tbcFiles.addAll(FileUtils.getFiles(origin, ext, null));
      }
    }
    catch (final IOException e) {
      throw new ProcessorException(
          "Failed to get generated files '" + ext + "' within '" + origin + "'", e);
    }
    
    for (final File tbcFile : tbcFiles) {
      final String gf = tbcFile.getPath().substring(1 + origin.getPath().length());
      final String outputPath = subDirectory + gf;
      final File outputFile = new File(destination, outputPath);
      final File sourceFile = findIfIsNonGeneratedSourceFile(outputPath);
      
      if (sourceFile == null) {
        try {
          FileUtils.copyFile(tbcFile, outputFile);
          getLog().debug("Copied generated file '" + tbcFile + "' to '" + outputFile + "'");
        }
        catch (final IOException e) {
          throw new ProcessorException(
              "Failed to copy generated file '" + tbcFile + "' to '" + outputFile + "'", e);
        }
      } else {
        getLog().debug("Skipping copying user file '" + outputPath
            + "' as custom or generated one '" + sourceFile + "' exists");
      }
    }
  }
  
  /**
   * Determines whether a given file exists in any of the non generated compile source roots
   * registered with the current Maven project or in any of the processor output directories.
   *
   * @param filename - the name of the file to check, relative to a compile source root or a
   *          processor output directory, must not be <code>
   *     null</code>
   * @return the (absolute) path to the existing source file if any, <code>null</code> otherwise
   */
  File findIfIsNonGeneratedSourceFile(final String filename) {
    for (final File nonGeneratedCompileSourceRoot : nonGeneratedCompileSourceRoots) {
      final File sourceFile = new File(nonGeneratedCompileSourceRoot, filename);
      if (sourceFile.exists()) {
        return sourceFile;
      }
    }
    for (final File procOutDir : getProcessorOutputDirectories()) {
      final File sourceFile = new File(procOutDir, filename);
      if (sourceFile.exists()) {
        return sourceFile;
      }
    }
    return null;
  }
  
  /**
   * True if the specified directory is a non generated compile source root of the current project,
   * false otherwise.
   *
   * @param dir - the directory to check, must not be <code>null</code>
   * @return <code>true</code> if the specified directory is a non generated compile source root of
   *         the project, <code>false</code> otherwise
   */
  boolean isNonGeneratedCompileSourceRoot(final File dir) {
    return nonGeneratedCompileSourceRoots.contains(dir);
  }
  
  /**
   * Deletes the specified intermediate directories.
   *
   * @param dirs - the directory to delete, must not be <code>null</code>
   */
  void deleteIntermediateDirectories(final File[] dirs) {
    for (final File dir : dirs) {
      try {
        FileUtils.deleteDirectory(dir);
        getLog().debug("Deleted intermediate directory '" + dir + "'");
      }
      catch (final IOException e) {
        // note-jacoco: quite impossible to set a test case for here
        getLog().warn("Failed to delete intermediate directory '" + dir + "'", e);
      }
    }
  }
}
