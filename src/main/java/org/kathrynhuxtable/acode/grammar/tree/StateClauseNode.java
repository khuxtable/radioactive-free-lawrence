package org.kathrynhuxtable.acode.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StateClauseNode implements AcdNode {

	private String state;
	private ExprNode value;
}
