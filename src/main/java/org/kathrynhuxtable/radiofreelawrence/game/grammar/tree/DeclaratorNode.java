package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.MyClassVisitor;

public interface DeclaratorNode extends BaseNode {
	void generate(MyClassVisitor cv, GameContext gameContext);
}
