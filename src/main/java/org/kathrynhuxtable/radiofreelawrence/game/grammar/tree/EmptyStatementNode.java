package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.Data;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
public class EmptyStatementNode implements StatementNode {
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void execute(GameData gameData) throws GameRuntimeException {
		// Nothing to do here.
	}
}
