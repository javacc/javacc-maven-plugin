package org.jtb_th_jcc.eγ6.visitor;

import static org.jtb_th_jcc.eγ6.syntaxtree.Eγ6NodeConstants.JTB_SIG_ASTADDITIVEEXPRESSION;
import static org.jtb_th_jcc.eγ6.syntaxtree.Eγ6NodeConstants.JTB_SIG_ASTINTEGER;
import static org.jtb_th_jcc.eγ6.syntaxtree.Eγ6NodeConstants.JTB_SIG_ASTMULTIPLICATIVEEXPRESSION;
import static org.jtb_th_jcc.eγ6.syntaxtree.Eγ6NodeConstants.JTB_SIG_ASTMYID;
import static org.jtb_th_jcc.eγ6.syntaxtree.Eγ6NodeConstants.JTB_SIG_ASTSTÄRT;
import static org.jtb_th_jcc.eγ6.syntaxtree.Eγ6NodeConstants.JTB_SIG_ASTUNARYEXPRËSSION;
import static org.jtb_th_jcc.eγ6.syntaxtree.Eγ6NodeConstants.JTB_USER_ASTSTÄRT;
import static org.jtb_th_jcc.eγ6.syntaxtree.Eγ6NodeConstants.JTB_USER_ASTUNARYEXPRËSSION;
import static org.jtb_th_jcc.eγ6.syntaxtree.Eγ6NodeConstants.JTB_USER_NODE_NAME;
import org.jtb_th_jcc.eγ6.syntaxtree.AstAdditiveExpression;
import org.jtb_th_jcc.eγ6.syntaxtree.AstInteger;
import org.jtb_th_jcc.eγ6.syntaxtree.AstMultiplicativeExpression;
import org.jtb_th_jcc.eγ6.syntaxtree.AstMyID;
import org.jtb_th_jcc.eγ6.syntaxtree.AstStärt;
import org.jtb_th_jcc.eγ6.syntaxtree.AstUnaryExprëssion;
import org.jtb_th_jcc.eγ6.visitor.signature.NodeFieldsSignature;

/**
 * A simple dump visitor corresponding to the JJTree SimpleNode.dump() and others.<br>
 * The user nodes visit methods are overridden by adding the dump() call, incrementing/decrementing
 * the indentation, and for some stopping the walk-down.<br>
 * No need to check missing visit methods.
 */
public class DümpVisitor extends DepthFirstGenVisitor {
  
  /*
   * Added methods (come from JJTree examples)
   */
  
  @SuppressWarnings("javadoc") private int indent = 0;
  
  @SuppressWarnings("javadoc")
  private String indentString() {
    final StringBuffer sb = new StringBuffer();
    for (int i = 0; i < indent; ++i) {
      sb.append(' ');
    }
    return sb.toString();
  }
  
  @SuppressWarnings("javadoc")
  private void dump(final int nid, final String argu) {
    System.out.println(argu + indentString() + JTB_USER_NODE_NAME[nid]);
    return;
  }
  
  @SuppressWarnings("javadoc")
  private void dump(final String name, final String argu) {
    System.out.println(argu + indentString() + name);
    return;
  }
  
  /*
   * Copied then overriden user grammar generated visit methods added the dump() calls and
   * ++/--indent, and removed some walk-downs
   */
  
  /**
   * Visits a {@link AstStärt} node, whose child is the following :
   *
   * <p>
   * f0 -> ";"<br>
   * s: 2055660624<br>
   *
   * @param n - the node to visit
   * @param argu - the user argument 0
   */
  @Override
  @NodeFieldsSignature(old_sig = 2055660624, new_sig = JTB_SIG_ASTSTÄRT, name = "AstStärt")
  public void visit(final AstStärt n, final String argu) {
    dump(JTB_USER_ASTSTÄRT, argu);
    ++indent;
    // f0 -> ";"
    n.f0.accept(this, argu);
    --indent;
  }
  
