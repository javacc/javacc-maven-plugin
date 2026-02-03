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
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.reporting.AbstractMavenReport;
import org.apache.maven.reporting.MavenReportException;

/**
 * Super class, managing common plugin-level parameters and methods, of the concrete mojos that
 * generate report files, either:
 * <ul>
 * <li>directly as a standalone mojo (through the {@link #execute()} method), or</li>
 * <li>indirectly from the web site generation (through the {@link #executeReport(Locale)}
 * method).</li>
 * </ul>
 * Much of a copy of {@link AbstractPluginMojo}, but extending another maven class.
 *
 * <p>
 * Each subclass manages a goal that
 *
 * <ul>
 * <li>manages the corresponding processor(s) command line arguments (options) as maven parameters,
 * with the help of corresponding beans, and
 * <li>triggers execution of one or more processors.
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
 */
public abstract class AbstractPluginReport extends AbstractMavenReport {
  
  /**
   * The set of Ant-like exclusion patterns used to prevent certain files from being processed.<br>
   * By default, this set is empty such that no files are excluded.
   */
  @Parameter //
  protected String[] excludes;
  
  /**
   * The fail on plugin error flag.<br>
   * It governs governs how the plugin will handle the errors it encountered on general
   * configuration (i.e. parameters not related to grammars).<br>
   * Possible values are <code>true</code> and <code>false</code>.<br>
   * On an error: if set to <code>false</code>, the error message is displayed, the execution
   * terminates, but the plugin does not report an error for the current execution (i.e. it
   * continues with the next execution);<br>
   * if set to <code>true</code> the error message is displayed, the execution terminates and the
   * plugin reports an error for the build.
   */
  @Parameter(property = "javacc.failOnPluginError", defaultValue = "true") //
  protected Boolean failOnPluginError;
  
  /**
   * The fail on grammar error parameter.<br>
   * It governs governs how the plugin will handle the errors it encountered while trying to read a
   * grammar file to retrieve the parser name (and the parser package for languages that use it)
   * (and in an execution the plugin may process zero, one or many grammars).<br>
   * Possible values are <code>first</code>, <code>last</code> and <code>ignore</code>.<br>
   * If set to <code>first</code>, the error message is displayed, the plugin stops processing other
   * grammars and reports an error for the build;<br>
   * if set to <code>last</code>, the error message is displayed, the plugin continues processing
   * other grammars and at the end it reports an error for the build;<br>
   * if set to <code>ignore</code> the error message(s) is(are) displayed but the plugin does not
   * report an error for the current execution (i.e. it continues with the next execution).
   */
  @Parameter(property = "javacc.failOnGrammarError", defaultValue = "first") //
  protected String failOnGrammarError;
  
  /**
   * The fail on processor error parameter.<br>
   * It governs how the plugin will handle the errors returned by the processor invocations and the
   * plugin post-processor copy operations (in an execution the plugin may process zero, one or many
   * grammars and invoke one or more processors for each).<br>
   * Possible values are <code>first</code>, <code>last</code> and <code>ignore</code>.<br>
   * If set to <code>first</code>, the error message is displayed, the plugin stops processing other
   * grammars and reports an error for the build;<br>
   * if set to <code>last</code>, the error message is displayed, the plugin continues processing
   * other grammars and at the end it reports an error for the build;<br>
   * if set to <code>ignore</code> the error message(s) is(are) displayed but the plugin does not
   * report an error for the current execution (i.e. it continues with the next execution).
   */
  @Parameter(property = "javacc.failOnProcessorError", defaultValue = "first") //
  protected String failOnProcessorError;
  
  /**
   * The set of Ant-like inclusion patterns used to select files from the source directory for
   * processing.<br>
   * By default, the pattern is:<br>
   * <code>**&#47;*.jj</code>, <code>**&#47;*.jjt</code>, <code>**&#47;*.jtb</code> for the
   * <code>jjdoc</code> goal.
   */
  @Parameter //
  protected String[] includes = null;
  
  /**
   * The skip processing flag.<br>
   * If true, no goal will not be executed.
   */
  @Parameter(property = "javacc.skip", defaultValue = "false") //
  protected Boolean skip;
  
