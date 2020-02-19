public class NanoMorphoParser {
	private static nml;
	public static void main(String[] args) {
		nml = new NanoMorphoLexer(args[0]);
		program();
	}

	private static void syntaxError(String expected, String got) {
		// throw new Error(
		System.out.println("Syntax error! Expected %s, but got %s.".format(expected, got));
	}

	// program = { function }
	// ;
	private static void program() {
		function();
	}

	// function = NAME, '(', [ NAME, { ',', NAME } ] ')'
	// '{', { decl, ';' }, { expr, ';' }, '}'
	// ;
	private static void function() {
		// TODO
		// Add code here

	}

<<<<<<< HEAD
=======
	<<<<<<<HEAD

	private static void expr() {
		if (nlm.getToken1() == 1008) { // RETURN
			nml.advance();
			expr();
		} else if (nml.getToken1() == 1003 && nml.getToken2() === 1010) { // NAME, OPNAME
			nml.advance();
			if (nml.getLexeme() == "=") {
				nml.advance();
				expr();
			} else {
				syntaxError("=", nml.getLexeme());
			}
		} else{
			orexpr();
		}
	}

	=======>>>>>>>7 a6c1fc66e1f22f4b199e33c9205d776e548a8d8
>>>>>>> 10df223428ba3fcfa4adef4b1920ef1ec23da1c0

	// decl = 'var', NAME, { ',', NAME }
	// ;
	private static void decl() {
		if (nml.getToken1() != 1009)
			syntaxError("var", nml.getLexeme());
		nml.advance();
		if (nml.getToken1() != 1009)
			syntaxError("NAME", nml.getLexeme());
		nml.advance();
		while (nml.getToken1() == 44) { // ','
			nml.advance();
			if (nml.getToken1() != 1009)
				syntaxError("NAME", nml.getLexeme());
			nml.advance();
		}
	}

	// expr = 'return', expr
	// | NAME, '=', expr
	// | orexpr
	// ;
	private static void expr() {
		if (nlm.getToken1() == 1008) { // RETURN
			nml.advance();
			expr();
		} else if (nml.getToken1() == 1003 && nml.getToken2() === 1010) { // NAME, OPNAME
			nml.advance();
			if (nml.getLexeme() == "=") {
				return expr();
			} else {
				syntaxError("=", nml.getLexeme());
			}
		} else{
			nml.advance();
			orexpr();
		}
	}

	// orexpr = andexpr, [ '||', orexpr ]
	// ;
	private static void orexpr() {
		andexpr();
		if (nml.getToken1() == 1010 && // OPNAME
				nml.getLexeme() == "||") { // ==
			nml.advance()
				orexpr();
		}
	}

	// andexpr = notexpr, [ '&&', andexpr ]
	// ;
	private static void andexpr() {
		notexpr();
		if (nml.getToken1() == 1010 && // OPNAME
				nml.getLexeme() == "&&") {
			nml.advance();
			andexpr();
		}
	}

	private String[] opname1= {"<", ">", ">=", "<=", "=="};
	private String[] opname2= {"+", "-"};
	private String[] opname3 = { "*", "/" };
	private String[] opname4 = {};

	private static void binopexpr1() {
		nml.advance();
		binopexpr2();
		while (Arrays.asList(opname1).contains(nml.getLexeme)) {
			nml.advance();
			binopexpr2();
		}
	}

	// notexpr = '!', notexpr | binopexpr1
	// ;
	private static void notexpr() {
		if (nml.getLexeme() == "!") {
			nml.advance();
			notexpr();
		} else {
			binopexpr1();
		}
	}

	// smallexpr = NAME
	// | NAME, '(', [ expr, { ',', expr } ], ')'
	// | opname, smallexpr
	// | LITERAL
	// | '(', expr, ')'
	// | ifexpr
	// | 'while', '(', expr, ')', body
	// ;
	private static void smallexpr() {
		if (nml.getToken1() == 1003 && nml.getToken2() == 40) { // NAME(...)
			token.advance();
			token.advance();
			// TODO
			// [ expr, {',', expr } ]
			if (nml.getToken1() != 41)
				syntaxError(")", nml.getLexeme()); // )
		}
		if (nml.getToken1() == 1003) { // NAME
			nml.advance();
			return;
		}
	}


	//ifexpr 		=	'if', '(', expr, ')' body, 
	//					{ 'elsif', '(', expr, ')', body }, 
	//					[ 'else', body ]
	//				;
	private static void ifexpr() {
		if (nml.getToken1() != 1001) syntaxError("if", nml.getLexeme());
		nml.advance();
		if (nml.getToken1() != 40) syntaxError("(", nml.getLexeme());
		nml.advance();
		expr();
		if (nml.getToken1() != 41) syntaxError(")", nml.getLexeme());
		nml.advance();
		if (nml.getToken1() != 123) syntaxError("{", nml.getLexeme());
		body();
		nml.advance();
		if (nml.getToken1() != 125) syntaxError("}", nml.getLexeme());

		//elsif token
		while (nml.getToken1() == 1005) {
			nml.advance(); //til þess að komast út úr elsif tokeninu
			if (nml.getToken1() != 40) syntaxError("(", nml.getLexeme());
			nml.advance();
			expr();
			if (nml.getToken1() != 41) syntaxError(")", nml.getLexeme());
			nml.advance();
			if (nml.getToken1() != 123) syntaxError("{", nml.getLexeme());
			body();
			nml.advance();
			if (nml.getToken1() != 125) syntaxError("}", nml.getLexeme());
		}

		//else token
		if (nml.getToken1() == 1006) {
			if (nml.getToken1() != 123) syntaxError("{", nml.getLexeme());
			nml.advance();
			body();
			nml.advance();
			if (nml.getToken1() != 125) syntaxError("}", nml.getLexeme());
		}


	// ifexpr = 'if', '(', expr, ')' body,
	// { 'elsif', '(', expr, ')', body },
	// [ 'else', body ]
	// ;
	private static ifexpr() {
		// TODO
	}

	// body = '{', { expr, ';' }, '}'
	// ;
	private static void body() {
		// TODO
	}
}
