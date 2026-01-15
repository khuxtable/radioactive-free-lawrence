package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.*;

import lombok.*;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
public class StateNode implements BaseNode {

	private Map<String, StateClauseNode> states = new LinkedHashMap<>();
	private SourceLocation sourceLocation;
}
