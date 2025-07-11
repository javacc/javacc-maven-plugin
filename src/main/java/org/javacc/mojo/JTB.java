package org.javacc.mojo;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.StreamConsumer;

/** Provides a facade for the mojos to invoke JTB. */
class JTB extends ToolFacade {

  /** The default package name for syntax tree files. */
  private static final String SYNTAX_TREE = "syntaxtree";

  /** The default package name for visitor files. */
  private static final String VISITOR = "visitor";

  /** The input grammar. */
  private File inputFile;

  /** The base directory for the option "-o". */
  private File outputDirectory;

  /** The output directory for the syntax tree files. */
  private File nodeDirectory;

  /** The output directory for the visitor files. */
  private File visitorDirectory;

  /** The option "-p". */
  private String packageName;

  /** The option "-np". */
  private String nodePackageName;

  /** The option "-vp". */
  private String visitorPackageName;

  /** The option "-e". */
  private Boolean supressErrorChecking;

  /** The option "-jd". */
  private Boolean javadocFriendlyComments;

  /** The option "-f". */
  private Boolean descriptiveFieldNames;

  /** The option "-ns". */
  private String nodeParentClass;

  /** The option "-pp". */
  private Boolean parentPointers;

  /** The option "-tk". */
  private Boolean specialTokens;

  /** The toolkit option "-scheme". */
  private Boolean scheme;

  /** The toolkit option "-printer". */
  private Boolean printer;

  /**
   * Sets the absolute path to the grammar file to pass into JTB for preprocessing.
   *
   * @param value The absolute path to the grammar file to pass into JTB for preprocessing.
   */
  public void setInputFile(final File value) {
    if (value != null && !value.isAbsolute()) {
      throw new IllegalArgumentException("path is not absolute: " + value);
    }
    inputFile = value;
  }

  /**
   * Sets the absolute path to the output directory for the generated grammar file.
   *
   * @param value The absolute path to the output directory for the generated grammar file. If this
   *     directory does not exist yet, it is created. Note that this path should already include the
   *     desired package hierarchy because JTB will not append the required sub directories
   *     automatically.
   */
  public void setOutputDirectory(final File value) {
    if (value != null && !value.isAbsolute()) {
      throw new IllegalArgumentException("path is not absolute: " + value);
    }
    outputDirectory = value;
  }

  /**
   * Gets the absolute path to the enhanced grammar file generated by JTB.
   *
   * @return The absolute path to the enhanced grammar file generated by JTB or <code>null</code> if
   *     either the input file or the output directory have not been set.
   */
  public File getOutputFile() {
    File outputFile = null;
    if (outputDirectory != null && inputFile != null) {
      final String fileName = FileUtils.removeExtension(inputFile.getName()) + ".jj";
      outputFile = new File(outputDirectory, fileName);
    }
    return outputFile;
  }

  /**
   * Sets the absolute path to the output directory for the syntax tree files.
   *
   * @param value The absolute path to the output directory for the generated syntax tree files, may
   *     be <code>null</code> to use a sub directory in the output directory of the grammar file. If
   *     this directory does not exist yet, it is created. Note that this path should already
   *     include the desired package hierarchy because JTB will not append the required sub
   *     directories automatically.
   */
  public void setNodeDirectory(final File value) {
    if (value != null && !value.isAbsolute()) {
      throw new IllegalArgumentException("path is not absolute: " + value);
    }
    nodeDirectory = value;
  }

  /**
   * Gets the absolute path to the output directory for the syntax tree files.
   *
   * @return The absolute path to the output directory for the syntax tree files, only <code>null
   *     </code> if neither {@link #outputDirectory} nor {@link #nodeDirectory} have been set.
   */
  private File getEffectiveNodeDirectory() {
    if (nodeDirectory != null) {
      return nodeDirectory;
    } else if (outputDirectory != null) {
      return new File(outputDirectory, getLastPackageName(getEffectiveNodePackageName()));
    }
    return null;
  }

  /**
   * Sets the absolute path to the output directory for the visitor files.
   *
   * @param value The absolute path to the output directory for the generated visitor files, may be
   *     <code>null</code> to use a sub directory in the output directory of the grammar file. If
   *     this directory does not exist yet, it is created. Note that this path should already
   *     include the desired package hierarchy because JTB will not append the required sub
   *     directories automatically.
   */
  public void setVisitorDirectory(final File value) {
    if (value != null && !value.isAbsolute()) {
      throw new IllegalArgumentException("path is not absolute: " + value);
    }
    visitorDirectory = value;
  }

  /**
   * Gets the absolute path to the output directory for the visitor files.
   *
   * @return The absolute path to the output directory for the visitor, only <code>null</code> if
   *     neither {@link #outputDirectory} nor {@link #visitorDirectory} have been set.
   */
  private File getEffectiveVisitorDirectory() {
    if (visitorDirectory != null) {
      return visitorDirectory;
    } else if (outputDirectory != null) {
      return new File(outputDirectory, getLastPackageName(getEffectiveVisitorPackageName()));
    }
    return null;
  }

