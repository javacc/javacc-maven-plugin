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
package org.javacc.mojo;

/** Enumeration of parser files extensions for the different output languages. */
public enum Suffix {
  /** Java language */
  Java("java"),
  /** C++ language */
  Cpp("cc"),
  /** C# language */
  CSharp("cs");

  /** The parser file extension. */
  private final String suffix;

  /**
   * Constructor.
   *
   * @param sfx the parser file extension.
   */
  Suffix(final String sfx) {
    suffix = sfx;
  }

  /**
   * @return the parser file extension
   */
  public String string() {
    return suffix;
  }
}
