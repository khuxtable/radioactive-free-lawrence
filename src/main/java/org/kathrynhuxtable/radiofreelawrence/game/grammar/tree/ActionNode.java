package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
public class ActionNode implements BaseNode {
	private String arg1;
	private List<ActionCode> actionCodes = new ArrayList<>();
	private SourceLocation sourceLocation;

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ActionCode implements BaseNode {
		private String arg2;
		private BlockNode code;
		private SourceLocation sourceLocation;
	}
}