  /**
   * The directories where the grammar files are located.<br>
   * They all must exist and be directories, otherwise a plugin error will be raised.<br>
   * These directories will be recursively scanned for input files to pass to JavaCC.<br>
   * Note that they will be under a common configuration; so if one wants a pom to process source
   * directories with different configuration, he must configure multiple executions with different
   * source directories.<br>
   * The parameters <code>includes</code> and <code>excludes</code> can be used to select a subset
   * of the files.<br>
   * If not absolute paths, maven internals considers they ar relative to <code>${basedir}</code>
   * and converts them accordingly to absolute paths (i.e. <code>src/main/javacc</code> will be
   * considered as <code>${basedir}/src/main/javacc</code>, but <code>/src/main/javacc</code> will
   * be considered as an absolute path, usually leading to an error.<br>
   * The default value, adequate for a user written grammar, but not for a generated (by a
   * preprocessor) grammar, is the array:<br>
   * <code>${basedir}/src/main/javacc</code>, <code>${basedir}/src/main/jjtree</code>,
   * <code>${basedir}/src/main/jtb</code>.
   */
  @Parameter(property = "javacc.sourceDirectories") //
  protected List<File> sourceDirectories = null;
  
  /**
   * Abstract getter.
   *
   * @return the grammar file encoding option value, or the default encoding if no option, never
   *         <code>null</code>
   */
  protected abstract String getGrammarFileEncoding();
  
  /**
   * Flag telling if initialization has been performed.<br>
   * Necessary, as {@link #canGenerateReport()} and {@link #executeReport(Locale)} can be called
   * together on the same mojo instance, {@link #executeReport(Locale)} directly by the site plugin
   * goal and or indirectly from the subclass mojo goal through (bottom->up)
   * {@link #generate(Sink, org.apache.maven.doxia.sink.SinkFactory, Locale)} /
   * {@link #reportToMarkup()} or {@link #reportToSite()} / {@link #execute()}, which itself calls
   * {@link #canGenerateReport()}.
   */
  private boolean initDone = false;
  
  /**
   * Initialize the mojo.
   * 
   * @throws PluginException - for an error in initialization
   */
  public void initialize() throws PluginException {
    checkOptions();
    initProcessor();
    initDone = true;
  }
  
  @Override
  public boolean canGenerateReport() {
    
    getLog().debug("Entering canGenerateReport()");
    
    if (skip) {
      getLog().info("Skipping processing as requested");
      return false;
    }
    
    try {
      if (!initDone) {
        initialize();
      }
    }
    catch (final PluginException e) {
      try {
        // failOnPluginError == 'true' will throw a MavenReportException
        handlePluginException(e);
        // failOnPluginError == 'false' will continue here
      }
      catch (final MavenReportException mre) {
        // in both cases we return false
      }
      return false;
    }
    
    for (final File dir : sourceDirectories) {
      final String[] files = dir.list();
      if (files != null && files.length > 0) {
        getLog().debug("canGenerateReport() on sourceDirectories '" + displaySourceDirectories()
            + "' returns true");
        return true;
      }
    }
    getLog().debug("canGenerateReport() on sourceDirectories '" + displaySourceDirectories()
        + "' returns false");
    return false;
  }
  
  @Override
  public void executeReport(final Locale locale) throws MavenReportException {
    
    getLog().debug(
        "Entering executeReport(final Locale locale), locale is '" + locale.getDisplayName() + "'");
    
    if (skip) {
      getLog().info("Skipping processing as requested");
      return;
    }
    
    try {
      if (!initDone) {
        initialize();
      }
    }
    catch (final PluginException e) {
      // failOnPluginError == 'true' will throw a MavenReportException
      handlePluginException(e);
      // failOnPluginError == 'false' will continue here
      return;
    }
    
    final Sink sink = getSink();
    createReportHeader(getBundle(locale), sink);
    
    List<GrammarInfo> grammarInfos = null;
    try {
      grammarInfos = scanForGrammars();
    }
    catch (final GrammarException e) {
      // 'ignore' for failOnGrammarError should not lead to throwing a GrammarException
      // 'first' and 'last' for failOnGrammarError must lead to throw a MojoExecutionException
      getLog().error(e.getMessage() + " while failOnGrammarError is '" + failOnGrammarError + "'");
      throw new MavenReportException(e.getMessage() /*, e*/);
    }
    
    if (grammarInfos == null //
        || grammarInfos.size() == 0) {
      String msg = "No grammars to process in source directories '";
      msg += displaySourceDirectories();
      msg += "'";
      getLog().info(msg);
      return;
    }
    
    int nb = 0;
    for (final GrammarInfo gi : grammarInfos) {
      try {
        processGrammar(gi);
      }
      catch (final ProcessorException e) {
        handleProcessorException(e);
      }
      createReportLink(sink, gi);
      nb++;
    }
    
    createReportFooter(sink);
    sink.flush();
    sink.close();
    
    getLog().info("Processed " + nb + " grammar(s) successfully and " + (grammarInfos.size() - nb)
        + " with errors");
    
    if (processorErrorToIgnore) {
      // failOnProcessorError == 'ignore'
      return;
    } else if (processorErrorFailure) {
      // failOnProcessorError == 'last'
      throw new MavenReportException(
          "Returning a build error as encountered one or more processor exceptions");
    }
  }
  
