package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import org.objectweb.asm.ClassVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;

public interface DeclaratorNode extends BaseNode {
	void generate(ClassVisitor cv, GameContext gameContext);
}
