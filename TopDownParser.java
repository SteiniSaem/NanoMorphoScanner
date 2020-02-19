public class TopDownParser
{
	public static void main(String[] args) 
	{

	}
}

private class Lexer
{
	private NanoMorphoLexer myLexer;
	private int tok1, tok;
	private String lex1, lex2;
	public Lexer(String fileName)
	{
		this.myLexer = new NanoMorphoLexer(new FileReader(fileName));
	}

	public static void init()
	{
		tok1 = myLexer.yylex();
		tok2 = myLexer.yylex();
	}

}