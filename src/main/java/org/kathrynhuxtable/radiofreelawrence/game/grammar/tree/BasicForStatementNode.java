package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

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
public class BasicForStatementNode implements StatementNode {
	private List<StatementNode> init;
	private ExprNode test;
	private List<StatementNode> update;
	private StatementNode statement;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void execute(GameData gameData) throws GameRuntimeException {
		gameData.localVariables.newBlockScope();
		try {
			for (executeList(init, gameData); test.evaluate(gameData) != 0; executeList(update, gameData)) {
				try {
					statement.execute(gameData);
				} catch (BreakException e) {
					if (e.ignore(label)) {
						throw e;
					} else {
						break;
					}
				} catch (ContinueException e) {
					if (e.ignore(label)) {
						throw e;
					}
					// Implicit continue
				}
			}
		} finally {
			gameData.localVariables.closeBlockScope();
		}
	}

	private void executeList(List<StatementNode> list, GameData gameData) throws GameRuntimeException {
		for (StatementNode statement : list) {
			statement.execute(gameData);
		}
	}
}
