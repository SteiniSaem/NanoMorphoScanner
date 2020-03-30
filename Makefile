
all: lexer.class parser.class finalcode.class

test: all
	java NanoMorphoFinalCodeGenerator tests/tinymorphotest.morpho

lexer.class: lexer.java
	javac NanoMorphoLexer.java

parser.class: byacc
	javac Parser.java ParserVal.java

finalcode.class:
	javac NanoMorphoFinalCodeGenerator.java

lexer.java:
	java -jar jflex-full-1.7.0.jar nanoMorphoLexer.jflex

byacc parser.java:
	byacc -J nanoMorpho.byaccj

clean:
	rm -rf *.class *~  Parser* NanoMorphoLexer* *.bak yacc.* *.mexe y.*
