package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IFEQ;

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
	public void generate(MethodVisitor mv, GameContext gameContext) {
		Label falseLabel = new Label();
		Label endLabel = new Label();

		expression.generate(mv, gameContext);
		mv.visitJumpInsn(IFEQ, falseLabel);
		trueExpression.generate(mv, gameContext);
		mv.visitJumpInsn(GOTO, endLabel);
		mv.visitLabel(falseLabel);
		falseExpression.generate(mv, gameContext);
		mv.visitLabel(endLabel);
	}
}
