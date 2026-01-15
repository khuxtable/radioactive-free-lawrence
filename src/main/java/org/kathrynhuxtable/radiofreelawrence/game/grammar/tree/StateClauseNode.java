package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@AllArgsConstructor
public class StateClauseNode implements BaseNode {

	private String state;
	private ExprNode value;
	private SourceLocation sourceLocation;
}
