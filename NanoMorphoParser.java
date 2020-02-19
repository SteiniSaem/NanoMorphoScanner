public class NanoMorphoParser {
	private static nml;
	public static void main(String[] args) {
		nml = new NanoMorphoLexer(args[0]);
		program();
	}

	private static void program() {
		function();
	}

	private static void function() {
		// Add code here

	}

	private static void ifexpr() {
		if (nml.getToken1() != 1001) syntaxError("if", nml.getLexeme());
		nml.advance();
		if (nml.getToken1() != 40) syntaxError("(", nml.getLexeme());
		nml.advance();
		expr();
		if (nml.getToken1() != 41) syntaxError(")", nml.getLexeme());
		nml.advance();
		if (nml.getToken1() != 123) syntaxError("{", nml.getLexeme());
		expr();
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
			expr();
			nml.advance();
			if (nml.getToken1() != 125) syntaxError("}", nml.getLexeme());
		}

		//else token
		if (nml.getToken1() == 1006) {
			if (nml.getToken1() != 123) syntaxError("{", nml.getLexeme());
			nml.advance();
			expr();
			nml.advance();
			if (nml.getToken1() != 125) syntaxError("}", nml.getLexeme());
		}

		}
	}

}
