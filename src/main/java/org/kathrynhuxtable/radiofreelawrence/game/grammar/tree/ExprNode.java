package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;

public interface ExprNode extends BaseNode {

	void generate(MethodVisitor mv, GameContext gameContext);
}
