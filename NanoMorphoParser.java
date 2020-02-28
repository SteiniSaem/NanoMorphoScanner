import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class NanoMorphoParser {
	private static boolean DEBUG = false;
	private static NanoMorphoLexer nml;
	public static void main(String[] args) throws FileNotFoundException,Exception {
		if (args[0].equals("--debug")) {
			DEBUG = true;
			args[0] = args[1];
		}
		nml = new NanoMorphoLexer(new FileReader(args[0]));
		nml.init();
		program(0);
	}

	private static void syntaxError(String expected, String got) throws Exception{
		// System.out.println(
		throw new Exception(
				String.format(
					"Syntax error! Expected %s, but got %s.",
					expected,
					got
					)
				);
	}

	// program		= { function }
	//				;
	private static void program(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering program at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		function(depth+1);
		while (nml.getToken1() != NanoMorphoLexer.EOF)
			function(depth+1);
	}

	// function		= NAME, '(', [ NAME, { ',', NAME } ] ')'
	//					'{', { decl, ';' }, { expr, ';' }, '}'
	//				;
	private static void function(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering function at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		if (nml.getToken1() != NanoMorphoLexer.NAME) 
			syntaxError("function name", nml.getLexeme1());
		nml.advance();
		if (nml.getToken1() != '(') // (
			syntaxError("(", nml.getLexeme1());
		nml.advance();

		if (nml.getToken1() != ')') { // )
			if (nml.getToken1() == NanoMorphoLexer.NAME) {
				nml.advance();
				while (nml.getToken1() == ',') { // ','
					nml.advance();
					if (nml.getToken1() != NanoMorphoLexer.NAME)
						syntaxError("parameter name", nml.getLexeme1());
					nml.advance();
				}
			}
		}

		if (nml.getToken1() != ')') // )
			syntaxError(") or parameter name", nml.getLexeme1());
		nml.advance();
		if (nml.getToken1() != '{') // {
			syntaxError("{", nml.getLexeme1());
		nml.advance();

		while (nml.getToken1() == NanoMorphoLexer.VAR) {
			decl(depth+1);
			while (nml.getToken1() == ',') { // ','
				nml.advance();
				if (nml.getToken1() != NanoMorphoLexer.NAME)
					syntaxError("variable name", nml.getLexeme1());
				nml.advance();
			}
			if (nml.getToken1() != ';') // ;
				syntaxError(";", nml.getLexeme1());
			nml.advance();
		}

		expr(depth+1);
		if (nml.getToken1() != ';') // ;
			syntaxError(";", nml.getLexeme1());
		nml.advance();
		while (nml.getToken1() != '}') { // }
			expr(depth+1);
			if (nml.getToken1() != ';') // ;
				syntaxError(";", nml.getLexeme1());
			nml.advance();
		}

		if (nml.getToken1() != '}') // }
			syntaxError("}", nml.getLexeme1());
		nml.advance();
	}


	// decl		= 'var', NAME, { ',', NAME }
	//			;
	private static void decl(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering decl at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		if (nml.getToken1() != NanoMorphoLexer.VAR)
			syntaxError("var", nml.getLexeme1());
		nml.advance();
		if (nml.getToken1() != NanoMorphoLexer.NAME)
			syntaxError("a variable name", nml.getLexeme1());
		if (DEBUG) System.out.print(String.format("Parsing declaration: var %s",
				nml.getLexeme1()));
		nml.advance();
		while (nml.getToken1() == ',') { // ','
			nml.advance();
			if (nml.getToken1() != NanoMorphoLexer.NAME)
				syntaxError("a variable name", nml.getLexeme1());
			if (DEBUG) System.out.print(String.format(", %s",
					nml.getLexeme1()));
			nml.advance();
		}
		if (DEBUG) System.out.println(";");
	}

	// expr		= 'return', expr
	//			| NAME, '=', expr
	//			| orexpr
	//			;
	private static void expr(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering expr at depth %d with: (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		if (nml.getToken1() == NanoMorphoLexer.RETURN) {
			if (DEBUG) System.out.println(String.format("Parsing return"));
			nml.advance();
			expr(depth+1);
		} else if (
				nml.getToken1() == NanoMorphoLexer.NAME &&
				nml.getToken2() == NanoMorphoLexer.OPNAME &&
				nml.getLexeme2().equals("=")
				) {
			if (DEBUG) System.out.println(String.format("Parsing assignment"));
			nml.advance();
			if (nml.getLexeme1().equals("=")) {
				expr(depth+1);
			} else {
				syntaxError("= in expression", nml.getLexeme1());
			}
		} else{
			if (DEBUG) System.out.println(String.format("Parsing orexpr"));
			orexpr(depth+1);
		}
	}

	// orexpr	= andexpr, [ '||', orexpr ]
	//			;
	private static void orexpr(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering orexpr at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		andexpr(depth+1);
		if (nml.getToken1() == NanoMorphoLexer.OPNAME && 
				nml.getLexeme1() == "|| in expression") { // ==
			nml.advance();
				orexpr(depth+1);
		}
	}

	// andexpr	= notexpr, [ '&&', andexpr ]
	//			;
	private static void andexpr(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering andexpr at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		notexpr(depth+1);
		if (nml.getToken1() == NanoMorphoLexer.OPNAME &&
				nml.getLexeme1() == "&& in expression") {
			nml.advance();
			andexpr(depth+1);
		}
	}

	private static Char[] opname1= {"?", "~", "^"};
	private static Char[] opname2= {":"};
	private static Char[] opname3= {"|"};
	private static Char[] opname4 = {"&"};
	private static Char[] opname5 = {"<", ">", "!", "="};
	private static Char[] opname6 = {"+", "-"};
	private static Char[] opname7 = {"*", "/", "%"};

	private static void binopexpr1(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering binopexpr1 at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		binopexpr2(depth+1);
		while (Arrays.asList(opname1).contains(nml.getLexeme1())) {
			binopexpr2(depth+1);
		}
	}

	private static void binopexpr2(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering binopexpr2 at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		// TODO Make right-associative
		binopexpr3(depth+1);
		while (Arrays.asList(opname2).contains(nml.getLexeme1())) {
			binopexpr3(depth+1);
		}
	}

	private static void binopexpr3(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering binopexpr3 at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		binopexpr4(depth+1);
		while (Arrays.asList(opname3).contains(nml.getLexeme1())) {
			binopexpr4(depth+1);
		}
	}

	private static void binopexpr4(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering binopexpr4 at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		binopexpr5(depth+1);
		while (Arrays.asList(opname4).contains(nml.getLexeme1())) {
			binopexpr5(depth+1);
		}
	}

	private static void binopexpr5(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering binopexpr5 at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		// TODO handle == != >= &c.
		binopexpr6(depth+1);
		while (Arrays.asList(opname5).contains(nml.getLexeme1())) {
			binopexpr6(depth+1);
		}
	}

	private static void binopexpr6(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering binopexpr6 at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		binopexpr7(depth+1);
		while (Arrays.asList(opname6).contains(nml.getLexeme1())) {
			binopexpr7(depth+1);
		}
	}

	private static void binopexpr7(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering binopexpr7 at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		smallexpr(depth+1);
		while (Arrays.asList(opname7).contains(nml.getLexeme1())) {
			smallexpr(depth+1);
		}
	}

	// notexpr	= '!', notexpr | binopexpr1
	//			;
	private static void notexpr(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering notexpr at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		if (nml.getLexeme1().equals("!")) {
			nml.advance();
			notexpr(depth+1);
		} else {
			binopexpr1(depth+1);
		}
	}

	// smallexpr	= NAME
	//				| NAME, '(', [ expr, { ',', expr } ], ')'
	//				| opname, smallexpr
	//				| LITERAL
	//				| '(', expr, ')'
	//				| ifexpr
	//				| 'while', '(', expr, ')', body
	//				;
	private static void smallexpr(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering smallexpr at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		if (nml.getToken1() == NanoMorphoLexer.NAME && nml.getToken2() == '(') { // NAME(...)
			nml.advance();
			nml.advance();
			if (nml.getToken1() != ')') {
				expr(depth+1);
				while (nml.getToken1() == ',') {
					expr(depth+1);
				}
			}
			if (nml.getToken1() != ')')
				syntaxError(")", nml.getLexeme1()); // )
			nml.advance();
		}
		else if (nml.getToken1() == NanoMorphoLexer.NAME) {
			nml.advance();
			return;
		}
		else if (nml.getToken1() == NanoMorphoLexer.OPNAME) { // opname, smallexpr
			nml.advance();
			smallexpr(depth+1);
		}
		else if (nml.getToken1() == NanoMorphoLexer.LITERAL) {
			nml.advance();
		}
		else if (nml.getToken1() == '(') { // ( expr )
			nml.advance();
			expr(depth+1);
			if (nml.getToken1() != ')') // )
				syntaxError(")", nml.getLexeme1());
			nml.advance();
		}
		else if (nml.getToken1() == NanoMorphoLexer.IF ||
				nml.getToken1() == NanoMorphoLexer.ELSIF
				) { 
			ifexpr(depth+1);
		}
		else if (nml.getToken1() == NanoMorphoLexer.WHILE) {
			nml.advance();
			if (nml.getToken1() != '(') syntaxError("(", nml.getLexeme1());
			nml.advance();
			expr(depth+1);
			if (nml.getToken1() != ')') syntaxError(")", nml.getLexeme1());
			nml.advance();
			body(depth+1);
		}
		else {
			syntaxError("expression", nml.getLexeme1());
		}
	}


	//ifexpr 		=	'if', '(', expr, ')' body, 
	//					{ 'elsif', '(', expr, ')', body }, 
	//					[ 'else', body ]
	//				;
	private static void ifexpr(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering ifexpr at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		if (nml.getToken1() != NanoMorphoLexer.IF) syntaxError("if", nml.getLexeme1());
		nml.advance();
		if (nml.getToken1() != '(') syntaxError("(", nml.getLexeme1());
		nml.advance();
		expr(depth+1);
		if (nml.getToken1() != ')') syntaxError(")", nml.getLexeme1());
		nml.advance();
		body(depth+1);
		
		while (nml.getToken1() == NanoMorphoLexer.ELSIF) {
			nml.advance();
			if (nml.getToken1() != '(') syntaxError("(", nml.getLexeme1());
			nml.advance();
			expr(depth+1);
			if (nml.getToken1() != ')') syntaxError(")", nml.getLexeme1());
			nml.advance();
			body(depth+1);
		}

		//else token
		if (nml.getToken1() == NanoMorphoLexer.ELSE) {
			nml.advance();
			body(depth+1);
		}
	}

	// body = '{', { expr, ';' }, '}'
	//		;
	private static void body(int depth) throws Exception {
		if (DEBUG) System.out.println(
				String.format(
					"Entering body at depth %d with (%s,%s)",
					depth,
					nml.getLexeme1(),
					nml.getLexeme2()
					)
				);
		if (nml.getToken1() != '{') syntaxError("{", nml.getLexeme1());
		nml.advance();
		while (nml.getToken1() != '}') { // '}'
			expr(depth+1);
			if (nml.getToken1() != ';') // ;
				syntaxError(";", nml.getLexeme1());
			nml.advance();
		}
		if (nml.getToken1() != '}') syntaxError("}", nml.getLexeme1());
		nml.advance();
	}
}
