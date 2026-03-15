package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.LinkedHashMap;
import java.util.Map;

import lombok.Data;
import org.objectweb.asm.ClassVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
public class StateNode implements DeclaratorNode {

	private Map<String, StateClauseNode> states = new LinkedHashMap<>();
	private SourceLocation sourceLocation;

	@Override
	public void generate(ClassVisitor cv, GameContext gameContext) {

	}
}
