/**
	JFlex scanner example based on a scanner for NanoMorpho.
	Authors:
	Kristófer Ásgeirsson (kra33)
	Valur Páll S. Valsson (vpv1)
	Þorsteinn Sæmundsson (ths265)

	This stand-alone scanner/lexical analyzer can be built and run using:
		java -jar JFlex-full-1.7.0.jar nanoMorphoLexer.jflex
		javac NanoMorphoLexer.java
		java NanoMorphoLexer inputfile > outputfile
	Also, the program 'make' can be used with the proper 'makefile':
		make test
 */

import java.io.*;

%%

%public
%class NanoMorphoLexer
%unicode
%byaccj

%{

// This part becomes a verbatim part of the program text inside
// the class, NanoMorphoLexer.java, that is generated.

// Definitions of tokens:
final static int ERROR = -1;
final static int IF = 1001;
final static int DEFINE = 1002;
final static int NAME = 1003;
final static int LITERAL = 1004;
final static int ELSIF = 1005;
final static int ELSE = 1006;
final static int WHILE = 1007;
final static int RETURN = 1008;
final static int VAR = 1009;
final static int OPNAME = 1010;

// A variable that will contain lexemes as they are recognized:
private static String lexeme;
private int token1;
private int token2;
private String currentLexeme;
private String nextLexeme;

public void init() {
	this.nextLexeme = lexeme;
	try {
		this.token2 = this.yylex();
	} catch (Exception e) {
		this.token1 = -1;
		System.out.println("Error reading next token.");
	}
	advance();
}

public int getToken1() {
	return this.token1;
}

public int getToken2() {
	return this.token2;
}

public String getLexeme1() {
	return this.currentLexeme;
}

public String getLexeme2() {
	return this.nextLexeme;
}

public void advance() {
	this.token1 = this.token2;
	this.currentLexeme = lexeme;
	try {
		this.currentLexeme = this.nextLexeme;
		this.token2 = this.yylex();
	} catch(Exception e) {
		this.token2 = -1;
		this.nextLexeme = "";
		System.out.println("Error reading next token.");
	}
}

// This runs the scanner:
public static void main( String[] args ) throws Exception
{
	NanoMorphoLexer lexer = new NanoMorphoLexer(new FileReader(args[0]));
	int token = lexer.yylex();
	while( token!=0 )
	{
		System.out.println(""+token+": \'"+lexeme+"\'");
		token = lexer.yylex();
	}
}

%}

  /* Reglulegar skilgreiningar */

  /* Regular definitions */

_DIGIT=[0-9]
_FLOAT={_DIGIT}+\.{_DIGIT}+([eE][+-]?{_DIGIT}+)?
_INT={_DIGIT}+
_STRING=\"([^\"\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|\\[0-7][0-7]|\\[0-7])*\"
_CHAR=\'([^\'\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|(\\[0-7][0-7])|(\\[0-7]))\'
_DELIM=[(){};,]
_NAME=([:letter:]|{_DIGIT})+
_OPNAME=([\+\-*/!%=><\:\^\~&|?]|==|--|\+\+|<=|>=|\!=|\|\||&&)
_SINGLE_COMMENT=(;;;.*(\n|\r))
_MULTI_COMMENT=(;;\*.*((\n|\r).*)+\*;;)


%%

  /* Lesgreiningarreglur */
  /* Scanning rules */

{_MULTI_COMMENT} {
	lexeme = yytext();
}

{_SINGLE_COMMENT} {
	lexeme = yytext();
}

{_DELIM} {
	lexeme = yytext();
	return yycharat(0);
}

{_STRING} | {_FLOAT} | {_CHAR} | {_INT} | null | true | false {
	lexeme = yytext();
	return LITERAL;
}


"if" {
	lexeme = yytext();
	return IF;
}

"elsif" {
	lexeme = yytext();
	return ELSIF;
}

"else" {
	lexeme = yytext();
	return ELSE;
}


"while" {
	lexeme = yytext();
	return WHILE;
}


{_OPNAME} {
	lexeme = yytext();
	return OPNAME;
}

"var" {
	lexeme = yytext();
	return VAR;
}

"return" {
	lexeme = yytext();
	return RETURN;
}

{_NAME} {
	lexeme = yytext();
	return NAME;
}


";".*$ {
}

[ \t\r\n\f] {
}

. {
	lexeme = yytext();
	return ERROR;
}
