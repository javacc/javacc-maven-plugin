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
import java.util.List;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * The <b>jtb</b> goal, for generating the tree files (but not the parser files) from a JTB
 * grammar.<br>
 * It searches the source directory for all grammar files and run JTB once for each file it finds,
 * managing the output directory from the user setting or the default setting, and adding it to the
 * project-wide compile source roots.<br>
 * It uses an intermediate output directory to help discard any generated file when the user wants
 * instead his customized version he has put in any compile source root.
 * <p>
 * Detailed information about the JTB options can be found on the
 * <a href="https://github.com/jtb-javacc/JTB/blob/master/doc/wiki/How_to_use.md">JTB wiki</a>.<br>
 * The code repository can be found within <a href="https://github.com/jtb-javacc">JTB at
 * GitHub</a>.
 *
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 */
@Mojo(name = "jtb", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class JTBGoalMojo extends AbstractPluginMojo {
  
  /** The bean handling JTB options as command line arguments. */
  private final JTBArgumentsBean jtbab = new JTBArgumentsBean();
  
  /**
   * The list of single full command line arguments, which should be in the form accepted by
   * JTB/JavaCC.
   *
   * <p>
   * Arguments' values for <code>-CODE_GENERATOR</code>, <code>-GRAMMAR_ENCODING</code>, <code>-d
   * </code> or <code>-JTB_D</code> and <code>-OUTPUT_DIRECTORY</code> are read and used by the
   * plugin; their default values (i.e. if these options are not present) are <code>"Java"</code>
   * (as for JTB/JavaCC), system property <code>file.encoding</code> (as for JTB/JavaCC) <code>
   * ${project.build.directory}/generated-sources/jtb</code> and <code>
   * ${project.build.directory}/generated-sources/javacc</code> (as usual maven convention, which is
   * different from JavaCC and JTB default values which are <code>"."</code>).<br>
   *
   * <p>
   * The list will be passed as it is to JTB, no control nor modification is done by the plugin,
   * except for <code>-d</code> or <code>-JTB_D</code>, whose value will be changed to an
   * intermediate temporary directory <code>
   * ${project.build.directory}/jtb-nnnnnn</code>. The given output directory value will be used at
   * the end to copy the generated files from the intermediate directory to it.
   *
   * <p>
   * The list has no default value (except for the 3 above options) (but JTB/JavaCC itself supplies
   * default values).<br>
   * Note that among those 3 default values, the grammar encoding one (platform file encoding) may
   * be not adequate as most of the times the project is under an IDE and globally set to a platform
   * independent encoding like 'UTF-8'.
   *
   * <p>
   * Example:<br>
   *
   * <pre>{@code
   * <jtbCmdLineArgs>
   *   <arg>-ns="MyNode"</arg>
   *   <arg>-JTB_VIS=false</arg>
   *   <arg>-code_generator:"Java"</arg>
   *   <arg>-Grammar_Encoding="UTF-8"</arg>
   *   <arg>-d="$}{{@code
   * project.build.directory
   * }}{@code /gen-src/jtb"</arg>
   *   <arg>-OUTPUT_DIRECTORY="$}{{@code
   * project.build.directory
   * }}{@code /gen-src/jj"</arg>
   * </jtbCmdLineArgs>
   * }</pre>
   *
   * <br>
   * Note that the <code>jtbCmdLineArgs</code> parameter is of type {@code List<String>}, which
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
   * See also
   * <a href= "https://github.com/javacc/javacc-maven-plugin#processor-parameters">Processor
   * parameters</a>.
   */
  @Parameter(property = "javacc.jtbCmdLineArgs") //
  protected List<String> jtbCmdLineArgs;
  
  @Override
  protected void initProcessor() throws PluginException {
    if (includes == null) {
      // note-jacoco: could add a test case without specific include, would be quite worthless
      includes = new String[] {
          "**/*.jtb"
      };
    }
    if (sourceDirectory == null) {
      sourceDirectory = new File(project.getBasedir(), JTBArgumentsBean.defSrcSubDir);
      getLog().debug("sourceDirectory initialized to '" + sourceDirectory + "'");
    }
    
    // mojo's log injected after the mojo is constructed, so wait until now to pass it to the bean
    jtbab.log = getLog();
    jtbab.setProcCmdLineArgs(jtbCmdLineArgs);
    jtbab.findSpecificOptions(project, "jtbCmdLineArgs");
  }
  
  @Override
  protected void processGrammar(final GrammarInfo grammarInfo) throws ProcessorException {
    final String ts = String.valueOf(System.currentTimeMillis()).substring(6);
    final File jtb = new File(project.getBuild().getDirectory(), "jtb-" + ts);
    final File tk = new File(project.getBuild().getDirectory(), "jjtk-" + ts);
    final File[] dirs = new File[] {
        jtb, tk
    };
    // TODO command line package and parsersubdirectory to modify grammarInfo
    runProcessorOnGrammar(grammarInfo, dirs, true);
  }
  
  @Override
  protected void runProcessor(final File gram, final File[] dirs) throws ProcessorException {
    final JTBProcessor jjp = new JTBProcessor(getLog());
    jjp.inputFile = gram;
    jjp.intermediateOutputDirectories = dirs;
    jjp.intermedOutDirOptions = new String[] {
        JTBArgumentsBean.argOutDir, JavaCCArgumentsBean.argOutDir
    };
    jjp.cmdLineArgs = jtbab.jtbCmdLineArgs;
    jjp.run();
  }
  
  @Override
  protected String getGrammarFileEncoding() {
    return (jtbab.grammarFileEncodingOptionValue == null)
        ? AbstractArgumentsBean.defaultGrammarFileEncoding
        : jtbab.grammarFileEncodingOptionValue;
  }
  
  @Override
  protected Language getLanguage() {
    return jtbab.languageOptionValue == null ? AbstractArgumentsBean.defaultLanguage
        : jtbab.languageOptionValue;
  }
  
  @Override
  protected File[] getProcessorOutputDirectories() {
    return jtbab.processorOutputDirectories;
  }
  
  @Override
  protected File[] getGoalOutputDirectories() {
    return jtbab.processorOutputDirectories;
  }
}
