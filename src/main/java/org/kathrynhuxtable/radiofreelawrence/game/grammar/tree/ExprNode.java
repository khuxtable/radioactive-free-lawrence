package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

public interface ExprNode extends BaseNode {

	void generate(MethodVisitor mv, GameContext gameContext);

	VariableType getVariableType(GameContext gameContext);
}
