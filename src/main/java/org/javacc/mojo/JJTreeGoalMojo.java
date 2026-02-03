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
 * The <b>jjtree</b> goal, for generating the tree files (but not the parser files) from a JJTree
 * grammar.<br>
 * It searches the source directory for all grammar files and run JJTree once for each file it
 * finds, managing the output directory from the user setting or the default setting, and adding it
 * to the project-wide compile source roots.<br>
 * It uses an intermediate output directory to help discard any generated file when the user wants
 * instead his customized version he has put in any compile source root.
 * <p>
 * Detailed information about the JJTree options can be found on the
 * <a href="https://javacc.github.io/javacc/">JavaCC website</a>.<br>
 * The code repositories can be found within <a href="https://github.com/javacc">JavaCC at
 * GitHub</a>.
 *
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 */
@Mojo(name = "jjtree", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class JJTreeGoalMojo extends AbstractPluginMojo {
  
  /** The bean handling JJTree options as command line arguments. */
  private final JJTreeArgumentsBean jjtab = new JJTreeArgumentsBean();
  
  /**
   * The list of single full command line arguments, which should be in the form accepted by
   * JJTree/JavaCC.
   *
   * <p>
   * Arguments' values for <code>-CODE_GENERATOR</code>, <code>-GRAMMAR_ENCODING</code> and
   * <code>-JJTREE_OUTPUT_DIRECTORY</code> are read and used by the plugin; their default values
   * (i.e. if these options are not present) are <code>"Java"</code> (as for JJTree/JavaCC), system
   * property <code>file.encoding</code> (as for JJTree/JavaCC) and <code>
   * ${project.build.directory}/generated-sources/jjtree</code> (as for usual maven convention -
   * JJTree default value is <code>"."</code>).<br>
   *
   * <p>
   * The list will be passed as it is to JJTree, no control nor modification is done by the plugin,
   * except for <code>-JJTREE_OUTPUT_DIRECTORY</code>, whose value will be changed to an
   * intermediate temporary directory <code>${project.build.directory}/jjtree-nnnnnn</code>. The
   * given output directory value will be used at the end to copy the generated files from the
   * intermediate directory to it.
   *
   * <p>
   * The list has no default value (except for the 3 above options) (but JJTree/JavaCC itself
   * supplies default values).<br>
   * Note that among those 3 default values, the grammar encoding one (platform file encoding) may
   * be not adequate as most of the times the project is under an IDE and globally set to a platform
   * independent encoding like 'UTF-8'.
   *
   * <p>
   * Example:<br>
   *
   * <pre>{@code
   * <jjtreeCmdLineArgs>
   *   <arg>-NODE_EXTENDS="MyNode"</arg>
   *   <arg>-MuLtI=true</arg>
   *   <arg>-code_generator:"Java"</arg>
   *   <arg>-Grammar_Encoding="UTF-8"</arg>
   *   <arg>-JJTREE_OUTPUT_DIRECTORY="$}{{@code
   * project.build.directory
   * }}{@code /gen-src/jjt"</arg>
   * </jjtreeCmdLineArgs>
   * }</pre>
   *
   * <br>
   * Note that the <code>jjtreeCmdLineArgs</code> parameter is of type {@code List<String>}, which
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
  @Parameter(property = "javacc.jjtreeCmdLineArgs") //
  protected List<String> jjtreeCmdLineArgs;
  
  @Override
  protected void initProcessor() throws PluginException {
    if (includes == null) {
      includes = new String[] {
          "**/*.jjt"
      };
    }
    if (sourceDirectory == null) {
      sourceDirectory = new File(project.getBasedir(), JJTreeArgumentsBean.defSrcSubDir);
      getLog().debug("sourceDirectory initialized to '" + sourceDirectory + "'");
    }
    
    // mojo's log injected after the mojo is constructed, so wait until now to pass it to the bean
    jjtab.log = getLog();
    jjtab.setProcCmdLineArgs(jjtreeCmdLineArgs);
    jjtab.findSpecificOptions(project, "jjtreeCmdLineArgs");
  }
  
  @Override
  protected void processGrammar(final GrammarInfo grammarInfo) throws ProcessorException {
    final String ts = String.valueOf(System.currentTimeMillis()).substring(6);
    final File jjtree = new File(project.getBuild().getDirectory(), "jjtree-" + ts);
    final File[] dirs = new File[] {
        jjtree
    };
    runProcessorOnGrammar(grammarInfo, dirs, true);
  }
  
  @Override
  protected void runProcessor(final File gram, final File[] dirs) throws ProcessorException {
    final JJTreeProcessor jjp = new JJTreeProcessor(getLog());
    jjp.inputFile = gram;
    jjp.intermediateOutputDirectories = dirs;
    jjp.intermedOutDirOptions = new String[] {
        JJTreeArgumentsBean.argOutDir
    };
    jjp.cmdLineArgs = jjtreeCmdLineArgs;
    jjp.run();
  }
  
  @Override
  protected String getGrammarFileEncoding() {
    return (jjtab.grammarFileEncodingOptionValue == null)
        ? AbstractArgumentsBean.defaultGrammarFileEncoding
        : jjtab.grammarFileEncodingOptionValue;
  }
  
  @Override
  protected Language getLanguage() {
    return jjtab.languageOptionValue == null ? AbstractArgumentsBean.defaultLanguage
        : jjtab.languageOptionValue;
  }
  
  @Override
  protected File[] getProcessorOutputDirectories() {
    return jjtab.processorOutputDirectories;
  }
  
  @Override
  protected File[] getGoalOutputDirectories() {
    return jjtab.processorOutputDirectories;
  }
}
