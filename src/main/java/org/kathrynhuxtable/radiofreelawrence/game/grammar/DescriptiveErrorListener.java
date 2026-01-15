package org.kathrynhuxtable.radiofreelawrence.game.grammar;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

// Example of a custom error listener including filename
public class DescriptiveErrorListener extends BaseErrorListener {
	public static final DescriptiveErrorListener INSTANCE = new DescriptiveErrorListener();

	@Override
	public void syntaxError(Recognizer<?, ?> recognizer,
	                        Object offendingSymbol,
	                        int line, int charPositionInLine,
	                        String msg,
	                        RecognitionException e) {
		String sourceName = recognizer.getInputStream().getSourceName();
		SourceLocation sourceLocation = new SourceLocation(sourceName, line, charPositionInLine);
		System.err.println(sourceLocation + ": " + msg);
	}
}
