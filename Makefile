
all: NanoMorphoLexer.class Parser.class 

test: all
	echo "not implemented"

NanoMorphoLexer.class Parser.class: NanoMorphoLexer.java Parser.java
	javac NanoMorphoLexer.java Parser.java ParserVal.java

NanoMorphoLexer.java:
	java -jar jflex-full-1.7.0.jar nanoMorphoLexer.jflex

byacc Parser.java:
	byacc -J nanoMorpho.byaccj

clean:
	rm -rf *.class *~  Parser* NanoMorphoLexer* *.bak yacc.* *.mexe y.*
