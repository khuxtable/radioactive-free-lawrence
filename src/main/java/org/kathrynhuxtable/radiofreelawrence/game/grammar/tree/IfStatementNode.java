package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IfStatementNode implements StatementNode {
	private List<ExprNode> expressions;
	private List<StatementNode> thenStatements;
	private String label;

	@Override
	public void execute(GameData gameData) throws GameRuntimeException {
		for (int i = 0; i < expressions.size(); i++) {
			if (expressions.get(i).evaluate(gameData) != 0) {
				thenStatements.get(i).execute(gameData);
				return;
			}
		}
		// Handle final else block
		if (thenStatements.size() > expressions.size()) {
			thenStatements.get(expressions.size()).execute(gameData);
		}
	}
}
