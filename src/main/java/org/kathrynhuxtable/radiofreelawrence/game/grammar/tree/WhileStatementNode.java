package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.exception.BreakException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.ContinueException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WhileStatementNode implements StatementNode {
	private boolean postTest;
	private ExprNode expression;
	private StatementNode statement;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void execute(GameData gameData) throws GameRuntimeException {
		if (postTest) {
			do {
				if (executeStatement(gameData)) break;
				// Note that this is repeat until, not do while so test is reversed.
			} while (expression.evaluate(gameData) == 0);
		} else {
			while (expression.evaluate(gameData) != 0) {
				if (executeStatement(gameData)) break;
			}
		}
	}

	private boolean executeStatement(GameData gameData) {
		try {
			statement.execute(gameData);
		} catch (BreakException e) {
			if (e.ignore(label)) {
				throw e;
			} else {
				return true;
			}
		} catch (ContinueException e) {
			if (e.ignore(label)) {
				throw e;
			}
			// Implicit continue
		}
		return false;
	}
}
