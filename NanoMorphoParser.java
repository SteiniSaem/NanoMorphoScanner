public class NanoMorhoParser {
	private static nml;
	public static void main(String[] args) {
		nml = new NanoMorphoLexer(args[0]);
		program();
	}

	private static void syntaxError(String expected, String got) {
		// throw new Error(
		System.out.println("Syntax error! Expected %s, but got %s.".format(expected, got));
	}

	private static void program() {
		function();
	}

	private static void function() {
		// Add code here
	}

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

	//notexpr		=	'!', notexpr | binopexpr1
	//				;
	private static void notexpr() {
		if (nml.getLexeme() == "!") {
			nml.advance();
			notexpr();
		} else {
			binopexpr1();
		}
	}
}
