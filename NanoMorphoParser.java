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
    static Object[] function() throws Exception {
        int argCount = 0;
        varCount = 0;
        varTable = new HashMap<String, Integer>();
        String funName = over(NAME);
        over('(');
        if (getToken() != ')') {
            for (;;) {
                addVar(over(NAME));
                argCount += 1;
                if (getToken() != ',')
                    break;
                over(',');
            }
        }
        over(')');
        over('{');
        while (getToken() == VAR) {
            varCount = decl();
            over(';');
        }
        Object[] exprs = {};
        while (getToken() != '}') {
            exprs = Arrays.copyOf(exprs, exprs.length + 1);
            exprs[exprs.length - 1] = expr();
            over(';');
        }
        over('}');
        Object[] ret = {funName, argCount, varCount, exprs}
        return ret;
    }

    static int decl() throws Exception {
        int varcount = 1;
        over(VAR);
        for (;;) {
            addVar(over(NAME))
            if (getToken() != ',')
                break;
            over(',');
            varcount++;
        }
        return varcount;
    }

    static Object[] expr() throws Exception {
        if (getToken() == RETURN) {
            over(RETURN);
            return {"RETURN", expr()};
        } else if (getToken() == NAME && NanoMorphoLexer.getToken2() == '=') {
            String name = over(NAME);
            over('=');
            return {"STORE", findVar(name), expr()};
        } else {
            return binopexpr(1);
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

    static Object[] smallexpr() throws Exception {
        Object[] e;
        switch (getToken()) {
            case NAME:
                String name = over(NAME);
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
                else {
                    e = {"FETCH", varTable.get(name)};
                }
                return e;
            case WHILE:
                over(WHILE);
                e={"WHILE",expr(),body()};
                return e;
            case IF:
                over(IF);
                Object[] e = {"IF", expr(), body(), null};
                Object[] ref = e;
                while (getToken() == ELSIF) {
                    over(ELSIF);
                    Object[] ref2 =  {"IF", expr(), body(), null};
                    ref[ref.length-1] = ref2;
                    ref = ref2;
                }
                if (getToken() == ELSE) {
                    over(ELSE);
                    ref2 = {"IF", true, body(), null};
                }
                return;
            case LITERAL:
                e={"LITERAL",over(LITERAL)};
                return e;
            case OPNAME:
                return {"CALL", over(OPNAME), smallexpr()};
            case '(':
                over('(');
                e = expr();
                over(')');
                return e;
            default:
                NanoMorphoLexer.expected("expression");
        }
    }

    static Object[] body() throws Exception {
        Object[] exprs = {};
        over('{');
        while (getToken() != '}') {
            exprs = Arrays.copyOf(exprs, exprs.length+1);
            exprs[length-1] = expr();
            over(';');
        }
        over('}');
        return {"BODY", exprs};
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
			else if (command.equals("STORE")) {
				Integer position = (Integer) e[1];
				Object[] expression = e[2];
				generateExpr(expression);
				System.out.println("(Store position)");
			}
			else if (command.equals("NOT")) {
				Object[] expression = e[1];
				generateExpr(expression);
				System.out.println("(Not)");
			}
			else if (command.equals("CALL")) {
				String function = (String) e[1];
				Object[] arguments = (Object[]) e[2];
				if (arguments.length != 0)
					generateExpr(arguments[0]);
				for (int i = 1; i < arguments.length; i++) {
					System.out.println("(Push)");
					generateExpr(argument);
				}
				int argCount = arguments.length;
				System.out.printf("(Call #\"%s[f%d]\" %2$d)\n", function, argCount);
			}
			else if (command.equals("FETCH")) {
				Integer position = (Integer) e[1];
				System.out.printf("(Fetch %d)\n", position);
			}
			else if (command.equals("LITERAL")) {
				System.out.printf("(MakeVal)\n", position);
			}
			else if (command.equals("IF")) {
				Object[] condition = (Object[]) e[1];
				Object[] body = (Object[]) e[2];
				Object[] elseblock = (Object[]) e[3];
				generateExpr(condition);
				String label = newLabel();
				System.out.printf("(GoFalse %s)\n", label);
				generateBody(body);
				System.out.printf("%s:\n", label);
				if (elseblock != null)
					generateExpr(elseblock);
			}
			else if (command.equals("WHILE")) {
				Object[] condition = (Object[]) e[1];
				Object[] body = (Object[]) e[2];
				String loopCheck = newLabel();
				String loopStart = newLabel();
				System.out.printf("(Go %s)\n", loopCheck);
				System.out.printf("%s:\n", loopStart);
				generateBody(body);
				System.out.printf("%s:\n", loopCheck);
				generateExpr(condition);
				System.out.printf("(GoTrue %s)\n", loopStart);
			}
			else if (command.equals("BODY")) {}
		}
    }

    static void generateBody(Object[] bodies) {
		// TODO
    }
}
