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

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.util.DirectoryScanner;

/**
 * Scans source directories for JavaCC / JJTree / JTB grammar files, performing stale detection.
 *
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 */
class GrammarDirectoryScanner {
  
  /** The logger. */
  private final Log log;
  
  /** The output language. */
  private final Language language;
  
  /** The grammar file encoding. */
  private final String encoding;
  
  /** The directory scanner used to scan the source directory for files. */
  private final DirectoryScanner src_ds;
  
  /** The directory scanner used to scan the target directory for files. */
  private final DirectoryScanner tgt_ds;
  
  /**
   * The array of absolute path to the output directories used to detect stale target files by
   * timestamp checking, may be <code>null</code> if no stale detection should be performed.
   */
  private File[] outputDirectories;
  
  /**
   * The delta in milliseconds of the last modification timestamp for testing whether a grammar file
   * needs recompilation because its main generated file is stale.
   */
  private long timestampDeltaMs;
  
  /**
   * Creates a new grammar directory scanner.
   *
   * @param lg - the Log to use
   * @param lang - the Language to generate for
   * @param enc - the grammar file encoding
   */
  public GrammarDirectoryScanner(final Log lg, final Language lang, final String enc) {
    log = lg;
    language = lang;
    encoding = enc;
    src_ds = new DirectoryScanner();
    src_ds.setFollowSymlinks(true);
    tgt_ds = new DirectoryScanner();
    tgt_ds.setFollowSymlinks(true);
  }
  
  /** The most recent lastModified timestamp of the dependencies jars. */
  private long jarsLastTS = 0;
  
  /**
   * Setter.
   *
   * @param ts - the most recent lastModified timestamp of the dependencies jars
   */
  public void setJarsLastTS(final long ts) {
    jarsLastTS = ts;
  }
  
  /**
   * Setter.
   *
   * @param directories - the arrays of absolute path to the output directories used to detect stale
   *          target files by timestamp checking, should not be <code>null</code>.
   */
  public void setOutputDirectories(final File[] directories) {
    outputDirectories = directories;
  }
  
  /**
   * Setter.
   *
   * @param milliseconds - the delta in milliseconds of the last modification timestamp
   */
  public void setTimestampDeltaMs(final long milliseconds) {
    timestampDeltaMs = milliseconds;
  }
  
  /**
   * Sets the directory scanner with its base directory.<br>
   * This directory must exist or the scanner will report an error.
   *
   * @param directory - the absolute path to the source directory to scan, must not be <code>null
   *     </code>
   */
  public void dsSetBasedir(final File directory) {
    src_ds.setBasedir(directory);
  }
  
  /**
   * Sets the directory scanner with its inclusion pattern.
   *
   * @param includes - the set of Ant-like inclusion patterns, may be <code>null</code> to include
   *          all files
   */
  public void dsSetIncludes(final String[] includes) {
    src_ds.setIncludes(includes);
  }
  
  /**
   * Sets the directory scanner with its exclusion patterns.
   *
   * @param excludes - the set of Ant-like exclusion patterns, may be <code>null</code> to exclude
   *          no files
   */
  public void dsSetExcludes(final String[] excludes) {
    src_ds.setExcludes(excludes);
    src_ds.addDefaultExcludes();
  }
  
