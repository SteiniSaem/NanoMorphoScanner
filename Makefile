
all: NanoMorphoLexer.class generatedParser.class 

test: all
	echo "not implemented"

NanoMorphoLexer.class generatedParser.class: NanoMorphoLexer.java generatedParser.java
	javac NanoMorphoLexer.java generatedParserVal.java generatedParser.java

NanoMorphoLexer.java:
	java -jar jflex-full-1.7.0.jar nanoMorphoLexer.jflex

byacc generatedParser.java:
	byacc -Jclass=generatedParser nanoMorpho.byaccj

clean:
	rm -rf *.class *~  generated* NanoMorphoLexer* *.bak yacc.* *.mexe y.*
