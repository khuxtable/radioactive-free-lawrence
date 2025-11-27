package org.kathrynhuxtable.acode.grammar;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;

public class ErrorReporter {
	private final List<String> errors = new ArrayList<>();

	public void reportError(ParserRuleContext context, String message) {
		int line = context == null ? 0 : context.start.getLine();
		int charPositionInLine = context == null ? 0 : context.start.getCharPositionInLine();
		errors.add("Error at line " + line + ":" + charPositionInLine + " - " + message);
	}

	public boolean hasErrors() {
		return !errors.isEmpty();
	}

	public List<String> getErrors() {
		return errors;
	}
}
