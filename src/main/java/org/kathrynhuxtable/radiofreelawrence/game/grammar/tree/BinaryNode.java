package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BinaryNode implements ExprNode {
	public enum Operator {
		OR("||", IFNE),
		AND("&&", IFEQ),
		BITOR("|", IOR),
		XOR("^", IXOR),
		BITAND("&", IAND),
		EQUALS("==", IF_ICMPEQ),
		NOTEQUALS("!=", IF_ICMPNE),
		LT("<", IF_ICMPLT),
		GT(">", IF_ICMPGT),
		LE("<=", IF_ICMPLE),
		GE(">=", IF_ICMPGE),
		LSHIFT("<<", ISHL),
		RSHIFT(">>", ISHR),
		URSHIFT(">>>", IUSHR),
		ADD("+", IADD),
		SUB("-", ISUB),
		MUL("*", IMUL),
		DIV("/", IDIV),
		MOD("%", IREM);

		private final String operator;
		private final int asmOpCode;

		Operator(String operator, int asmOpCode) {
			this.operator = operator;
			this.asmOpCode = asmOpCode;
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
	public void generate(MethodVisitor mv, GameContext gameContext) {
		switch (operator) {
		case BITOR, XOR, BITAND, LSHIFT, RSHIFT, URSHIFT, ADD, SUB, MUL, DIV, MOD -> simpleOperation(mv, gameContext);
		case OR, AND -> shortCircuitOperation(mv, gameContext);
		case EQUALS, NOTEQUALS, LT, GT, LE, GE -> comparisonOperation(mv, gameContext);
		}
	}

	private void simpleOperation(MethodVisitor mv, GameContext gameContext) {
		left.generate(mv, gameContext);
		right.generate(mv, gameContext);

		mv.visitInsn(operator.asmOpCode);
	}

	private void shortCircuitOperation(MethodVisitor mv, GameContext gameContext) {
		Label altLabel = new Label();
		Label endLabel = new Label();

		left.generate(mv, gameContext);
		mv.visitJumpInsn(operator.asmOpCode, altLabel);
		right.generate(mv, gameContext);
		mv.visitJumpInsn(operator.asmOpCode, altLabel);
		mv.visitInsn(operator == Operator.OR ? ICONST_0 : ICONST_1);
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(altLabel);
		mv.visitInsn(operator == Operator.OR ? ICONST_1 : ICONST_0);
		mv.visitLabel(endLabel);
	}

	private void comparisonOperation(MethodVisitor mv, GameContext gameContext) {
		left.generate(mv, gameContext);
		right.generate(mv, gameContext);

		Label thenLabel = new Label();
		Label endLabel = new Label();

		mv.visitJumpInsn(operator.asmOpCode, thenLabel);

		// False is the fall through condition
		mv.visitInsn(ICONST_0);
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(thenLabel);
		mv.visitInsn(ICONST_1);
		mv.visitLabel(endLabel);
	}
}
