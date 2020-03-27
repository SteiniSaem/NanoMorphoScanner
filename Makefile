
all: NanoMorphoLexer.class NanoMorphoParser.class byacc

test: all
	echo "not implemented"

NanoMorphoLexer.class NanoMorphoParser.class: NanoMorphoLexer.java
	javac NanoMorphoLexer.java NanoMorphoParser.java

NanoMorphoLexer.java:
	java -jar jflex-full-1.7.0.jar nanoMorphoLexer.jflex

byacc:
	byacc -Jclass=NanoMorphoParser nanoMorpho.byaccj

clean:
	rm -rf *.class *~  NanoMorphoLexer* *.bak yacc.* *.mexe y.*
