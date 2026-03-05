package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

import static org.objectweb.asm.Opcodes.*;

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
	private LValueNode left;
	private ExprNode right;

	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		if (assignmentOperator == AssignmentOperator.EQUAL) {
			right.generate(mv, gameContext);
		} else {
			left.getExpr().generate(mv, gameContext);
			right.generate(mv, gameContext);

			switch (assignmentOperator) {
			case DIV_ASSIGN -> mv.visitInsn(IDIV);
			case MOD_ASSIGN -> mv.visitInsn(IREM);
			case ADD_ASSIGN -> mv.visitInsn(IADD);
			case SUB_ASSIGN -> mv.visitInsn(ISUB);
			case LSHIFT_ASSIGN -> mv.visitInsn(ISHL);
			case RSHIFT_ASSIGN -> mv.visitInsn(ISHR);
			case URSHIFT_ASSIGN -> mv.visitInsn(IUSHR);
			case AND_ASSIGN -> mv.visitInsn(IAND);
			case XOR_ASSIGN -> mv.visitInsn(IXOR);
			case OR_ASSIGN -> mv.visitInsn(IOR);
			}
		}

		mv.visitInsn(DUP);
		left.generate(mv, gameContext);
	}
}
