// Höfundur: Snorri Agnarsson, 2017-2020

import java.util.Vector;
import java.util.HashMap;
import java.util.Arrays;

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

	final static boolean generateAndOr = true;

    // Forward one lexeme.
    // Returns the lexeme advanced over.
    static String advance() throws Exception {
        return NanoMorphoLexer.advance();
    }

    // Forward one lexeme which must have the given token.
    // Returns the lexeme advanced over.
    static String over(int tok) throws Exception {
        return NanoMorphoLexer.over(tok);
    }

    // Forward one lexeme which must have the given token,
    // Returns the lexeme advanced over.
    static String over(char tok) throws Exception {
        return NanoMorphoLexer.over(tok);
    }

    static int getToken() {
        return NanoMorphoLexer.getToken();
    }

    // The symbol table consists of the following two variables.
    private static int varCount;
    private static HashMap<String, Integer> varTable;

    // Adds a new variable to the symbol table.
    // Throws Error if the variable already exists.
    private static void addVar(String name) {
        if (varTable.get(name) != null)
            throw new Error("Variable " + name + " already exists, near line " + NanoMorphoLexer.getNextLine());
        varTable.put(name, varCount++);
    }

    // Finds the location of an existing variable.
    // Throws Error if the variable does not exist.
    private static int findVar(String name) {
        Integer res = varTable.get(name);
        if (res == null)
            throw new Error("Variable " + name + " does not exist, near line " + NanoMorphoLexer.getNextLine());
        return res;
    }

    static public void main(String[] args) throws Exception {
        Object[] code = null;
        try {
            NanoMorphoLexer.startLexer(args[0]);
            code = program();
        } catch (Throwable e) {
            System.out.println(e.getMessage());
        }
        generateProgram(args[0], code);
    }

    // return array of name of function, num of arguments, num of local variables,
    // body
    static Object[] program() throws Exception {
        Object[] ret = {};
        while (getToken() != 0) {
            ret = Arrays.copyOf(ret, ret.length + 1);
            ret[ret.length - 1] = function();
        }
        return ret;
    }

    // count arguments and local variables
    static void function() throws Exception {
        varCount = 0;
        varTable = new HashMap<String, Integer>();
        over(NAME);
        over('(');
        if (getToken() != ')') {
            for (;;) {
                over(NAME);
                if (getToken() != ',')
                    break;
                over(',');
            }
        }
        over(')');
        over('{');
        while (getToken() == VAR) {
            decl();
            over(';');
        }
        while (getToken() != '}') {
            expr();
            over(';');
        }
        over('}');
    }

    static int decl() throws Exception {
        int varcount = 1;
        over(VAR);
        for (;;) {
            over(NAME);
            if (getToken() != ',')
                break;
            over(',');
        }
        return varcount;
    }

    static void expr() throws Exception {
        if (getToken() == RETURN) {
            over(RETURN);
            expr();
        } else if (getToken() == NAME && NanoMorphoLexer.getToken2() == '=') {
            over(NAME);
            over('=');
            expr();
        } else {
            binopexpr();
        }
    }

    static Object[] binopexpr(int pri) throws Exception {
        if (pri > 7) {
            return smallexpr();
        } else if (pri == 2) {
            Object[] e = binopexpr(3);
            if (getToken() == OPNAME && priority(NanoMorphoLexer.getLexeme()) == 2) {
                String op = advance();
                e = new Object[] { "CALL", op, new Object[] { e, binopexpr(2) } };
            }
            return e;
        } else {
            Object[] e = binopexpr(pri + 1);
            while (getToken() == OPNAME && priority(NanoMorphoLexer.getLexeme()) == pri) {
                String op = advance();
                e = new Object[] { "CALL", op, new Object[] { e, binopexpr(pri + 1) } };
            }
            return e;
        }
    }

    static int priority(String opname) {
        switch (opname.charAt(0)) {
            case '^':
            case '?':
            case '~':
                return 1;
            case ':':
                return 2;
            case '|':
                return 3;
            case '&':
                return 4;
            case '!':
            case '=':
            case '<':
            case '>':
                return 5;
            case '+':
            case '-':
                return 6;
            case '*':
            case '/':
            case '%':
                return 7;
            default:
                throw new Error("Invalid opname");
        }
    }

    static void smallexpr() throws Exception {
        switch (getToken()) {
            case NAME:
                over(NAME);
                if (getToken() == '(') {
                    over('(');
                    if (getToken() != ')') {
                        for (;;) {
                            expr();
                            if (getToken() == ')')
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
                while (getToken() == ELSIF) {
                    over(ELSIF);
                    expr();
                    body();
                }
                if (getToken() == ELSE) {
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
        while (getToken() != '}') {
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
		if (generateAndOr) {
			System.out.println("*");
			System.out.println("{{");
			System.out.println("#\"&&[f2]\" = ");
			System.out.println("[");
			System.out.println("(Fetch 0)");
			System.out.println("(Push)");
			System.out.println("(Fetch 1)");
			System.out.println("(Call #\"&[f2] 2\")");
			System.out.println("(GoFalse _false");
			System.out.println("(GoTrue _true");
			System.out.println("_false:");
			System.out.println("(MakeValR false)");
			System.out.println("_true:");
			System.out.println("(MakeValR true)");
			System.out.println("]");
			System.out.println("}}");
		}
        System.out.println("*");
        System.out.println("BASIS;");
    }

    static void generateFunction(Object[] fun) {
		// [name,argcount,varcount,exprs]
		Vector[] ret = new Vector();
		for (Object[] f : fun) {
			String name = f[0];
			Integer argcount = f[1];
			Integer varcount = f[2];
			Object[] exprs = f[3];
			System.out.println("#\"" + name + "\"[f" + argCount + "]" + " =");
			for (int i = 0; i < varCount; i++) {
				System.out.println("(MakeVal null)");
				System.out.println("(Push)");
			}
			generateExpr(exprs);
		}
    }

    // All existing labels, i.e. labels the generated
    // code that we have already produced, should be
    // of form
    // _xxxx
    // where xxxx corresponds to an integer n
    // such that 0 <= n < nextLabel.
    // So we should update nextLabel as we generate
    // new labels.
    // The first generated label would be _0, the
    // next would be _1, and so on.
    private static int nextLabel = 0;

    // Returns a new, previously unused, label.
    // Useful for control-flow expressions.
    static String newLabel() {
        return "_" + (nextLabel++);
    }

    static void generateExpr(Object[] expressions) {
		// TODO
		for (Object[] e : expressions) {
			String command = (String) e[0];
			if (command.equals("RETURN")) {
				Object[] expression = e[1];
				generateExpr(expression);
				System.out.println("(Return)");
			}
			if (command.equals("STORE")) {
				Integer position = (Integer) e[1];
				Object[] expression = e[2];
				generateExpr(expression);
				System.out.println("(Store position)");
			}
			if (command.equals("NOT")) {
				Object[] expression = e[1];
				generateExpr(expression);
				System.out.println("(Not)");
			}
			if (command.equals("CALL")) {
				String function = (String) e[1];
				Object[] arguments = (Object[]) e[2];
				int argCount = arguments.length;
				System.out.printf("(Call #\"%s[f%d]\" %2$d)\n", function, argCount);
			}
			if (command.equals("FETCH")) {}
			if (command.equals("LITERAL")) {}
			if (command.equals("IF")) {}
			if (command.equals("WHILE")) {}
			if (command.equals("BODY")) {}
		}
    }

    static void generateBody(Object[] bodies) {
		// TODO
    }
}
