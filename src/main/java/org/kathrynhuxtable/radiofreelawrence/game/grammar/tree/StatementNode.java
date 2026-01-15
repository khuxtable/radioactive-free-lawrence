package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;

public interface StatementNode extends BaseNode {

	void execute(GameData gameData) throws GameRuntimeException;
}
