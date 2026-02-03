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
import java.util.List;

import org.apache.maven.plugin.logging.Log;

/**
 * Super class of the concrete processors.
 *
 * <p>
 * Each processor is a facade to programmatically run a JavaCC generator or a preprocessor (JJTree
 * or JTB) or the JJDoc tool.<br>
 * Some goals may run a preprocessor and a generator in a row.
 *
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 * @see AbstractPluginMojo
 * @see AbstractArgumentsBean
 */
abstract class AbstractProcessor {
  
  /** The logger. */
  protected final Log log;
  
  /** The the absolute path to the (input) grammar file. */
  protected File inputFile;
  
  /** The output file name. */
  protected String outputFileName;
  
  /** The array of absolute paths to the intermediate output directories. */
  protected File[] intermediateOutputDirectories = null;
  
  /** The array of options to set the intermediate output directories. */
  protected String[] intermedOutDirOptions = null;
  
  /** The command line arguments. */
  protected List<String> cmdLineArgs;
  
  /**
   * Constructor.
   *
   * @param lg - the logger
   */
  public AbstractProcessor(final Log lg) {
    log = lg;
  }
  
  /**
   * Gets the name of the processor.
   *
   * @return the name of the processor, never <code>null</code>
   */
  protected String getProcessorName() {
    final String name = getClass().getName();
    return name.substring(name.lastIndexOf('.') + 1);
  }
  
  /**
   * Runs the processor, wrapping return info; its parameters/options must have been set previously.
   *
   * @throws ProcessorException if the processor could not be invoked or reported a non-zero return
   *           code
   */
  public void run() throws ProcessorException {
    int rc;
    try {
      log.debug("Running '" + getProcessorName() + "'");
      rc = execute();
      log.debug("'" + getProcessorName() + "' returned '" + rc + "'");
    }
    catch (final Exception e) {
      // note-jacoco: quite impossible to set a test case here, as processors throw exceptions
      // mainly on IO problems, and for unsupported encoding the plugin raises before reaching
      // here a GrammarException while trying to read the grammar file
      throw new ProcessorException(
          "Failed to execute processor '" + getProcessorName() + "': " + e.getMessage(), e);
    }
    if (rc != 0) {
      throw new ProcessorException(
          "Processor '" + getProcessorName() + "' reported exit code '" + rc + "'");
    }
  }
  
  /**
   * Delegates execution of the processor to the concrete class; its parameters/options must have
   * been set previously.
   *
   * @return the exit code of the processor, non-zero means failure
   * @throws ProcessorException if the processor could not be invoked
   */
  protected abstract int execute() throws ProcessorException;
  
  /**
   * Generates the array of arguments, from the configured options, the intermediate output
   * directory and the input file.
   *
   * @return the arguments
   */
  protected String[] genArgs() {
    final List<String> argsList = new ArrayList<String>(cmdLineArgs.size() + 2);
    for (final String arg : cmdLineArgs) {
      if (!matchOverridenOption(arg)) {
        argsList.add(arg);
      }
    }
    if (intermediateOutputDirectories != null) {
      for (int i = 0; i < intermediateOutputDirectories.length; i++) {
        argsList.add(intermedOutDirOptions[i] + "=\"" + intermediateOutputDirectories[i] + "\"");
      }
    }
    argsList.add(inputFile.getAbsolutePath());
    return (String[]) argsList.toArray(new String[argsList.size()]);
  }
  
  /**
   * @param arg - the argument
   * @return true if the given argument matches one of the option overriden by the plugin, false
   *         otherwise.
   */
  abstract boolean matchOverridenOption(final String arg);
  
}
