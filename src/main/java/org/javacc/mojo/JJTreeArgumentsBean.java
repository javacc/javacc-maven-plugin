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

import java.util.List;
import java.util.regex.Pattern;

import org.apache.maven.project.MavenProject;

/**
 * The bean handling JJTree options as command line arguments.
 *
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 */
public class JJTreeArgumentsBean extends AbstractArgumentsBean {
  
  /**
   * The list of single full command line arguments.
   *
   * @see JJTreeGoalMojo#jjtreeCmdLineArgs
   */
  protected List<String> jjtreeCmdLineArgs;
  
  @Override
  protected List<String> getProcCmdLineArgs() {
    return jjtreeCmdLineArgs;
  }
  
  @Override
  public void setProcCmdLineArgs(final List<String> argsList) {
    jjtreeCmdLineArgs = argsList;
  }
  
  /** The JJTree default source sub directory. */
  protected static final String defSrcSubDir = "src/main/jjtree";
  
  /** The JJTree default output sub directory. */
  protected static final String defOutSubDir = "generated-sources/jjtree";
  
  /** The command line argument to set the JJTree output directory option. */
  protected static final String argOutDir = "-JJTREE_OUTPUT_DIRECTORY";
  
  /** The regex to find the JJTree output directory option. With double quotes. */
  private static final String regexJjtOutDir = "-[jJ][jJ][tT][rR][eE][eE]_[oO][uU][tT][pP][uU][tT]_[dD][iI][rR][eE][cC][tT][oO][rR][yY][=:]\"(.*)\"";
  
  /** The pattern compiled from regex to find the jjtree output directory option. */
  private static final Pattern pattJjtOutDir = Pattern.compile(regexJjtOutDir);
  
  /** The array of patterns compiled from regex to find the output directories options. */
  protected static final Pattern[] patterns = new Pattern[] {
      pattJjtOutDir
  };
  
  /** The array of corresponding default output sub directories. */
  protected static final String[] defOutSubDirs = new String[] {
      defOutSubDir
  };
  
  @Override
  protected void findSpecificOptions(final MavenProject project, final String param)
      throws PluginException {
    super.findSpecificOptions(project, param, patterns, defOutSubDirs);
  }
}
