import java.io.FileReader;
import java.io.FileNotFoundException;
import java.util.Arrays;

public class NanoMorphoParser {
	private static NanoMorphoLexer nml;
	public static void main(String[] args) throws FileNotFoundException {
		nml = new NanoMorphoLexer(new FileReader(args[0]));
		program();
	}

	private static void syntaxError(String expected, String got) {
		// throw new Error(
		System.out.println("Syntax error! Expected %s, but got %s.".format(expected, got));
	}

	// program		= { function }
	//				;
	private static void program() {
		function();
	}

	// function		= NAME, '(', [ NAME, { ',', NAME } ] ')'
	//					'{', { decl, ';' }, { expr, ';' }, '}'
	//				;
	private static void function() {
		if (nml.getToken1() != 1003) // NAME
			syntaxError("function name", nml.getLexeme());
		nml.advance();
		if (nml.getToken1() != 40) // (
			syntaxError("(", nml.getLexeme());
		nml.advance();

		if (nml.getToken1() != 41) { // )
			if (nml.getToken1() == 1003) { // NAME
				nml.advance();
				while (nml.getToken1() == 44) { // ','
					nml.advance();
					if (nml.getToken1() != 1003) // NAME
						syntaxError("parameter name", nml.getLexeme());
					nml.advance();
				}
			}

		}

		if (nml.getToken1() != 41) // )
			syntaxError(") or parameter name", nml.getLexeme());
		nml.advance();
		if (nml.getToken1() != 123) // {
			syntaxError("{", nml.getLexeme());
		nml.advance();

		while (nml.getToken1() == 1009) { // 'var'
			nml.advance();
			if (nml.getToken1() != 1003) // NAME
				syntaxError("variable name", nml.getLexeme());
			nml.advance();
		}

		while (nml.getToken1() != 125) { // expr
			expr();
		}

		if (nml.getToken1() != 125) // }
			syntaxError("}", nml.getLexeme());
		nml.advance();
	}


	// decl		= 'var', NAME, { ',', NAME }
	//			;
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

	// expr		= 'return', expr
	//			| NAME, '=', expr
	//			| orexpr
	//			;
	private static void expr() {
		if (nml.getToken1() == 1008) { // RETURN
			nml.advance();
			expr();
		} else if (nml.getToken1() == 1003 && nml.getToken2() == 1010) { // NAME, OPNAME
			nml.advance();
			if (nml.getLexeme() == "=") {
				expr();
			} else {
				syntaxError("=", nml.getLexeme());
			}
		} else{
			nml.advance();
			orexpr();
		}
	}

	// orexpr	= andexpr, [ '||', orexpr ]
	//			;
	private static void orexpr() {
		andexpr();
		if (nml.getToken1() == 1010 && // OPNAME
				nml.getLexeme() == "||") { // ==
			nml.advance();
				orexpr();
		}
	}

	// andexpr	= notexpr, [ '&&', andexpr ]
	//			;
	private static void andexpr() {
		notexpr();
		if (nml.getToken1() == 1010 && // OPNAME
				nml.getLexeme() == "&&") {
			nml.advance();
			andexpr();
		}
	}

	private static String[] opname1= {"&&","||"};
	private static String[] opname2= {"<", ">", ">=", "<=", "==", "!="};
	private static String[] opname3= {"+", "-"};
	private static String[] opname4 = { "*", "/" };
	private static String[] opname5 = {"^"};
	private static String[] opname6 = {"&","|"};
	private static String[] opname7 = {":","%"};

	private static void binopexpr1() {
		binopexpr2();
		while (Arrays.asList(opname1).contains(nml.getLexeme())) {
			binopexpr2();
		}
	}

	private static void binopexpr2() {
		binopexpr3();
		while (Arrays.asList(opname2).contains(nml.getLexeme())) {
			binopexpr3();
		}
	}

	private static void binopexpr3() {
		binopexpr4();
		while (Arrays.asList(opname3).contains(nml.getLexeme())) {
			binopexpr4();
		}
	}

	private static void binopexpr4() {
		binopexpr5();
		while (Arrays.asList(opname4).contains(nml.getLexeme())) {
			binopexpr5();
		}
	}

	private static void binopexpr5() {
		binopexpr6();
		while (Arrays.asList(opname5).contains(nml.getLexeme())) {
			binopexpr6();
		}
	}

	private static void binopexpr6() {
		binopexpr7();
		while (Arrays.asList(opname6).contains(nml.getLexeme())) {
			binopexpr7();
		}
	}

	private static void binopexpr7() {
		smallexpr();
		while (Arrays.asList(opname7).contains(nml.getLexeme())) {
			smallexpr();
		}
	}

	// notexpr	= '!', notexpr | binopexpr1
	//			;
	private static void notexpr() {
		if (nml.getLexeme() == "!") {
			nml.advance();
			notexpr();
		} else {
			binopexpr1();
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
	private static void smallexpr() {
		if (nml.getToken1() == 1003 && nml.getToken2() == 40) { // NAME(...)
			nml.advance();
			nml.advance();
			if (nml.getToken1() != 41) {
				expr();
				while (nml.getToken1() == 44) {
					expr();
				}
			}
			if (nml.getToken1() != 41)
				syntaxError(")", nml.getLexeme()); // )
			nml.advance();
		}
		if (nml.getToken1() == 1003) { // NAME
			nml.advance();
			return;
		}
		if (nml.getToken1() == 1010) { // opname, smallexpr
			nml.advance();
			smallexpr();
		}
		if (nml.getToken1() == 1004) { // LITERAL
			nml.advance();
		}
		if (nml.getToken1() == 40) { // ( expr )
			nml.advance();
			expr();
			if (nml.getToken1() != 41) // )
				syntaxError(")", nml.getLexeme());
			nml.advance();
		}
		if (nml.getToken1() == 1001) { // if (...)
			ifexpr();
		}
		if (nml.getToken1() == 1007) { // while (...)
			nml.advance();
			if (nml.getToken1() != 40) // (
				syntaxError("(", nml.getLexeme());
			nml.advance();
			expr();
			if (nml.getToken1() != 41) // )
				syntaxError(")", nml.getLexeme());
			body();
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
	}

	// body = '{', { expr, ';' }, '}'
	// ;
	private static void body() {
		if (nml.getToken1() != 123) syntaxError("{", nml.getLexeme());
		nml.advance();
		while (nml.getToken1() != 125) { // '}'
			expr();
		}
		if (nml.getToken1() != 125) syntaxError("}", nml.getLexeme());
		nml.advance();
	}
}
