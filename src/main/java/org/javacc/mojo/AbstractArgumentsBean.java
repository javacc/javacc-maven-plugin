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
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;

/**
 * Super class of the concrete beans managing common specific processor arguments (options).
 *
 * <p>
 * As some goals run one processor and others run two or more of them, and<br>
 * as each processor has its own set of options, and<br>
 * as some processors (preprocessors) use part of other processors (generators) options, and<br>
 * as the (corresponding) parameters must be managed by the goal mojos,<br>
 * then<br>
 * in order to factorize code,<br>
 * the set of options / parameters are copied to and managed in the concrete beans, and <br>
 * the goal mojos handle references to one or more of these beans (a common way in Java to handle
 * multiple inheritance).
 *
 * <p>
 * Most of the processors options are transparent for the plugin, so none are declared as single
 * parameters as in previous versions of the plugin; since 3.8.0 they are declared as a List of
 * Strings holding each a full argument (like <code>-OPTION=value</code>); this for the benefit that
 * processors options additions or removals should not impact the plugin and therefore not lead to a
 * new release of the plugin.<br>
 * A few specific options are read and used by the plugin, in this class or in subclasses.
 *
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 * @see AbstractPluginMojo
 * @see AbstractProcessor
 */
abstract class AbstractArgumentsBean {
  
  /** The logger. */
  protected Log log;
  
  /**
   * Abstract getter for the list of single full command line arguments, which should be in the form
   * accepted by the processor.<br>
   *
   * @return the list of single full command line arguments
   */
  protected abstract List<String> getProcCmdLineArgs();
  
  /**
   * Abstract setter for the list of single full command line arguments, which should be in the form
   * accepted by the processor.<br>
   *
   * @param argsList - the list of single full command line arguments
   */
  protected abstract void setProcCmdLineArgs(List<String> argsList);
  
  /** The default grammar file encoding. */
  protected static final String defaultGrammarFileEncoding = System.getProperty("file.encoding");
  
  /** The grammar file encoding option value. */
  protected String grammarFileEncodingOptionValue = null;
  
  /** The default output language. */
  protected static final Language defaultLanguage = Language.JAVA;
  
  /** The output language option value. */
  protected Language languageOptionValue = null;
  
  /** The processor output directories (may be more than one). */
  protected File[] processorOutputDirectories = null;
  
  /** The regex to find the code generator option. */
  protected static final String codeGen = "-[cC][oO][dD][eE]_[gG][eE][nN][eE][rR][aA][tT][oO][rR][=:]\"(.*)\"";
  
  /** The regex to find the grammar encoding option. */
  protected static final String gramEnc = "-[gG][rR][aA][mM][mM][aA][rR]_[eE][nN][cC][oO][dD][iI][nN][gG][=:]\"(.*)\"";
  
  /**
   * Retrieve common specific processor options needed by the plugin.
   *
   * @param project - the current maven project
   * @param param - the command line arguments parameter name (for debug display)
   * @param procOutDirPatterns - the patterns compiled from the regex to find the processor output
   *          directories options
   * @param subDir - the sub directories of the project's build directory to form the default
   *          processor output directories
   * @throws PluginException if invalid language option value
   */
  protected void findSpecificOptions(final MavenProject project, final String param,
      final Pattern[] procOutDirPatterns, final String[] subDir) throws PluginException {
    
    if (procOutDirPatterns == null) {
      return;
    }
    
    final Pattern codeGenPatt = Pattern.compile(codeGen);
    final Pattern gramEncPatt = Pattern.compile(gramEnc);
    
    processorOutputDirectories = new File[procOutDirPatterns.length];
    
    for (final String arg : getProcCmdLineArgs()) {
      log.debug(param + " / arg = '" + arg + "'");
      Matcher matcher;
      String opt;
      
      matcher = codeGenPatt.matcher(arg);
      if (matcher.find()) {
        opt = matcher.group(1);
        log.debug("Code generator option is '" + opt + "', -> language");
        languageOptionValue = Language.getLanguageFrom(opt);
      }
      
      matcher = gramEncPatt.matcher(arg);
      if (matcher.find()) {
        opt = matcher.group(1);
        log.debug("Grammar file encoding option is '" + opt + "', -> grammarFileEncoding");
        grammarFileEncodingOptionValue = opt;
      }
      
      for (int i = 0; i < procOutDirPatterns.length; i++) {
        matcher = procOutDirPatterns[i].matcher(arg);
        if (matcher.find()) {
          opt = matcher.group(1);
          log.debug(
              "Output directory option is '" + opt + "', -> processorOutputDirectories[" + i + "]");
          processorOutputDirectories[i] = new File(opt);
        }
      } // end for outDirPatt 1
    } // end for getProcCmdLineArgs
    
    for (int i = 0; i < procOutDirPatterns.length; i++) {
      if (processorOutputDirectories[i] == null) {
        // if no option set, the plugin will copy the generated files to the build (sub)directory
        processorOutputDirectories[i] = new File(project.getBuild().getDirectory(), subDir[i]);
        log.debug("No output directory option set, defaulting to '"
            + processorOutputDirectories[i].getPath() + "', -> processorOutputDirectories[" + i
            + "]");
      }
      // if processorOutputDirectory does not exist, it will be created by the processor
      if (processorOutputDirectories[i].exists() && !processorOutputDirectories[i].isDirectory()) {
        throw new PluginException(
            "Output directory '" + processorOutputDirectories[i] + "' is an existing file");
      }
      
      try {
        final String podcp = processorOutputDirectories[i].getCanonicalPath();
        if (!podcp.equals(processorOutputDirectories[i].getPath())) {
          log.debug(
              "Output directory " + i + " transformed to its canonical pathname '" + podcp + "'");
          processorOutputDirectories[i] = new File(podcp);
        }
      }
      catch (final IOException e) {
        throw new PluginException("Output directory '" + processorOutputDirectories[i]
            + "' looks invalid: '" + e.getMessage() + "'");
      }
    } // end for outDirPatt 2
  }
  
  /**
   * Retrieve some specific processor options needed by the plugin.
   *
   * @param project - the current maven project
   * @param param - the command line arguments parameter name (for debug display)
   * @throws PluginException if invalid option value
   */
  protected abstract void findSpecificOptions(final MavenProject project, final String param)
      throws PluginException;
}
