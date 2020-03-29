
all: NanoMorphoLexer.class Parser.class FinalCodeGenerator.class

test: all
	java NanoMorphoFinalCodeGenerator tests/tinymorphotest.morpho

NanoMorphoLexer.class Parser.class FinalCodeGenerator.class: NanoMorphoLexer.java Parser.java
	javac NanoMorphoLexer.java Parser.java ParserVal.java NanoMorphoFinalCodeGenerator.java

NanoMorphoLexer.java:
	java -jar jflex-full-1.7.0.jar nanoMorphoLexer.jflex

byacc Parser.java:
	byacc -J nanoMorpho.byaccj

clean:
	rm -rf *.class *~  Parser* NanoMorphoLexer* *.bak yacc.* *.mexe y.*