  /**
   * Scans the source directory for grammar files that match at least one inclusion pattern but no
   * exclusion pattern, performing timestamp checking to include grammars that are older than the
   * dependent jars and optionally performing timestamp checking to exclude grammars whose
   * corresponding parser files are up to date.
   *
   * @param failOnGrammarError - the plugin parameter failOnGrammarError value
   * @return a list of grammar infos, may be <code>null</code> if no grammar in the directory, and
   *         may be empty if no stale grammar found
   * @throws GrammarException if reading the grammar file failed, or if no parser name can be
   *           retrieved in the grammar
   */
  public List<GrammarInfo> scanForGrammars(final String failOnGrammarError)
      throws GrammarException {
    
    // here we do not catch the IllegalStateException as basedir (previously set to sourceDirectory)
    // has already (normally) be checked as not null, existing and being a directory
    src_ds.scan();
    final String[] includedFiles = src_ds.getIncludedFiles();
    
    if (includedFiles.length == 0) {
      return null;
    }
    
    final List<GrammarInfo> includedGrammars = new ArrayList<GrammarInfo>();
    int nbError = 0;
    for (final String includedFile : includedFiles) {
      log.debug("IncludedFile = '" + includedFile + "'");
      
      GrammarInfo grammarInfo = null;
      try {
        grammarInfo = new GrammarInfo(log, language, encoding, src_ds.getBasedir(), includedFile);
      }
      catch (final GrammarException e) {
        if ("first".equals(failOnGrammarError)) {
          // 'first': let it be handled above
          throw e;
        } else {
          // 'last' or 'ignore': continue on next grammar
          nbError++;
          log.error(e.getMessage());
          log.info(
              "Continuing finding grammars as failOnGrammarError is set to 'last' or 'ignore'");
          continue;
        }
      }
      final File grammarFile = grammarInfo.getAbsoluteGrammarFile();
      final long grammarTS = grammarFile.lastModified();
      log.debug(
          "LastModified timestamp of grammar file '" + grammarFile + "' is '" + grammarTS + "'");
      
      if (timestampDeltaMs >= 0) {
        // stale detection needed
        for (final File outDir : outputDirectories) {
          if (includedGrammars.contains(grammarInfo)) {
            // (do not to include more than once a grammar)
            break;
          }
          log.debug("Getting target files on '" + outDir + "'");
          final File[] targetFiles = getTargetFile(outDir, grammarInfo);
          if (targetFiles == null //
              || targetFiles.length == 0) {
            // no generated file
            includedGrammars.add(grammarInfo);
            log.info(
                "Grammar file '" + grammarFile + "' included as no existing main generated file");
          } else {
            for (final File targetFile : targetFiles) {
              final long targetTS = targetFile.lastModified();
              log.debug("LastModified timestamp of main generated file '" + targetFile + "' is '"
                  + targetTS + "'");
              if (targetTS + timestampDeltaMs < grammarTS) {
                // grammar file more recent than generated file
                includedGrammars.add(grammarInfo);
                log.info("Grammar file '" + grammarFile
                    + "' included as newer than main generated file '" + targetFile + "'");
                break;
              }
              if (targetTS + timestampDeltaMs < jarsLastTS) {
                // jar file more recent than target file
                includedGrammars.add(grammarInfo);
                log.info("Grammar file'" + grammarFile + "' included as main generated file '"
                    + targetFile + "' older than dependent jar(s)");
                break;
              }
              log.info("Grammar file '" + grammarFile + "' not included");
            }
          }
        }
      } else {
        // no stale detection requested
        includedGrammars.add(grammarInfo);
        log.info("Grammar file '" + grammarFile + "' included as no stale detection requested");
      }
    }
    if (nbError > 0) {
      if ("last".equals(failOnGrammarError)) {
        throw new GrammarException(
            "Grammar reading error(s) encountered (see above), scan finished and leaving execution");
      } else {
        // failOnGrammarError == 'ignore'
        log.info("Encountered " + nbError
            + " grammar reading errors, but ignored and continuing execution");
      }
    }
    
    return includedGrammars;
  }
  
  /**
   * Determines the main generated file corresponding to the specified grammar file.
   *
   * @param targetDirectory - the absolute path to the output directory for the target files, must
   *          not be <code>null</code>
   * @param grammarInfo - the grammar info describing the grammar file, must not be <code>null
   *     </code>
   * @return a file array with main generated file, may be <code>null</code>, may be empty
   */
  protected File[] getTargetFile(final File targetDirectory, final GrammarInfo grammarInfo) {
    if (!targetDirectory.exists()) {
      log.debug("targetDirectory '" + targetDirectory + "' does not exist, no target file");
      return null;
    }
    tgt_ds.setBasedir(targetDirectory);
    tgt_ds.setIncludes(new String[] {
        grammarInfo.getMainGeneratedFile()
    });
    tgt_ds.setExcludes(null);
    // here we do not catch the IllegalStateException as targetDirectory
    // has already (normally) be checked as not null, existing and being a directory
    tgt_ds.scan();
    final String[] includedFiles = tgt_ds.getIncludedFiles();
    // well, we should get 0 or 1 result
    final File[] targetFiles = new File[includedFiles.length];
    int k = 0;
    for (final String includedFile : includedFiles) {
      targetFiles[k] = new File(targetDirectory, includedFile);
      k++;
    }
    log.debug("targetFiles = '" + Arrays.toString(targetFiles) + "'");
    return targetFiles;
  }
}
