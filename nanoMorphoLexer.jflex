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

private static String lexeme1;
private static String lexeme2;
private static int token1;
private static int token2;
private static NanoMorphoLexer lexer;
private static Parser yyparser;
private static int line1, column1, line2, column2;

public static void startLexer( String filename, Parser parser ) throws Exception
{
	yyparser = parser;
	Reader reader;
	if (filename == null) reader = new BufferedReader(new InputStreamReader(System.in));
	else reader = new FileReader(filename);
	lexer = new NanoMorphoLexer(reader);
	token2 = lexer.yylex();
	line2 = lexer.yyline;
	column2 = lexer.yycolumn;
	advance();
}

public static String advance() throws Exception
{
	String res = lexeme1;
	token1 = token2;
	lexeme1 = lexeme2;
	line1 = line2;
	column1 = column2;
	if( token2==0 ) return res;
	token2 = lexer.yylex();
	line2 = lexer.yyline;
	column2 = lexer.yycolumn;
	return res;
}

public static int getLine()
{
	return line1+1;
}

public static int getColumn()
{
	return column1+1;
}

public static int getToken1()
{
	return token1;
}

public static int getToken2()
{
	return token2;
}

public static String getLexeme()
{
	return lexeme1;
}

private static void expected( int tok )
{
	expected(tokname(tok));
}

private static void expected( char tok )
{
	expected("'"+tok+"'");
}

public static void expected( String tok )
{
	throw new Error("Expected "
		+ tok 
		+ ", found '"
		+ lexeme1
		+ "' near line "
		+ (line1+1)
		+ ", column "
		+ (column1+1));
}

private static String tokname( int tok )
{
	switch( tok )
	{
	case Parser.IF:
		return "if";
	case Parser.ELSE:
		return "else";
	case Parser.ELSIF:
		return "elsif";
	case Parser.WHILE:
		return "while";
	case Parser.VAR:
		return "var";
	case Parser.RETURN:
		return "return";
	case Parser.NAME:
		return "name";
	case Parser.OPNAME:
		return "operation";
	case Parser.LITERAL:
		return "literal";
	default:
		return ""+(char)tok;
	}
}

public static String over( int tok ) throws Exception
{
	if( token1!=tok ) expected(tok);
	String res = lexeme1;
	advance();
	return res;
}

public static String over( char tok ) throws Exception
{
	if( token1!=tok ) expected(tok);
	String res = lexeme1;
	advance();
	return res;
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
