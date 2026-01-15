package org.kathrynhuxtable.radiofreelawrence.game.grammar;

import lombok.Data;
import org.antlr.v4.runtime.IntStream;
import org.antlr.v4.runtime.ParserRuleContext;

@Data
public class SourceLocation {
	private String filePath;
	private int line;
	private int charPositionInLine;

	public SourceLocation(String filePath, int line, int charPositionInLine) {
		if (filePath != null && !filePath.isEmpty() && !filePath.equals(IntStream.UNKNOWN_SOURCE_NAME)) {
			this.filePath = filePath;
		}
		this.line = line;
		this.charPositionInLine = charPositionInLine;
	}

	public SourceLocation(ParserRuleContext context) {
		line = context == null ? 0 : context.start.getLine();
		charPositionInLine = context == null ? 0 : context.start.getCharPositionInLine();
		filePath = null;
		if (context != null && context.start.getInputStream() != null) {
			filePath = context.start.getInputStream().getSourceName();
		}
	}

	public String toString() {
		if (filePath == null) {
			return "line " + line + ":" + charPositionInLine;
		} else {
			return filePath + ":" + line + ":" + charPositionInLine;
		}
	}
}
