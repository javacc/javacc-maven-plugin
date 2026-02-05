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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * The <b>jjdoc</b> goal, for producing documentation for a JavaCC / JJTree / JTB grammar.<br>
 * It is used indirectly under the hood when using the plugin as a reporting plugin, triggered by
 * the Maven site plugin, producing JJDoc reports. In that case there can be only one execution and
 * one configuration defining the source directories and other parameters.<br>
 * It can also be used directly as a standalone goal, as a build plugin, if one wants more control,
 * for example for multiple executions with different configurations with different sets of source
 * directories and other parameters.<br>
 * It searches the source directories for all grammar files (those included and not excluded) and
 * runs JJDoc once for each file it finds, producing output files of the format set through the
 * JJDoc options, and of names set through JJDoc options <code>OUTPUT_DIRECTORY</code> and
 * <code>OUTPUT_FILE</code>.<br>
 * It also produces, for each execution, an HTML "index" file named through the plugin parameter
 * <code>jjdocReportsPage</code> containing a table with the hyperlinks for the grammar file name to
 * its corresponding generated JJDoc document, in a directory set by a plugin parameter
 * <code>jjdocReportsDirectory</code> if used in a build plugin or by the Maven site plugin
 * parameter <code>outputDirectory</code> if used in a reporting plugin.<br>
 * And finally, if used in a reporting plugin, the Maven site plugin will create a menu entry to the
 * HTML "index" page in the "Project Documentation / Project Reports" menu of the site.
 * <p>
 * Detailed information about the JJDoc options can be found on the
 * <a href="https://javacc.github.io/javacc/documentation/jjdoc.html">JJDoc documentation
 * page</a>.<br>
 * Examples can be found in the integration tests <a href=
 * "https://github.com/javacc/javacc-maven-plugin/tree/master/src/it/jjdoc-goal">jjdoc-goal</a> and
 * <a href=
 * "https://github.com/javacc/javacc-maven-plugin/tree/master/src/it/site-phase">site-phase</a>.<br>
 * The code repositories can be found within <a href="https://github.com/javacc">JavaCC at
 * GitHub</a> and <a href="https://github.com/jtb-javacc">JTB at GitHub</a>.
 *
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 */
@Mojo(name = "jjdoc", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class JJDocGoalMojo extends AbstractPluginReport {
  
  /** The bean handling JJDoc options as command line arguments. */
  private final JJDocArgumentsBean jjdab = new JJDocArgumentsBean();
  
  /**
   * The list of single full command line arguments, which should be in the form accepted by JJDoc.
   *
   * <p>
   * No arguments are read and used by the plugin.
   *
   * <p>
   * The list will be passed as it is to JJDoc, no control nor modification is done by the
   * plugin.<br>
   * Note that JJDoc can internally pass some options to the parser it calls.
   *
   * <p>
   * The list has no default value.
   *
   * <p>
   * Example:<br>
   *
   * <pre>{@code
   * <jjdocCmdLineArgs>
   *   <arg>-CODE_GENERATOR="C#"</arg>
   *   <arg>-BNF=true</arg>
   *   <arg>-CSS="src/main/resources/my_css"</arg>
   * </jjdocCmdLineArgs>
   * }</pre>
   *
   * <br>
   * Note that the <code>javaccCmdLineArgs</code> parameter is of type {@code List<String>}, which
   * implies that the inner tags names can have any names and may be appear many times (like
   * <code>arg</code> above).<br>
   * Note also that if the project has a parent which also configures the plugin with parameters (to
   * factorize them for the children for example), the child parameters will complement / replace
   * the parent's ones for those that are absent / present in the parent; so
   * <ul>
   * <li>if one wants to get rid of (all) the parent's ones, he must use the <code>
   * combine.self="override"</code> attribute at the list level in the child, and</li>
   * <li>if one wants to replace some of the parent's ones and add new ones in the child it is
   * recommended to use distinct tag names in the parent, the same tag names in the child for those
   * that must be replaced and another tag name or other tag names for the new ones.</li>
   * </ul>
   * See also <a href=
   * "https://github.com/javacc/javacc-maven-plugin/readme.md#processor_parameters">Processor
   * parameters</a>.
   */
  @Parameter(property = "javacc.jjdocCmdLineArgs") //
  protected List<String> jjdocCmdLineArgs;
  
  /**
   * The output directory where Maven / Doxia generates the HTML files summarizing the JJDoc reports
   * generation.<br>
   * Note that this parameter is only relevant if the goal is run from the command line or from the
   * default build lifecycle.<br>
   * If the goal is run indirectly as part of a site generation, the output directory configured in
   * the Maven Site Plugin is used instead (it will usually be "${project.build.directory}/site").
   * <p>
   * Note that this is not the directory where JJDoc itself generates its reports, which must be set
   * by the JJDoc OUTPUT_DIRECTORY option.
   *
   */
  @Parameter(property = "javacc.jjdocReportsDirectory", defaultValue = "${project.build.directory}/generated-jjdoc") //
  private File jjdocReportsDirectory;
  
  /**
   * The HTML page name (with extension), relative to the output directory, where Maven / Doxia
   * generates the table of the JJDoc reports for the current execution.<br>
   * Use a non default value for each of multiple executions, to differentiate reports.
   */
  @Parameter(property = "javacc.jjdocReportsPage", defaultValue = "/jjdoc-reports.html") //
  protected String jjdocReportsPage;
  
  @Override
  protected void initProcessor() throws PluginException {
    if (includes == null) {
      includes = new String[] {
          "**/*.jj", "**/*.jjt", "**/*.jtb"
      };
      getLog().debug("no custom includes, so initialized to '" + Arrays.toString(includes) + "'");
    } else {
      getLog().debug("custom includes is '" + Arrays.toString(includes) + "'");
    }
    boolean custom = true;
    if (sourceDirectories == null) {
      custom = false;
      sourceDirectories = new ArrayList<>();
    }
    if (sourceDirectories.isEmpty()) {
      final File jjdir = new File(project.getBasedir(), JavaCCArgumentsBean.defSrcSubDir);
      if (jjdir.exists()) {
        sourceDirectories.add(jjdir);
      }
      final File jjtdir = new File(project.getBasedir(), JJTreeArgumentsBean.defSrcSubDir);
      if (jjtdir.exists()) {
        sourceDirectories.add(jjtdir);
      }
      final File jtbdir = new File(project.getBasedir(), JTBArgumentsBean.defSrcSubDir);
      if (jtbdir.exists()) {
        sourceDirectories.add(jtbdir);
      }
    }
    if (custom) {
      getLog().debug("custom sourceDirectories is '" + displayDirectories(sourceDirectories) + "'");
    } else {
      getLog().debug("no custom sourceDirectories, so initialized to '"
          + displayDirectories(sourceDirectories) + "'");
    }
    getLog().debug("jjdocReportsPage is '" + jjdocReportsPage + "'");
    getLog().debug("jjdocReportsDirectory is '" + jjdocReportsDirectory + "'");
    
    // mojo's log injected after the mojo is constructed, so wait until now to pass it to the bean
    jjdab.log = getLog();
    jjdab.setProcCmdLineArgs(jjdocCmdLineArgs);
    jjdab.findSpecificOptions(project, "jjdocCmdLineArgs");
  }
  
  @Override
  protected void processGrammar(final GrammarInfo grammarInfo) throws ProcessorException {
    getLog().debug("processGrammar '" + grammarInfo.getAbsoluteGrammarFile() + "'");
    final JJDocProcessor jjp = new JJDocProcessor(getLog());
    jjp.inputFile = grammarInfo.getAbsoluteGrammarFile();
    jjp.cmdLineArgs = jjdocCmdLineArgs;
    jjp.run();
    grammarInfo.setMainGeneratedFile(jjp.outputFileName);
  }
  
  @Override
  public String getName(final Locale locale) {
    return getBundle(locale).getString("report.jjdoc.name");
  }
  
  @Override
  protected File[] getProcessorOutputDirectories() {
    return jjdab.processorOutputDirectories;
  }
  
  @Override
  public String getDescription(final Locale locale) {
    return getBundle(locale).getString("report.jjdoc.short.description");
  }
  
  @Override
  protected String getOutputDirectory() {
    return jjdocReportsDirectory.getAbsolutePath();
  }
  
  @Override
  public String getOutputPath() {
    return jjdocReportsPage;
  }
  
  /**
   * @deprecated Use {@link #getOutputPath()} instead.
   * @see org.apache.maven.reporting.MavenReport#getOutputName()
   * @return The name of the main report file.
   */
  @Deprecated
  @Override
  public String getOutputName() {
    return getOutputPath();
  }
  
  //  /**
  //   * The JJDoc output file will have a <code>.html</code> or <code>.txt</code> extension depending
  //   * on the value of the parameters {@link #text} and {@link #bnf}.
  //   *
  //   * @return The file extension (including the leading period) to be used for the JJDoc output
  //   *         files.
  //   */
  //  private String getOutputFileExtension() {
  //    if (Boolean.TRUE.equals(text) || Boolean.TRUE.equals(bnf)) {
  //      return ".txt";
  //    } else {
  //      return ".html";
  //    }
  //  }
  
  @Override
  protected String getGrammarFileEncoding() {
    return (jjdab.grammarFileEncodingOptionValue == null)
        ? AbstractArgumentsBean.defaultGrammarFileEncoding
        : jjdab.grammarFileEncodingOptionValue;
  }
  
  @Override
  protected void createReportHeader(final ResourceBundle bundle, final Sink sink) {
    sink.head();
    sink.title();
    sink.text(bundle.getString("report.jjdoc.title"));
    sink.title_();
    sink.head_();
    
    sink.body();
    
    sink.section1();
    sink.sectionTitle1();
    sink.text(bundle.getString("report.jjdoc.title"));
    sink.sectionTitle1_();
    sink.text(bundle.getString("report.jjdoc.description"));
    sink.section1_();
    
    sink.lineBreak();
    sink.table();
    sink.tableRows();
    sink.tableRow();
    sink.tableHeaderCell();
    sink.text(bundle.getString("report.jjdoc.table.heading"));
    sink.tableHeaderCell_();
    sink.tableRow_();
  }
  
  @Override
  protected void createReportFooter(final Sink sink) {
    sink.tableRows_();
    sink.table_();
    sink.body_();
  }
  
  @Override
  protected void createReportLink(final Sink sink, final GrammarInfo grammarInfo) {
    sink.tableRow();
    sink.tableCell();
    final String jjdocFile = grammarInfo.getMainGeneratedFile();
    getLog().debug("jjdocFile = '" + jjdocFile + "'");
    sink.link(new File(jjdocFile).toURI().getPath());
    final File sourceDirectory = grammarInfo.getSourceDirectory();
    getLog().debug("sourceDirectory = '" + sourceDirectory.getName() + "', '"
        + sourceDirectory.getAbsoluteFile() + "', '"
        + sourceDirectory.getAbsoluteFile().toURI().toString() + "'");
    final File grammarFile = new File(sourceDirectory, grammarInfo.grammarFile);
    getLog().debug("grammarFile = '" + grammarFile.getName() + "'");
    String grammarFileRelativePath = sourceDirectory.toURI().relativize(grammarFile.toURI())
        .toString();
    getLog().debug("grammarFileRelativePath = '" + grammarFileRelativePath + "'");
    if (grammarFileRelativePath.startsWith("/")) {
      grammarFileRelativePath = grammarFileRelativePath.substring(1);
    }
    sink.text(grammarFileRelativePath);
    sink.link_();
    sink.tableCell_();
    sink.tableRow_();
    getLog().debug("createReportLink '" + jjdocFile + "', '" + grammarFileRelativePath + "'");
  }
  
  @Override
  protected ResourceBundle getBundle(final Locale locale) {
    return ResourceBundle.getBundle("jjdoc-report", locale, getClass().getClassLoader());
  }
}
