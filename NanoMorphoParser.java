public class NanoMorhoParser {
	private static nml;
	public static void main(String[] args) {
		nml = new NanoMorphoLexer(args[0]);
		program();
	}

	private static void syntaxError(String expected, String got) {
		//throw new Error(
		System.out.println(
				"Syntax error! Expected %s, but got %s."
				.format(expected,got)
				);
	}

	private static void program() {
		function();
	}

	private static void function() {
		// Add code here
	}

	// decl		=	'var', NAME, { ',', NAME }
	// 			;
	private static void decl() {
		if (nml.getToken1() != 1009) syntaxError("var", nml.getLexeme());
		nml.advance();
		if (nml.getToken1() != 1009) syntaxError("NAME", nml.getLexeme());
		nml.advance();
		while (nml.getToken1() == 44) { // ','
			nml.advance();
			if (nml.getToken1() != 1009) syntaxError("NAME", nml.getLexeme());
			nml.advance();
		}
	}

}
