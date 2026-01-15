package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalVariableDeclarationStatementNode implements StatementNode {
	private List<VariableDeclaratorNode> declarators;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void execute(GameData gameData) throws GameRuntimeException {
		for (VariableDeclaratorNode variableDeclaratorNode : declarators) {
			String identifier = variableDeclaratorNode.getIdentifier().getName();

			int value = 0;
			if (variableDeclaratorNode.getExpression() != null) {
				value = variableDeclaratorNode.getExpression().evaluate(gameData);
			}

			gameData.localVariables.addVariable(identifier, value);
		}
	}
}
