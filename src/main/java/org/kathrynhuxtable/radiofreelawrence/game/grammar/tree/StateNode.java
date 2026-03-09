package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.MyClassVisitor;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
public class StateNode implements DeclaratorNode {

	private Map<String, StateClauseNode> states = new LinkedHashMap<>();
	private SourceLocation sourceLocation;

	@Override
	public void generate(MyClassVisitor cv, GameContext gameContext) {

	}
}
