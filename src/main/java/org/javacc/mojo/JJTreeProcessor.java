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

import java.util.Arrays;
import java.util.regex.Pattern;

import org.apache.maven.plugin.logging.Log;

/**
 * Holds parameters/options and execution for the JJTree processor.
 *
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 */
public class JJTreeProcessor extends AbstractProcessor {
  
  /**
   * Constructor.
   *
   * @param lg - the logger
   */
  public JJTreeProcessor(final Log lg) {
    super(lg);
  }
  
  @Override
  protected int execute() throws ProcessorException {
    final String[] args = genArgs();
    log.debug("Running 'org.javacc.jjtree.Main.mainProgram()' with arguments: "
        + Arrays.asList(genArgs()).toString());
    try {
      return org.javacc.jjtree.Main.mainProgram(args);
    }
    catch (final Exception e) {
      // note-jacoco: quite impossible to set a test case for here, jjtree does not throw
      // checked exceptions
      throw new ProcessorException(
          "Error running 'org.javacc.jjtree.Main.mainProgram()' with arguments: "
              + Arrays.asList(genArgs()).toString(),
          e);
    }
  }
  
  @Override
  boolean matchOverridenOption(final String arg) {
    for (final Pattern p : JJTreeArgumentsBean.patterns) {
      if (p.matcher(arg).find()) {
        return true;
      }
    }
    return false;
  }
}
