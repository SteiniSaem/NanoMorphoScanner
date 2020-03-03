// Höfundur: Snorri Agnarsson, 2017-2020

import java.util.Vector;
import java.util.HashMap;

public class NanoMorphoParser {
    final static int ERROR = -1;
    final static int IF = 1001;
    final static int ELSE = 1002;
    final static int ELSIF = 1003;
    final static int WHILE = 1004;
    final static int VAR = 1005;
    final static int RETURN = 1006;
    final static int NAME = 1007;
    final static int OPNAME = 1008;
    final static int LITERAL = 1009;

    static String advance() throws Exception {
        return NanoMorphoLexer.advance();
    }

    static String over(int tok) throws Exception {
        return NanoMorphoLexer.over(tok);
    }

    static String over(char tok) throws Exception {
        return NanoMorphoLexer.over(tok);
    }

    static int getToken1() {
        return NanoMorphoLexer.getToken1();
    }

    static public void main(String[] args) throws Exception {
        try {
            NanoMorphoLexer.startLexer(args[0]);
            program();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
    }

    static void program() throws Exception {
        while (getToken1() != 0)
            function();
    }

    static void function() throws Exception {
        over(NAME);
        over('(');
        if (getToken1() != ')') {
            for (;;) {
                over(NAME);
                if (getToken1() != ',')
                    break;
                over(',');
            }
        }
        over(')');
        over('{');
        while (getToken1() == VAR) {
            decl();
            over(';');
        }
        while (getToken1() != '}') {
            expr();
            over(';');
        }
        over('}');
    }

    static void decl() throws Exception {
        int varCount = 0;
        over(VAR);
        for (;;) {
            over(NAME);
            if (getToken1() != ',')
                break;
            over(',');
        }
        varCount += 1;
    }

    static void expr() throws Exception {
        if (getToken1() == RETURN) {
            over(RETURN);
            expr();
        } else if (getToken1() == NAME && NanoMorphoLexer.getToken2() == '=') {
            over(NAME);
            over('=');
            expr();
        } else {
            binopexpr();
        }
    }

    static void binopexpr() throws Exception {
        smallexpr();
        while (getToken1() == OPNAME) {
            over(OPNAME);
            smallexpr();
        }
    }

    static void smallexpr() throws Exception {
        switch (getToken1()) {
            case NAME:
                over(NAME);
                if (getToken1() == '(') {
                    over('(');
                    if (getToken1() != ')') {
                        for (;;) {
                            expr();
                            if (getToken1() == ')')
                                break;
                            over(',');
                        }
                    }
                    over(')');
                }
                return;
            case WHILE:
                over(WHILE);
                expr();
                body();
                return;
            case IF:
                over(IF);
                expr();
                body();
                while (getToken1() == ELSIF) {
                    over(ELSIF);
                    expr();
                    body();
                }
                if (getToken1() == ELSE) {
                    over(ELSE);
                    body();
                }
                return;
            case LITERAL:
                over(LITERAL);
                return;
            case OPNAME:
                over(OPNAME);
                smallexpr();
                return;
            case '(':
                over('(');
                expr();
                over(')');
                return;
            default:
                NanoMorphoLexer.expected("expression");
        }
    }

    static void body() throws Exception {
        over('{');
        while (getToken1() != '}') {
            expr();
            over(';');
        }
        over('}');
    }

    static void generateProgram(String filename, Object[] funs) {
        String programname = filename.substring(0, filename.indexOf('.'));
        System.out.println("\"" + programname + ".mexe\" = main in");
        System.out.println("!");
        System.out.println("{{");
        for (Object f : funs) {
            generateFunction((Object[]) f);
        }
        System.out.println("}}");
        System.out.println("*");
        System.out.println("BASIS;");
    }

    static void generateFunction( Object[] fun )
    {
		...
    }

    // All existing labels, i.e. labels the generated
    // code that we have already produced, should be
    // of form
    // _xxxx
    // where xxxx corresponds to an integer n
    // such that 0 <= n < nextLab.
    // So we should update nextLab as we generate
    // new labels.
    // The first generated label would be _0, the
    // next would be _1, and so on.
    private static int nextLab = 0;

    // Returns a new, previously unused, label.
    // Useful for control-flow expressions.
    static String newLabel() {
        return "_" + (nextLabel++);
    }

    static void generateExpr( Object[] e )
    {
		...
    }

    static void generateBody( Object[] bod )
    {
		...
    }
}