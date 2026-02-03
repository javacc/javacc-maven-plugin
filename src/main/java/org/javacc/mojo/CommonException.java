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

import org.apache.maven.plugin.MojoExecutionException;

/**
 * Super class of exceptions in this plugin.<br>
 * Subclasses are:
 *
 * <ul>
 * <li>{@link PluginException}: for plugin parameters and processor options needed by the plugin,
 * for files and directories related errors
 * <li>{@link GrammarException}: for errors when the plugin reads the grammars (to find inside the
 * parser name, the parser package if any...)
 * <li>{@link ProcessorException}: for errors thrown by the processor(s) when processing grammars
 * </ul>
 *
 * @since 3.8.0
 * @author Maͫzͣaͬsͨ
 */
@SuppressWarnings("serial")
class CommonException extends MojoExecutionException {
  
  /**
   * Construct a new <code>CommonException</code> exception wrapping an underlying <code>
   * Exception</code> and providing a <code>message</code>.
   *
   * @param message - the exception message
   * @param cause - the exception cause
   */
  CommonException(final String message, final Exception cause) {
    super(message, cause);
  }
  
  /**
   * Construct a new <code>CommonException</code> exception wrapping an underlying <code>
   * Throwable</code> and providing a <code>message</code>.
   *
   * @param message - the exception message
   * @param cause - the exception cause
   */
  CommonException(final String message, final Throwable cause) {
    super(message, cause);
  }
  
  /**
   * Construct a new <code>CommonException</code> exception providing a <code>message</code>.
   *
   * @param message - the exception message
   */
  CommonException(final String message) {
    super(message);
  }
}

/** Exceptions for plugin configuration. */
@SuppressWarnings({
    "serial", "javadoc"
})
class PluginException extends CommonException {
  
  PluginException(final String msg, final Exception cause) {
    super(msg, cause);
  }
  
  PluginException(final String msg, final Throwable cause) {
    super(msg, cause);
  }
  
  PluginException(final String msg) {
    super(msg);
  }
}

/** Exceptions on reading individual grammars. */
@SuppressWarnings({
    "serial", "javadoc"
})
class GrammarException extends CommonException {
  
  GrammarException(final String msg, final Exception cause) {
    super(msg, cause);
  }
  
  GrammarException(final String msg, final Throwable cause) {
    super(msg, cause);
  }
  
  GrammarException(final String msg) {
    super(msg);
  }
}

/** Exceptions on processing grammars. */
@SuppressWarnings({
    "serial", "javadoc"
})
class ProcessorException extends CommonException {
  
  ProcessorException(final String msg, final Exception cause) {
    super(msg, cause);
  }
  
  ProcessorException(final String msg, final Throwable cause) {
    super(msg, cause);
  }
  
  ProcessorException(final String msg) {
    super(msg);
  }
}