  /**
   * Create the header and title for the HTML report page.
   *
   * @param bundle - the resource bundle with the text.
   * @param sink The sink to write to the main report file.
   */
  protected abstract void createReportHeader(ResourceBundle bundle, Sink sink);
  
  /**
   * Create the HTML footer for the report page.
   *
   * @param sink The sink to write the HTML report page.
   */
  protected abstract void createReportFooter(Sink sink);
  
  /**
   * Create a table row containing a link to the JJDoc report for a grammar file.
   *
   * @param sink - the sink to write the report
   * @param grammarInfo - the grammar file information
   */
  protected abstract void createReportLink(Sink sink, GrammarInfo grammarInfo);
  
  /**
   * Get the resource bundle for the report text.
   *
   * @param locale - the locale to use for this report
   * @return The resource bundle
   */
  protected abstract ResourceBundle getBundle(Locale locale);
  
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
   * @throws MavenReportException if the plugin is configured to fail on plugin configuration error
   */
  private void handlePluginException(final PluginException e) throws MavenReportException {
    if (failOnPluginError) {
      getLog().error(e.getMessage());
      throw new MavenReportException(e.getMessage() /*, e*/);
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
   * @throws MavenReportException if the plugin is configured to fail on grammar processing error
   */
  private void handleProcessorException(final ProcessorException e) throws MavenReportException {
    if ("first".equals(failOnProcessorError)) {
      getLog().error(e.getMessage());
      throw new MavenReportException(e.getMessage() /*, e*/);
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
  protected void checkOptions() throws PluginException {
    
    /* sourceDirectories */
    // here it can be null; if so it will be initialized to a default value by each processor;
    // and if not null, maven internals seems to always convert relative paths to an absolute ones
    getLog().debug("sourceDirectories is '" + displaySourceDirectories() + "'");
    if (sourceDirectories != null) {
      for (final File dir : sourceDirectories) {
        if (!dir.exists()) {
          throw new PluginException("sourceDirectory '" + dir + "' does not exist");
        } else if (!dir.isDirectory()) {
          throw new PluginException("sourceDirectory '" + dir + "' is not a directory");
        }
      }
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
   * Initializes (some default values specific to the processor(s) and those ones).
   *
   * @throws PluginException if invalid option value
   */
  protected abstract void initProcessor() throws PluginException;
  
  /**
   * Scans the source directories for grammar files which need processing.
   *
   * @return a list of grammar infos describing the found grammar files, may be <code>null</code> if
   *         no grammar in the directory
   * @throws GrammarException if some grammar file could not be read or parsed
   */
  protected List<GrammarInfo> scanForGrammars() throws GrammarException {
    getLog().debug("Scanning for grammars in '" + displaySourceDirectories() + "'");
    final GrammarDirectoryScanner gds = //
        new GrammarDirectoryScanner(getLog(), null, null);
    gds.dsSetExcludes(excludes);
    gds.dsSetIncludes(includes);
    gds.setJarsLastTS(-1L);
    gds.setOutputDirectories(null);
    gds.setTimestampDeltaMs(-1L);
    List<GrammarInfo> grammarInfos = null;
    for (final File dir : sourceDirectories) {
      gds.dsSetBasedir(dir);
      final List<GrammarInfo> grInfos = gds.scanForGrammars(failOnGrammarError);
      if (grInfos != null && grInfos.size() > 0) {
        if (grammarInfos == null) {
          grammarInfos = grInfos;
        } else {
          grammarInfos.addAll(grInfos);
        }
      }
    }
    getLog().debug("Found grammars: "
        + (grammarInfos == null ? "none" : Arrays.toString(grammarInfos.toArray())));
    return grammarInfos;
  }
  
  /**
   * Abstract getter.
   *
   * @return the array of absolute paths to the directories where the (current) processor will
   *         output its generated files, never <code>null</code>
   */
  protected abstract File[] getProcessorOutputDirectories();
  
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
   * @return the string of the comma separated list of source directories
   */
  protected String displaySourceDirectories() {
    if (sourceDirectories == null) {
      return null;
    }
    String msg = "";
    for (int i = 0; i < sourceDirectories.size(); i++) {
      try {
        msg += sourceDirectories.get(i).getCanonicalPath();
      }
      catch (final IOException e) {
        msg += "IOException on element " + " of sourceDirectories";
      }
      if (i < sourceDirectories.size() - 1) {
        msg += ", ";
      }
    }
    return msg;
  }
}
