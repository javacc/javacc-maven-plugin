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
import java.util.Arrays;
import java.util.List;

import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * The <b>jjtree-javacc</b> goal, for generating the tree and the parser files from a JJTree
 * grammar.<br>
 * It searches the source directory for all grammar files and run JJTree and JavaCC once for each
 * file it finds, managing the output directory from the user setting or the default setting, and
 * adding it to the project-wide compile source roots.<br>
 * It uses an intermediate output directory for each processor to help discard any generated file
 * when the user wants instead his customized version he has put in any compile source root.
 * <p>
 * Detailed information about the JJTree and JavaCC options can be found on the
 * <a href="https://javacc.github.io/javacc/">JavaCC website</a>.<br>
 * The code repositories can be found within <a href="https://github.com/javacc">JavaCC at
 * GitHub</a>.
 * 
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 */
@Mojo(name = "jjtree-javacc", defaultPhase = LifecyclePhase.GENERATE_SOURCES, threadSafe = true)
public class JJTreeJavaCCGoalMojo extends AbstractPluginMojo {
  
  /** The bean handling JJTree options as command line arguments. */
  private final JJTreeArgumentsBean jjtab = new JJTreeArgumentsBean();
  
  /** The bean handling JavaCC options as command line arguments. */
  private final JavaCCArgumentsBean jjab = new JavaCCArgumentsBean();
  
  /**
   * @see JJTreeGoalMojo#jjtreeCmdLineArgs
   */
  @Parameter(property = "javacc.jjtreeCmdLineArgs") //
  protected List<String> jjtreeCmdLineArgs;
  
  /**
   * @see JavaCCGoalMojo#javaccCmdLineArgs
   */
  @Parameter(property = "javacc.javaccCmdLineArgs") //
  protected List<String> javaccCmdLineArgs;
  
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
    
    // jjtree
    jjtab.log = getLog();
    jjtab.setProcCmdLineArgs(jjtreeCmdLineArgs);
    jjtab.findSpecificOptions(project, "jjtreeCmdLineArgs");
    
    // javacc
    jjab.log = getLog();
    jjab.setProcCmdLineArgs(javaccCmdLineArgs);
    jjab.findSpecificOptions(project, "javaccCmdLineArgs");
    
    // check for consistency
    checkConsistentSpecificOptions(jjtab, jjab, "jjtree");
  }
  
  /** An index to memorize which processor is the current one in use. */
  int processorIndex = 1;
  
  @Override
  protected void processGrammar(final GrammarInfo grammarInfo) throws ProcessorException {
    // JJTree
    String ts = String.valueOf(System.currentTimeMillis()).substring(6);
    final File jjtree = new File(project.getBuild().getDirectory(), "jjtree-" + ts);
    File[] dirs = new File[] {
        jjtree
    };
    processorIndex = 1;
    runProcessorOnGrammar(grammarInfo, dirs, true);
    // JavaCC
    final GrammarInfo jjGrammarInfo = grammarInfo.deriveJJ(getProcessorOutputDirectories()[0]);
    ts = String.valueOf(System.currentTimeMillis()).substring(6);
    final File javacc = new File(project.getBuild().getDirectory(), "javacc-" + ts);
    dirs = new File[] {
        javacc
    };
    processorIndex = 2;
    runProcessorOnGrammar(jjGrammarInfo, dirs, false);
  }
  
  @Override
  protected void runProcessor(final File gram, final File[] dirs) throws ProcessorException {
    if (processorIndex == 1) {
      final JJTreeProcessor jjp = new JJTreeProcessor(getLog());
      jjp.inputFile = gram;
      jjp.intermediateOutputDirectories = dirs;
      jjp.intermedOutDirOptions = new String[] {
          JJTreeArgumentsBean.argOutDir
      };
      jjp.cmdLineArgs = jjtreeCmdLineArgs;
      jjp.run();
    } else {
      final JavaCCProcessor jjp = new JavaCCProcessor(getLog());
      jjp.inputFile = gram;
      jjp.intermediateOutputDirectories = dirs;
      jjp.intermedOutDirOptions = new String[] {
          JavaCCArgumentsBean.argOutDir
      };
      jjp.cmdLineArgs = javaccCmdLineArgs;
      jjp.run();
    }
  }
  
  /** The array of goal's output directories. */
  private File[] goalOutputDirectories = null;
  
  /**
   * Returns the option set in the <code>jjtree</code> arguments, or if none the option set in the
   * <code>javacc</code> arguments, or if none the default value.
   */
  @Override
  protected String getGrammarFileEncoding() {
    final String jjtGFE = jjtab.grammarFileEncodingOptionValue;
    final String jjGFE = jjab.grammarFileEncodingOptionValue;
    if (jjtGFE != null) {
      return jjtGFE;
    }
    if (jjGFE != null) {
      return jjGFE;
    }
    return AbstractArgumentsBean.defaultGrammarFileEncoding;
  }
  
  /**
   * Returns the option set in the <code>jjtree</code> arguments, or if none the option set in the
   * <code>javacc</code> arguments, or if none the default value.
   */
  @Override
  protected Language getLanguage() {
    final Language jjtLang = jjtab.languageOptionValue;
    final Language jjLang = jjab.languageOptionValue;
    if (jjtLang != null) {
      return jjtLang;
    }
    if (jjLang != null) {
      return jjLang;
    }
    return AbstractArgumentsBean.defaultLanguage;
  }
  
  @Override
  protected File[] getProcessorOutputDirectories() {
    if (processorIndex == 1) {
      return jjtab.processorOutputDirectories;
    } else {
      return jjab.processorOutputDirectories;
    }
  }
  
  @Override
  protected File[] getGoalOutputDirectories() {
    if (goalOutputDirectories == null) {
      goalOutputDirectories = Arrays.copyOf(jjtab.processorOutputDirectories,
          jjtab.processorOutputDirectories.length + jjab.processorOutputDirectories.length);
      System.arraycopy(jjab.processorOutputDirectories, 0, goalOutputDirectories,
          jjtab.processorOutputDirectories.length, jjab.processorOutputDirectories.length);
    }
    return goalOutputDirectories;
  }
}
