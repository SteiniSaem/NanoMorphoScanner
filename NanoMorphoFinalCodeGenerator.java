public class NanoMorphoFinalCodeGenerator {

	final static boolean generateAndOr = true;
	final static boolean generateExp = true;

	static Parser parser;

	public static void main(String[] args) throws Exception {
		boolean debug = false;
		if (args.length > 0 && args[0].equals("--debug")) {
			System.out.println("Entering Final Code Generator main function");
			System.out.print("Turning debugging on...");
			debug = true;
			if (args.length > 1) args[0] = args[1];
			System.out.println(" Done");
		}
		else if (args.length > 1) {
			throw new Error("Invalid command line arguments");
		}
		String filename = null;
		if (args.length > (debug? 1:0)) {
			if (debug) System.out.println("Moving filename to first position");
			filename = args[0];
		}
		if (debug) System.out.println("Initialising parser");
		parser = new Parser(filename);
		if (debug ) {
			System.out.println("Turning debugging on in Parser");
			parser.yydebug = true;
		}
		if (debug) System.out.println("Starting parsing");
		parser.yyparse();
	}

	public static void generateProgram(String filename, Object[] funs) {
		String programname = filename.substring(0, filename.indexOf('.'));
		System.out.println("\"" + programname + ".mexe\" = main in\n!\n{{");
		for (Object f : funs) {
			generateFunction((Object[]) f);
		}
		System.out.println("}}");
		if (generateAndOr) {
			System.out.println("*\n{{\n#\"+[f1]\" = \n[\n(Return)\n];\n}}\n*\n{{");
			System.out.println("#\"|[f2]\" = \n[\n(CallR #\"||[f2]\" 2)\n];");
			System.out.println("}}\n*\n{{\n#\"&[f2]\" = \n[\n(CallR #\"&&[f2]\" 2)");
			System.out.println("];\n}}\n*\n{{\n#\"&&[f2]\" = \n[\n(Fetch 0)\n(GoFalse _false)");
			System.out.println("(Fetch 1)\n(GoFalse _false)\n(GoTrue _true)\n_false:");
			System.out.println("(MakeValR false)\n_true:\n(MakeValR true)\n];\n}}");
			System.out.println("*\n{{\n#\"||[f2]\" = \n[\n(Fetch 0)\n(GoTrue _true)");
			System.out.println("(Fetch 1)\n(GoTrue _true)\n(GoFalse _false)\n_false:");
			System.out.println("(MakeValR false)\n_true:\n(MakeValR true)\n];");
			System.out.println("}}");
		}
		if (generateExp) {
			System.out.println("*\n{{\n#\"^[f2]\" =\n[\n;;; While loop\n;;; var c;");
			System.out.println("(MakeVal 1)\n(Push)\n(Go _loopCheck)\n_loopStart:");
			System.out.println(";;; if( b%2 == 0 )\n(Fetch 1)\n(MakeValP 2)\n(Call #\"%[f2]\" 2)");
			System.out.println("(MakeValP 0)\n(Call #\"==[f2]\" 2)\n(GoTrue _true)");
			System.out.println("(GoFalse _false)\n_loopContinue:\n(Go _loopCheck)");
			System.out.println("_true:\n;;; a = a*a\n(Fetch 0)\n(FetchP 0)\n(Call #\"*[f2]\" 2)");
			System.out.println("(Store 0)\n;;; b = b/2\n(Fetch 1)\n(MakeValP 2)");
			System.out.println("(Call #\"/[f2]\" 2)\n(Store 1)\n(Go _loopContinue)");
			System.out.println("_false:\n;;; c = c*a\n(Fetch 2)\n(FetchP 0)\n(Call #\"*[f2]\" 2)");
			System.out.println("(Store 2)\n;;; b = b-1\n(Fetch 1)\n(MakeValP 1)");
			System.out.println("(Call #\"-[f2]\" 2)\n(Store 1)\n(Go _loopContinue)");
			System.out.println(";;; b > 1\n_loopCheck:\n(Fetch 1)\n(MakeValP 1)");
			System.out.println("(Call #\">[f2]\" 2)\n(GoTrue _loopStart)\n;;; return a*c");
			System.out.println("(Fetch 0)\n(FetchP 2)\n(CallR #\"*[f2]\" 2)\n];");
			System.out.println("}}");
		}
		System.out.println("*\nBASIS;");
	}

	static void generateFunction(Object[] fun) {
		// [name,argcount,varcount,exprs]
		String name = (String) fun[0];
		Integer argCount = (Integer) fun[1];
		Integer varCount = (Integer) fun[2];
		Object[] exprs = (Object[]) fun[3];
		System.out.println("#\"" + name + "[f" + argCount + "]\"" + " =\n[");
		for (int i = 0; i < varCount; i++) {
			System.out.println("(MakeVal null)\n(Push)");
		}
		for (Object expr : exprs) {
			generateExpr((Object[]) expr);
		}
		System.out.println("(Return)\n];");
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
			System.out.printf("(Store %d)\n", position);
		}
		else if (command.equals("NOT")) {
			Object[] expression = (Object[]) e[1];
			generateExpr(expression);
			System.out.println("(Not)");
		}
		else if (command.equals("CALL")) {
			String function = (String) e[1];
			Object[] arguments = (Object[]) e[2];
			int argCount = arguments.length;
			if (argCount != 0) {
				if (arguments[0] instanceof String) {
					generateExpr(arguments);
					argCount = arguments.length - 1;
				}
				else {
					generateExpr((Object[]) arguments[0]);
					for (int i = 1; i < arguments.length; i++) {
						System.out.println("(Push)");
						generateExpr((Object[]) arguments[i]);
					}
				}
			}
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





