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
	static boolean DEBUG = false;

	// Debug if debug flag is set
	static void debug(String s) {
		if (DEBUG) System.out.println(s);
	}

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

    static int getToken1() {
        return NanoMorphoLexer.getToken1();
    }

    // The symbol table consists of the following two variables.
    private static int varCount;
    private static HashMap<String, Integer> varTable;

    // Adds a new variable to the symbol table.
    // Throws Error if the variable already exists.
    private static void addVar(String name) {
        if (varTable.get(name) != null)
            throw new Error("Variable " + name + " already exists, near line " + NanoMorphoLexer.getLine());
        varTable.put(name, varCount++);
    }

    // Finds the location of an existing variable.
    // Throws Error if the variable does not exist.
    private static int findVar(String name) {
        Integer res = varTable.get(name);
        if (res == null)
            throw new Error("Variable " + name + " does not exist, near line " + NanoMorphoLexer.getLine());
        return res;
    }

    static public void main(String[] args) throws Exception {
        Object[] code = null;
		for (String arg : args) debug("Argument: " + arg);
		if (args[0].equals("--debug")) {
			DEBUG = true;
			args[0] = args[1];
		}
		else { DEBUG = false; }
		debug("Starting lexer...");
		NanoMorphoLexer.startLexer(args[0]);
		debug("Starting program parsing");
		varTable = new HashMap<String, Integer>();
		code = program();
        generateProgram(args[0], code);
    }

    // return array of name of function, num of arguments, num of local variables,
    // body
    static Object[] program() throws Exception {
        Object[] ret = new Object[] {};
        while (getToken1() != 0) {
            ret = Arrays.copyOf(ret, ret.length + 1);
            ret[ret.length - 1] = function();
        }
        return ret;
    }

    // count arguments and local variables
    static Object[] function() throws Exception {
        int argCount = 0;
        varCount = 0;
        String funName = over(NAME);
        over('(');
        if (getToken1() != ')') {
            for (;;) {
                addVar(over(NAME));
                argCount += 1;
                if (getToken1() != ',')
                    break;
                over(',');
            }
        }
        over(')');
        over('{');
        while (getToken1() == VAR) {
            varCount = decl();
            over(';');
        }
        Object[] exprs = new Object[] {};
        while (getToken1() != '}') {
            exprs = Arrays.copyOf(exprs, exprs.length + 1);
            exprs[exprs.length - 1] = expr();
            over(';');
        }
        over('}');
        Object[] ret = new Object[] {funName, argCount, varCount, exprs};
        return ret;
    }

    static int decl() throws Exception {
        int varcount = 1;
        over(VAR);
        for (;;) {
            addVar(over(NAME));
            if (getToken1() != ',')
                break;
            over(',');
            varcount++;
        }
        return varcount;
    }

    static Object[] expr() throws Exception {
        if (getToken1() == RETURN) {
            over(RETURN);
            return new Object[] {"RETURN", expr()};
        } else if (getToken1() == NAME && NanoMorphoLexer.getToken2() == '=') {
            String name = over(NAME);
            over('=');
            return new Object[] {"STORE", findVar(name), expr()};
        } else {
            return binopexpr(1);
        }
    }

    static Object[] binopexpr(int pri) throws Exception {
        if (pri > 7) {
            return smallexpr();
        } else if (pri == 2) {
            Object[] e = binopexpr(3);
            if (getToken1() == OPNAME && priority(NanoMorphoLexer.getLexeme()) == 2) {
                String op = advance();
                e = new Object[] { "CALL", op, new Object[] { e, binopexpr(2) } };
            }
            return e;
        } else {
            Object[] e = binopexpr(pri + 1);
            while (getToken1() == OPNAME && priority(NanoMorphoLexer.getLexeme()) == pri) {
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
        switch (getToken1()) {
            case NAME:
                String name = over(NAME);
                if (getToken1() == '(') {
					e = new Object[] {"CALL", name, null};
					Object[] args = new Object[] {};
                    over('(');
                    if (getToken1() != ')') {
                        for (;;) {
                            Object[] t = expr();
							args = Arrays.copyOf(args, args.length+1);
							args[args.length-1] = t;
                            if (getToken1() == ')')
                                break;
                            over(',');
                        }
                    }
                    over(')');
                }
                else {
                    e = new Object[] {"FETCH", findVar(name)};
                }
                return e;
            case WHILE:
                over(WHILE);
                e = new Object[] {"WHILE",expr(),body()};
                return e;
            case IF:
                over(IF);
                Object[] ref = new Object[] {"IF", expr(), body(), null};
				Object[] top = ref;
                while (getToken1() == ELSIF) {
                    over(ELSIF);
                    Object[] ref2 = new Object[] {"IF", expr(), body(), null};
                    ref[ref.length-1] = ref2;
                    ref = ref2;
                }
                if (getToken1() == ELSE) {
                    over(ELSE);
                    Object[] ref2 = new Object[] {"IF", true, body(), null};
                }
                return top;
            case LITERAL:
				e = new Object[] {"LITERAL",over(LITERAL)};
				return e;
			case OPNAME:
				return new Object[] {"CALL", over(OPNAME), smallexpr()};
			case '(':
				over('(');
				e = expr();
				over(')');
				return e;
			default:
				NanoMorphoLexer.expected("expression");
		}
		return null;
	}

	static Object[] body() throws Exception {
		Object[] exprs = new Object[] {};
		over('{');
		while (getToken1() != '}') {
			exprs = Arrays.copyOf(exprs, exprs.length+1);
			exprs[exprs.length-1] = expr();
			over(';');
		}
		over('}');
		return new Object[] {"BODY", exprs};
	}

	static void generateProgram(String filename, Object[] funs) {
		String programname = filename.substring(0, filename.indexOf('.'));
		debug("Starting printing of program...");
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
			System.out.println("(GoFalse _false)");
			System.out.println("(GoTrue _true)");
			System.out.println("_false:");
			System.out.println("(MakeValR false)");
			System.out.println("_true:");
			System.out.println("(MakeValR true)");
			System.out.println("];");
			System.out.println("}}");
		}
		System.out.println("*");
		System.out.println("BASIS;");
	}

	static void generateFunction(Object[] fun) {
		// [name,argcount,varcount,exprs]
		String name = (String) fun[0];
		Integer argCount = (Integer) fun[1];
		Integer varCount = (Integer) fun[2];
		debug(String.format("Generating function %s with %d arguments, and %d local variables.",
					name, argCount, varCount));
		Object[] exprs = (Object[]) fun[3];
		System.out.println("#\"" + name + "[f" + argCount + "]\"" + " =");
		System.out.println("[");
		for (int i = 0; i < varCount; i++) {
			System.out.println("(MakeVal null)");
			System.out.println("(Push)");
		}
		for (Object expr : exprs) {
			generateExpr((Object[]) expr);
		}
		System.out.println("(Return)");
		System.out.println("];");
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

    static void generateExpr(Object[] e) {
		String command = (String) e[0];
		if (command.equals("RETURN")) {
			Object[] expression = (Object[]) e[1];
			generateExpr(expression);
			System.out.println("(Return)");
		}
		else if (command.equals("STORE")) {
			Integer position = (Integer) e[1];
			Object[] expression = (Object[]) e[2];
			generateExpr(expression);
			System.out.println("(Store position)");
		}
		else if (command.equals("NOT")) {
			Object[] expression = (Object[]) e[1];
			generateExpr(expression);
			System.out.println("(Not)");
		}
		else if (command.equals("CALL")) {
			String function = (String) e[1];
			Object[] arguments = (Object[]) e[2];
			if (arguments.length != 0)
				generateExpr((Object[]) arguments[0]);
			for (int i = 1; i < arguments.length; i++) {
				System.out.println("(Push)");
				generateExpr((Object[]) arguments[i]);
			}
			int argCount = arguments.length;
			System.out.printf("(Call #\"%s[f%d]\" %2$d)\n", function, argCount);
		}
		else if (command.equals("FETCH")) {
			Integer position = (Integer) e[1];
			System.out.printf("(Fetch %d)\n", position);
		}
		else if (command.equals("LITERAL")) {
			String literal = (String) e[1];
			System.out.printf("(MakeVal %s)\n", literal);
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
		else if (command.equals("BODY")) {
			generateBody(e);
		}
    }

    static void generateBody(Object[] bodies) {
		Object[] expressions = (Object[]) bodies[1];
		for (Object expression : expressions) {
			generateExpr((Object[]) expression);
		}
    }
}
