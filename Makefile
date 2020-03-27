
all: NanoMorphoLexer.class NanoMorphoParser.class

test: all
	echo "not implemented"

NanoMorphoLexer.class NanoMorphoParser.class: NanoMorphoLexer.java NanoMorphoParser.java
	javac NanoMorphoLexer.java NlnoMorphoGeneratedParser.java NanoMorphoGeneratedParserVal.java

NanoMorphoLexer.java: NanoMorphoParser.java
	java -jar jflex-full-1.7.0.jar nanoMorphoLexer.jflex

NanoMorphoParser.java:
	byacc -Jclass=NanoMorphoGeneratedParser nanoMorpho.byaccj

clean:
	rm -rf *.class *~ NanoMorphoGenerated*.java NanoMorphoLexer* *.bak yacc.* *.mexe