  /**
   * Sets the option "-p". Will overwrite the options "-np" and "-vp" if specified.
   *
   * @param value The option value, may be <code>null</code>.
   */
  public void setPackageName(final String value) {
    packageName = value;
  }

  /**
   * Sets the option "-np".
   *
   * @param value The option value, may be <code>null</code>.
   */
  public void setNodePackageName(final String value) {
    nodePackageName = value;
  }

  /**
   * Gets the effective package name for the syntax tree files.
   *
   * @return The effective package name for the syntax tree files, never <code>null</code>.
   */
  private String getEffectiveNodePackageName() {
    if (packageName != null) {
      return (packageName.length() <= 0) ? SYNTAX_TREE : packageName + '.' + SYNTAX_TREE;
    } else if (nodePackageName != null) {
      return nodePackageName;
    } else {
      return SYNTAX_TREE;
    }
  }

  /**
   * Sets the option "-vp".
   *
   * @param value The option value, may be <code>null</code>.
   */
  public void setVisitorPackageName(final String value) {
    visitorPackageName = value;
  }

  /**
   * Gets the effective package name for the visitor files.
   *
   * @return The effective package name for the visitor files, never <code>null</code>.
   */
  private String getEffectiveVisitorPackageName() {
    if (packageName != null) {
      return (packageName.length() <= 0) ? VISITOR : packageName + '.' + VISITOR;
    } else if (visitorPackageName != null) {
      return visitorPackageName;
    } else {
      return VISITOR;
    }
  }

  /**
   * Sets the option "-e".
   *
   * @param value The option value, may be <code>null</code>.
   */
  public void setSupressErrorChecking(final Boolean value) {
    supressErrorChecking = value;
  }

  /**
   * Sets the option "-jd".
   *
   * @param value The option value, may be <code>null</code>.
   */
  public void setJavadocFriendlyComments(final Boolean value) {
    javadocFriendlyComments = value;
  }

  /**
   * Sets the option "-f".
   *
   * @param value The option value, may be <code>null</code>.
   */
  public void setDescriptiveFieldNames(final Boolean value) {
    descriptiveFieldNames = value;
  }

  /**
   * Sets the option "-ns".
   *
   * @param value The option value, may be <code>null</code>.
   */
  public void setNodeParentClass(final String value) {
    nodeParentClass = value;
  }

  /**
   * Sets the option "-pp".
   *
   * @param value The option value, may be <code>null</code>.
   */
  public void setParentPointers(final Boolean value) {
    parentPointers = value;
  }

  /**
   * Sets the option "-tk".
   *
   * @param value The option value, may be <code>null</code>.
   */
  public void setSpecialTokens(final Boolean value) {
    specialTokens = value;
  }

  /**
   * Sets the toolkit option "-scheme".
   *
   * @param value The option value, may be <code>null</code>.
   */
  public void setScheme(final Boolean value) {
    scheme = value;
  }

  /**
   * Sets the toolkit option "-printer".
   *
   * @param value The option value, may be <code>null</code>.
   */
  public void setPrinter(final Boolean value) {
    printer = value;
  }

  /** {@inheritDoc} */
  @Override
  protected int execute() throws Exception {
    final String[] args = generateArguments();

    if (outputDirectory != null && !outputDirectory.exists()) {
      outputDirectory.mkdirs();
    }

    // fork JTB because of its lack to re-initialize its static parser
    final ForkedJvm jvm = new ForkedJvm();
    jvm.setMainClass("EDU.purdue.jtb.JTB");
    jvm.addArguments(args);
    jvm.setSystemOut(new MojoLogStreamConsumer(false));
    jvm.setSystemErr(new MojoLogStreamConsumer(true));
    if (getLog().isDebugEnabled()) {
      getLog().debug("Forking: " + jvm);
    }
    final int exitcode = jvm.run();

    moveJavaFiles();

    return exitcode;
  }

  /**
   * Assembles the command line arguments for the invocation of JTB according to the configuration.
   *
   * @return A string array that represents the command line arguments to use for JTB.
   */
  private String[] generateArguments() {
    final List<String> argsList = new ArrayList<String>();

    argsList.add("-np");
    argsList.add(getEffectiveNodePackageName());

    argsList.add("-vp");
    argsList.add(getEffectiveVisitorPackageName());

    if (supressErrorChecking != null && supressErrorChecking.booleanValue()) {
      argsList.add("-e");
    }

    if (javadocFriendlyComments != null && javadocFriendlyComments.booleanValue()) {
      argsList.add("-jd");
    }

    if (descriptiveFieldNames != null && descriptiveFieldNames.booleanValue()) {
      argsList.add("-f");
    }

    if (nodeParentClass != null) {
      argsList.add("-ns");
      argsList.add(nodeParentClass);
    }

    if (parentPointers != null && parentPointers.booleanValue()) {
      argsList.add("-pp");
    }

    if (specialTokens != null && specialTokens.booleanValue()) {
      argsList.add("-tk");
    }

    if (scheme != null && scheme.booleanValue()) {
      argsList.add("-scheme");
    }

    if (printer != null && printer.booleanValue()) {
      argsList.add("-printer");
    }

    final File outputFile = getOutputFile();
    if (outputFile != null) {
      argsList.add("-o");
      argsList.add(outputFile.getAbsolutePath());
    }

    if (inputFile != null) {
      argsList.add(inputFile.getAbsolutePath());
    }

    return (String[]) argsList.toArray(new String[argsList.size()]);
  }

