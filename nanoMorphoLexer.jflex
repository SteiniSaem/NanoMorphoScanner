/**
	Höfundur: Snorri Agnarsson, 2017-2020
	Viðbætur:
	Kristófer Ásgeirsson
	Valur Páll S. Valsson
	Þorsteinn Sæmundsson

	Þennan þáttara má þýða og keyra með skipununum
		java -jar JFlex-full-1.7.0.jar nanomorpholexer.jflex
		javac NanoMorphoLexer.java NanoMorphoParser.java
		java NanoMorphoParser inntaksskrá
	Einnig má nota forritið 'make', ef viðeigandi 'makefile'
	er til staðar:
		make test
 */

import java.io.*;

%%

%public
%class NanoMorphoLexer
%unicode
%byaccj
%line
%column

%{
private static NanoMorphoLexer lexer;
private static Parser yyparser;

public NanoMorphoLexer( java.io.Reader r, Parser yyparser )
{
	this(r);
	this.yyparser = yyparser;
}

public int getLine() {
	return yyline+1;
}

public int getColumn() {
	return yycolumn+1;
}

%}

  /* Reglulegar skilgreiningar */

  /* Regular definitions */

_DIGIT=[0-9]
_FLOAT={_DIGIT}+\.{_DIGIT}+([eE][+-]?{_DIGIT}+)?
_INT={_DIGIT}+
_STRING=\"([^\"\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|\\[0-7][0-7]|\\[0-7])*\"
_CHAR=\'([^\'\\]|\\b|\\t|\\n|\\f|\\r|\\\"|\\\'|\\\\|(\\[0-3][0-7][0-7])|(\\[0-7][0-7])|(\\[0-7]))\'
_DELIM=[(){},;=]
_NAME=([:letter:]|{_DIGIT})+
_OPNAME=[\+\-*/!%&=><\:\^\~&|?]+

%%

  /* Lesgreiningarreglur */

{_DELIM} {
	yyparser.yylval = new ParserVal(yytext());
	return yycharat(0);
}

{_STRING} | {_FLOAT} | {_CHAR} | {_INT} | null | true | false {
	yyparser.yylval = new ParserVal(yytext());
	return Parser.LITERAL;
}

"if" {
	return Parser.IF;
}

"else" {
	return Parser.ELSE;
}

"elsif" {
	return Parser.ELSIF;
}

"while" {
	return Parser.WHILE;
}

"var" {
	return Parser.VAR;
}

"return" {
	return Parser.RETURN;
}

{_NAME} {
	yyparser.yylval = new ParserVal(yytext());
	return Parser.NAME;
}


{_OPNAME} {
	yyparser.yylval = new ParserVal(yytext());
	switch (yytext().charAt(0)) {
	    case '?':
	    case '~':
	        return Parser.OP1;
	    case ':':
	        return Parser.OP2;
	    case '|':
	        return Parser.OP3;
	    case '&':
	        return Parser.OP4;
	    case '!':
	    case '=':
	    case '<':
	    case '>':
	        return Parser.OP5;
	    case '+':
	    case '-':
	        return Parser.OP6;
	    case '*':
	    case '/':
	    case '%':
	        return Parser.OP7;
	    case '^':
	        return Parser.OP8;
	    default:
	        throw new Error("Invalid opname");
	}
}

"###"(.*(\n|\r))+.*"###"|"#".*$ {
}

[ \t\r\n\f] {
}

. {
	return Parser.ERROR;
}
