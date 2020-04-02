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


public Parser yyparser;

public NanoMorphoLexer( java.io.Reader r, Parser yyparser )
{
	this(r);
	this.yyparser = yyparser;
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
	lexeme2 = yytext();
	yyparser.yylval = new ParserVal(lexeme2);
	return yycharat(0);
}

{_STRING} | {_FLOAT} | {_CHAR} | {_INT} | null | true | false {
	lexeme2 = yytext();
	yyparser.yylval = new ParserVal(lexeme2);
	return Parser.LITERAL;
}

"if" {
	lexeme2 = yytext();
	return Parser.IF;
}

"else" {
	lexeme2 = yytext();
	return Parser.ELSE;
}

"elsif" {
	lexeme2 = yytext();
	return Parser.ELSIF;
}

"while" {
	lexeme2 = yytext();
	return Parser.WHILE;
}

"var" {
	lexeme2 = yytext();
	return Parser.VAR;
}

"return" {
	lexeme2 = yytext();
	return Parser.RETURN;
}

{_NAME} {
	lexeme2 = yytext();
	yyparser.yylval = new ParserVal(lexeme2);
	return Parser.NAME;
}


{_OPNAME} {
	lexeme2 = yytext();
	yyparser.yylval = new ParserVal(lexeme2);
	return Parser.OPNAME;
}

"###"(.*(\n|\r))+.*"###"|"#".*$ {
}

[ \t\r\n\f] {
}

. {
	lexeme2 = yytext();
	return Parser.ERROR;
}
