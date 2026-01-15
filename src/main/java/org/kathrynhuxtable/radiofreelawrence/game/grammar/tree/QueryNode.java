package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QueryNode implements ExprNode {
	private ExprNode expression;
	private ExprNode trueExpression;
	private ExprNode falseExpression;
	private SourceLocation sourceLocation;

	@Override
	public int evaluate(GameData gameData) {
		return expression.evaluate(gameData) != 0 ? trueExpression.evaluate(gameData) : falseExpression.evaluate(gameData);
	}
}
