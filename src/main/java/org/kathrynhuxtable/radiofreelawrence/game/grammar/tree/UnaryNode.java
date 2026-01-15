package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

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
public class UnaryNode implements ExprNode {
	public enum UnaryOperator {
		MINUS, NOT, BITNOT, PREINC, PREDEC, POSTINC, POSTDEC
	}

	private UnaryOperator operator;
	private ExprNode expression;
	private SourceLocation sourceLocation;

	@Override
	public int evaluate(GameData gameData) {
		int value = expression.evaluate(gameData);

		switch (operator) {
		case MINUS:
			value = -value;
			break;
		case NOT:
			value = value == 0 ? 1 : 0;
			break;
		case BITNOT:
			value = ~value;
			break;
		case PREINC:
			gameData.setLeftHandSide(expression, ++value);
			break;
		case PREDEC:
			gameData.setLeftHandSide(expression, --value);
			break;
		case POSTINC:
			gameData.setLeftHandSide(expression, value + 1);
			break;
		case POSTDEC:
			gameData.setLeftHandSide(expression, value - 1);
			break;
		default:
			throw new GameRuntimeException("Invalid Unary Operator");
		}

		return value;
	}
}
