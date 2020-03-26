
all: NanoMorphoLexer.class NanoMorphoParser.class

test: all
	echo "not implemented"

NanoMorphoLexer.class NanoMorphoParser.class: NanoMorphoLexer.java
	javac NanoMorphoLexer.java NanoMorphoParser.java

NanoMorphoLexer.java:
	java -jar jflex-full-1.7.0.jar nanoMorphoLexer.jflex

ExprParser.java:
	./byacc -Jclass=ExprParser mformula.byaccj

clean:
	rm -rf *.class *~ *.java *.bak yacc.* *.mexe
