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
public class AssignmentNode implements ExprNode {
	public enum AssignmentOperator {
		EQUAL("="),
		MUL_ASSIGN("*="),
		DIV_ASSIGN("/="),
		MOD_ASSIGN("%="),
		ADD_ASSIGN("+="),
		SUB_ASSIGN("-="),
		LSHIFT_ASSIGN("<<="),
		RSHIFT_ASSIGN(">>="),
		URSHIFT_ASSIGN(">>="),
		AND_ASSIGN("&="),
		XOR_ASSIGN("^="),
		OR_ASSIGN("|=");

		private final String operator;

		AssignmentOperator(String operator) {
			this.operator = operator;
		}

		public String operator() {
			return this.operator;
		}

		public static AssignmentOperator findOperator(String operator) {
			for (AssignmentOperator op : AssignmentOperator.values()) {
				if (op.operator().equals(operator))
					return op;
			}
			return null;
		}
	}

	private AssignmentOperator assignmentOperator;
	private ExprNode left;
	private ExprNode right;

	private SourceLocation sourceLocation;

	@Override
	public int evaluate(GameData gameData) {
		int value = right.evaluate(gameData);

		switch (assignmentOperator) {
		case EQUAL:
			gameData.setLeftHandSide(left, value);
			break;
		case MUL_ASSIGN:
			gameData.setLeftHandSide(left, left.evaluate(gameData) * value);
			break;
		case DIV_ASSIGN:
			gameData.setLeftHandSide(left, left.evaluate(gameData) / value);
			break;
		case MOD_ASSIGN:
			gameData.setLeftHandSide(left, left.evaluate(gameData) % value);
			break;
		case ADD_ASSIGN:
			gameData.setLeftHandSide(left, left.evaluate(gameData) + value);
			break;
		case SUB_ASSIGN:
			gameData.setLeftHandSide(left, left.evaluate(gameData) - value);
			break;
		case LSHIFT_ASSIGN:
			gameData.setLeftHandSide(left, left.evaluate(gameData) << value);
			break;
		case RSHIFT_ASSIGN:
			gameData.setLeftHandSide(left, left.evaluate(gameData) >> value);
			break;
		case URSHIFT_ASSIGN:
			gameData.setLeftHandSide(left, left.evaluate(gameData) >>> value);
			break;
		case AND_ASSIGN:
			gameData.setLeftHandSide(left, left.evaluate(gameData) & value);
			break;
		case XOR_ASSIGN:
			gameData.setLeftHandSide(left, left.evaluate(gameData) ^ value);
			break;
		case OR_ASSIGN:
			gameData.setLeftHandSide(left, left.evaluate(gameData) | value);
			break;
		}

		return value;
	}
}
