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

/**
 * Enumeration for the different output languages and their characteristics.
 *
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 */
public enum Language {
  /** Java */
  JAVA(".java", LanguageHelper.AR_JJ_EXT, "Java", true, false, "java"),
  /** C++ */
  CPP(".cc", LanguageHelper.AR_JJ_H_EXT, "C++", false, true, "cpp"),
  /** C# */
  CSHARP(".cs", LanguageHelper.AR_JJ_EXT, "C#", false, true, "csharp"), //
  /** JavaScript */
  JAVASCRIPT(".js", LanguageHelper.AR_JJ_EXT, "JS", false, true, "js"), //
  /** Kotlin */
  KOTLIN(".kt", LanguageHelper.AR_JJ_EXT, "Kotlin", true, false, "kotlin"), //
  /** Python */
  PYTHON(".py", LanguageHelper.AR_JJ_EXT, "Python", false, true, "python"), //
  ;
  
  /** Generated files main extension. Well known languages file extensions. */
  protected final String extension;
  
  /** Generated files other extensions. Well known languages file extensions. */
  protected final String[] otherExtensions;
  
  /** Generated files all extensions. Well known languages file extensions. */
  protected final String[] allExtensions;
  
  /** CodeGenerator option. Defined in CodeGenerator.getName() int the javacc-8 repositories. */
  protected final String cgOption;
  
  /** Flag telling if the corresponding generator manages package names or not. */
  protected final Boolean usesPackage;
  
  /** Flag telling if the corresponding generator manages the grammar paths or not. */
  protected final Boolean usesPath;
  
  /**
   * Subdirectory appearing in different paths (in the javacc-8 generator repositories) (notably in
   * the templates paths), corresponding to the generator.
   */
  protected final String subDir;
  
  /**
   * Constructor.
   *
   * @param ext - the parser file extension
   * @param oth - the generated files other extensions
   * @param cgo - the CodeGenerator option
   * @param pkg - the usesPackage flag
   * @param pth - the usesPath flag
   * @param dir - the subdirectory
   */
  Language(final String ext, final String oth[], final String cgo, final boolean pkg,
      final boolean pth, final String dir) {
    extension = ext;
    otherExtensions = oth;
    allExtensions = new String[otherExtensions.length + 1];
    allExtensions[0] = ext;
    for (int i = 0, j = 1; i < otherExtensions.length; i++, j++) {
      allExtensions[j] = otherExtensions[i];
    }
    cgOption = cgo;
    usesPackage = pkg;
    usesPath = pth;
    subDir = dir;
  }
  
  /**
   * @param value - a CodeGenerator option value
   * @return the corresponding language
   * @throws PluginException if invalid option value
   */
  public static final Language getLanguageFrom(final String value) throws PluginException {
    final String u = value.toUpperCase();
    if (u.equals(JAVA.cgOption.toUpperCase())) {
      return JAVA;
    } else if (u.equals(CPP.cgOption.toUpperCase())) {
      return CPP;
    } else if (u.equals(CSHARP.cgOption.toUpperCase())) {
      return CSHARP;
    } else {
      throw new PluginException("Invalid CodeGenerator option " + value);
    }
  }
  
  /**
   * Just to hold constants used in the enum.
   *
   * @since 3.8.0
   * @author Maͫzͣaͬsͨ
   */
  public static class LanguageHelper {
    
    /** JavaCC file extension in an array. */
    public static final String[] AR_JJ_EXT = {
        GrammarInfo.JJ_EXT
    };
    
    /** JavaCC and C/C++ header file extensions in an array. */
    public static final String[] AR_JJ_H_EXT = {
        GrammarInfo.JJ_EXT, ".h"
    };
  }
}
