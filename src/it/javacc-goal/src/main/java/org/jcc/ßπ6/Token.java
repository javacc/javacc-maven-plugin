/* CUSTOMIZED PARSER FILE - MUST NOT BE OVERWRITTEN BY JAVACC */
package org.jcc.ßπ6;

public class Token {

  public int kind;

  public int beginLine, beginColumn, endLine, endColumn;

  public String image;

  public Token next;

  public Token specialToken;

  public Token() {}

  public Token(final int kind) {
    this(kind, null);
  }

  public Token(final int kind, final String image) {
    this.kind = kind;
    this.image = image;
  }

  public Object getValue() {
    return null;
  }

  @Override
  public String toString() {
    return image;
  }

  public static Token newToken(final int ofKind, final String image) {
    switch (ofKind) {
      default:
        return new Token(ofKind, image);
    }
  }

  public static Token newToken(final int ofKind) {
    return newToken(ofKind, null);
  }
}
