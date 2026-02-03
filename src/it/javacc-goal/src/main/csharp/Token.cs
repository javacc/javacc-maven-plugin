/* CUSTOMIZED PARSER FILE - MUST NOT BE OVERWRITTEN BY JAVACC */
namespace BP7 {

public partial class Token {

  public int kind;

  public int beginLine;

  public int beginColumn;

  public int endLine;

  public int endColumn;

  public string image;

  public Token next;

  public Token specialToken;

  public object getValue() {
    return null;
  }

  public Token() {
  }

  public Token(int kind) : this(kind, null) {
  }

  public Token(int kind, string image) {
    this.kind = kind;
    this.image = image;
  }

  override public string ToString() {
    if (kind == 0) { // 0 is always EOF
      return "EOF";
    }
    return image;
  }

  public static Token newToken(int ofKind, string image) {
    switch(ofKind) {
      default :
        return new Token(ofKind, image);
    }
  }

  public static Token newToken(int ofKind) {
    return newToken(ofKind, null);
  }

} // end class

} // end namespace
