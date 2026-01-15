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
public class BinaryNode implements ExprNode {
	public enum Operator {
		OR("||"),
		AND("&&"),
		BITOR("|"),
		XOR("^"),
		BITAND("&"),
		EQUALS("=="),
		NOTEQUALS("!="),
		LT("<"),
		GT(">"),
		LE("<="),
		GE(">="),
		LSHIFT("<<"),
		RSHIFT(">>"),
		URSHIFT(">>>"),
		ADD("+"),
		SUB("-"),
		MUL("*"),
		DIV("/"),
		MOD("%");

		private final String operator;

		Operator(String operator) {
			this.operator = operator;
		}

		public String operator() {
			return this.operator;
		}

		public static Operator findOperator(String operator) {
			for (Operator op : Operator.values()) {
				if (op.operator().equals(operator))
					return op;
			}
			return null;
		}
	}

	private Operator operator;
	private ExprNode left;
	private ExprNode right;

	private SourceLocation sourceLocation;

	@Override
	public int evaluate(GameData gameData) {
		return switch (operator) {
			case OR -> left.evaluate(gameData) != 0 || right.evaluate(gameData) != 0 ? 1 : 0;
			case AND -> left.evaluate(gameData) != 0 && right.evaluate(gameData) != 0 ? 1 : 0;
			case BITOR -> left.evaluate(gameData) | right.evaluate(gameData);
			case XOR -> left.evaluate(gameData) ^ right.evaluate(gameData);
			case BITAND -> left.evaluate(gameData) & right.evaluate(gameData);
			case EQUALS -> left.evaluate(gameData) == right.evaluate(gameData) ? 1 : 0;
			case NOTEQUALS -> left.evaluate(gameData) != right.evaluate(gameData) ? 1 : 0;
			case LT -> left.evaluate(gameData) < right.evaluate(gameData) ? 1 : 0;
			case GT -> left.evaluate(gameData) > right.evaluate(gameData) ? 1 : 0;
			case LE -> left.evaluate(gameData) <= right.evaluate(gameData) ? 1 : 0;
			case GE -> left.evaluate(gameData) >= right.evaluate(gameData) ? 1 : 0;
			case LSHIFT -> left.evaluate(gameData) << right.evaluate(gameData);
			case RSHIFT -> left.evaluate(gameData) >> right.evaluate(gameData);
			case URSHIFT -> left.evaluate(gameData) >>> right.evaluate(gameData);
			case ADD -> left.evaluate(gameData) + right.evaluate(gameData);
			case SUB -> left.evaluate(gameData) - right.evaluate(gameData);
			case MUL -> left.evaluate(gameData) * right.evaluate(gameData);
			case DIV -> left.evaluate(gameData) / right.evaluate(gameData);
			case MOD -> left.evaluate(gameData) % right.evaluate(gameData);
		};
	}
}