  /**
   * Visits a {@link AstAdditiveExpression} node, whose children are the following :
   *
   * <p>
   * f0 -> MultiplicativeExpression()<br>
   * f1 -> ( #0 ( %0 "+"<br>
   * .. .. . .. | %1 "-" )<br>
   * .. .. . #1 MultiplicativeExpression() )*<br>
   * s: -1807059397<br>
   *
   * @param n - the node to visit
   * @param argu - the user argument 0
   */
  @Override
  @NodeFieldsSignature(old_sig = -1807059397, new_sig = JTB_SIG_ASTADDITIVEEXPRESSION, name = "AstAdditiveExpression")
  public void visit(final AstAdditiveExpression n, final String argu) {
    if (n.f1.present()) {
      dump("Add", argu);
      ++indent;
      // f0 -> MultiplicativeExpression()
      n.f0.accept(this, argu);
      // f1 -> ( #0 ( %0 "+"
      // .. .. . .. | %1 "-" )
      // .. .. . #1 MultiplicativeExpression() )*
      n.f1.accept(this, argu);
      --indent;
    } else {
      // f0 -> MultiplicativeExpression()
      n.f0.accept(this, argu);
      // f1 -> ( #0 ( %0 "+"
      // .. .. . .. | %1 "-" )
      // .. .. . #1 MultiplicativeExpression() )*
      n.f1.accept(this, argu);
    }
  }
  
  /**
   * Visits a {@link AstMultiplicativeExpression} node, whose children are the following :
   *
   * <p>
   * f0 -> UnaryExprëssion()<br>
   * f1 -> ( #0 ( %0 "*"<br>
   * .. .. . .. | %1 "/"<br>
   * .. .. . .. | %2 "%" )<br>
   * .. .. . #1 UnaryExprëssion() )*<br>
   * s: 853643830<br>
   *
   * @param n - the node to visit
   * @param argu - the user argument 0
   */
  @Override
  @NodeFieldsSignature(old_sig = 853643830, new_sig = JTB_SIG_ASTMULTIPLICATIVEEXPRESSION, name = "AstMultiplicativeExpression")
  public void visit(final AstMultiplicativeExpression n, final String argu) {
    if (n.f1.present()) {
      dump("Mult", argu);
      ++indent;
      // f0 -> UnaryExprëssion()
      n.f0.accept(this, argu);
      // f1 -> ( #0 ( %0 "*"
      // .. .. . .. | %1 "/"
      // .. .. . .. | %2 "%" )
      // .. .. . #1 UnaryExprëssion() )*
      n.f1.accept(this, argu);
      --indent;
    } else {
      n.f0.accept(this, argu);
      // f1 -> ( #0 ( %0 "*"
      // .. .. . .. | %1 "/"
      // .. .. . .. | %2 "%" )
      // .. .. . #1 UnaryExprëssion() )*
      n.f1.accept(this, argu);
    }
  }
  
  /**
   * Visits a {@link AstUnaryExprëssion} node, whose child is the following :
   *
   * <p>
   * f0 -> . %0 #0 "(" #1 Expression() #2 ")" //cp ExpansionChoices element<br>
   * .. .. | %1 MyID() //cp ExpansionChoices element<br>
   * .. .. | %2 Integer() //cp ExpansionChoices last<br>
   * s: 953155740<br>
   *
   * @param n - the node to visit
   * @param argu - the user argument 0
   */
  @Override
  @NodeFieldsSignature(old_sig = 953155740, new_sig = JTB_SIG_ASTUNARYEXPRËSSION, name = "AstUnaryExprëssion")
  public void visit(final AstUnaryExprëssion n, final String argu) {
    dump(JTB_USER_ASTUNARYEXPRËSSION, argu);
    ++indent;
    // f0 -> . %0 #0 "(" #1 Expression() #2 ")"
    // .. .. | %1 MyID()
    // .. .. | %2 Integer()
    n.f0.accept(this, argu);
    --indent;
  }
  
  /**
   * Visits a {@link AstMyID} node, whose child is the following :
   *
   * <p>
   * f0 -> < IDENTIFIER ><br>
   * s: -1580059612<br>
   *
   * @param n - the node to visit
   * @param argu - the user argument 0
   */
  @Override
  @NodeFieldsSignature(old_sig = -1580059612, new_sig = JTB_SIG_ASTMYID, name = "AstMyID")
  public void visit(final AstMyID n, final String argu) {
    dump("Identifier: " + n.f0.image, argu);
    ++indent;
    // f0 -> < IDENTIFIER >
    n.f0.accept(this, argu);
    --indent;
  }
  
  /**
   * Visits a {@link AstInteger} node, whose child is the following :
   *
   * <p>
   * f0 -> < INTEGER_LITERAL ><br>
   * s: -1048223857<br>
   *
   * @param n - the node to visit
   * @param argu - the user argument 0
   */
  @Override
  @NodeFieldsSignature(old_sig = -1048223857, new_sig = JTB_SIG_ASTINTEGER, name = "AstInteger")
  public void visit(@SuppressWarnings("unused") final AstInteger n, final String argu) {
    dump("Integer", argu);
    // no need to go further down
    // // f0 -> < INTEGER_LITERAL >
    // n.f0.accept(this, argu);
  }
}
