
all: NanoMorphoLexer.class Parser.class FinalCodeGenerator.class

test: all
	java NanoMorphoFinalCodeGenerator tests/tinymorphotest.morpho | java -jar morpho.jar -c
	java -jar morpho.jar tests/tinymorphotest
	java NanoMorphoFinalCodeGenerator tests/morphotest.morpho | java -jar morpho.jar -c
	java -jar morpho.jar tests/morphotest

NanoMorphoLexer.class Parser.class FinalCodeGenerator.class: NanoMorphoLexer.java Parser.java
	javac NanoMorphoLexer.java Parser.java ParserVal.java NanoMorphoFinalCodeGenerator.java

NanoMorphoLexer.java:
	java -jar jflex-full-1.7.0.jar nanoMorphoLexer.jflex

byacc Parser.java:
	byacc -J nanoMorpho.byaccj

clean:
	rm -rf *.class *~  Parser* NanoMorphoLexer* *.bak yacc.* tests/*.mexe y.*