  /**
   * Gets the last identifier from the specified package name. For example, returns "apache" upon
   * input of "org.apache". JTB uses this approach to derive the output directories for the visitor
   * and syntax tree files.
   *
   * @param name The package name from which to retrieve the last sub package, may be <code>null
   *     </code>.
   * @return The name of the last sub package or <code>null</code> if the input was <code>null
   *     </code>
   */
  private String getLastPackageName(final String name) {
    if (name != null) {
      return name.substring(name.lastIndexOf('.') + 1);
    }
    return null;
  }

  /**
   * Moves the previously generated Java files to their proper target directories. JTB simply
   * assumes that the current working directory represents the parent package of the configured
   * node/visitor packages which does not meet our needs.
   *
   * @throws IOException If the move failed.
   */
  private void moveJavaFiles() throws IOException {
    final File nodeSrcDir =
        new File(getLastPackageName(getEffectiveNodePackageName())).getAbsoluteFile();
    final File nodeDstDir = getEffectiveNodeDirectory();
    moveDirectory(nodeSrcDir, nodeDstDir);

    final File visitorSrcDir =
        new File(getLastPackageName(getEffectiveVisitorPackageName())).getAbsoluteFile();
    final File visitorDstDir = getEffectiveVisitorDirectory();
    moveDirectory(visitorSrcDir, visitorDstDir);
  }

  /**
   * Moves all Java files generated by JTB from the specified source directory to the given target
   * directory. Existing files in the target directory will be overwritten. Note that this move
   * assumes a flat source directory, i.e. copying of sub directories is not supported.<br>
   * <br>
   * This method must be used instead of {@link java.io.File#renameTo(java.io.File)} which would
   * fail if the target directory already existed (at least on Windows).
   *
   * @param sourceDir The absolute path to the source directory, must not be <code>null</code>.
   * @param targetDir The absolute path to the target directory, must not be <code>null</code>.
   * @throws IOException If the move failed.
   */
  private void moveDirectory(final File sourceDir, final File targetDir) throws IOException {
    getLog().debug("Moving JTB output files: " + sourceDir + " -> " + targetDir);
    /*
     * NOTE: The source directory might be the current working directory if JTB was told to output into the default
     * package. The current working directory might be quite anything and will likely contain sub directories not
     * created by JTB. Therefore, we do a defensive move and only delete the expected Java source files.
     */
    final File[] sourceFiles = sourceDir.listFiles();
    if (sourceFiles == null) {
      return;
    }
    for (int i = 0; i < sourceFiles.length; i++) {
      final File sourceFile = sourceFiles[i];
      if (sourceFile.isFile() && sourceFile.getName().endsWith(".java")) {
        try {
          FileUtils.copyFileToDirectory(sourceFile, targetDir);
          if (!sourceFile.delete()) {
            getLog().error("Failed to delete original JTB output file: " + sourceFile);
          }
        } catch (final Exception e) {
          throw new IOException(
              "Failed to move JTB output file: " + sourceFile + " -> " + targetDir);
        }
      }
    }
    if (sourceDir.list().length <= 0) {
      if (!sourceDir.delete()) {
        getLog().error("Failed to delete original JTB output directory: " + sourceDir);
      }
    } else {
      getLog().debug("Keeping non empty JTB output directory: " + sourceDir);
    }
  }

  /**
   * Gets a string representation of the command line arguments.
   *
   * @return A string representation of the command line arguments.
   */
  @Override
  public String toString() {
    return Arrays.asList(generateArguments()).toString();
  }

  /** Consume and log command line output from the JJDoc process. */
  class MojoLogStreamConsumer implements StreamConsumer {

    /** The line prefix used by JTB to report infos. */
    private static final String INFO_PREFIX = "JTB: ";

    /**
     * Determines if the stream consumer is being used for <code>System.out</code> or <code>
     * System.err</code>.
     */
    private final boolean err;

    /**
     * Single param constructor.
     *
     * @param error If set to <code>true</code>, all consumed lines will be logged at the error
     *     level.
     */
    public MojoLogStreamConsumer(final boolean error) {
      err = error;
    }

    /**
     * Consume a line of text.
     *
     * @param line The line to consume.
     */
    @Override
    public void consumeLine(final String line) {
      if (line.startsWith("JTB version")) {
        getLog().debug(line);
      } else if (line.startsWith(INFO_PREFIX)) {
        getLog().debug(line.substring(INFO_PREFIX.length()));
      } else if (err && line.length() > 0) {
        getLog().error(line);
      } else {
        getLog().debug(line);
      }
    }
  }
}
