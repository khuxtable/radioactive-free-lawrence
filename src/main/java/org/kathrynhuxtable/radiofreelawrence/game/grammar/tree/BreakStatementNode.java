package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.exception.BreakException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.LoopControlException.ControlType;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakStatementNode implements StatementNode {
	private String identifier;
	private ControlType controlType;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void execute(GameData gameData) throws GameRuntimeException {
		if (controlType != ControlType.CODE) {
			throw new BreakException(controlType);
		} else {
			throw new BreakException(identifier);
		}
	}
}
