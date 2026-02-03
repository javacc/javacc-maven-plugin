/*
 * Copyright (c) 2006, Sun Microsystems, Inc. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this list of conditions and the
 * following disclaimer. * Redistributions in binary form must reproduce the above copyright notice, this list
 * of conditions and the following disclaimer in the documentation and/or other materials provided with the
 * distribution. * Neither the name of the Sun Microsystems, Inc. nor the names of its contributors may be
 * used to endorse or promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
// added to the original JJTree example
// enhanced from ex2jtb because of the children option
package org.jtb_jcc.eg05.ast;

import org.jtb_jcc.eg05.vis.IGenVisitor;
import org.jtb_jcc.eg05.Token;

@SuppressWarnings("javadoc")
public class MyID implements INode {

  /** Added for customization example */
  private String name = "my name";

  public Token f0;

  private static final long serialVersionUID = 153L;

  public MyID(final Token n0) {
    f0 = n0;
  }

  /**
   * @param n - the name
   */
  public void setName(final String n) {
    name = n;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "Identifier: " + name;
  }

  /*
   * Visitors accept methods (no -novis option, visitors specification : Gen,void,java.lang.String)
   */

  @Override
  public void accept(final IGenVisitor vis, final String argu) {
    vis.visit(this, argu);
  }
}
