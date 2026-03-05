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
@AllArgsConstructor
@NoArgsConstructor
public class WhileStatementNode implements StatementNode {
	private boolean postTest;
	private ExprNode expression;
	private StatementNode statement;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		if (postTest) {
			Label topLabel = new Label();
			Label continueLabel = new Label();
			Label endLabel = new Label();
			gameContext.variableStore.newLoopScope(label, endLabel, continueLabel);
			mv.visitLabel(topLabel);
			statement.generate(mv, gameContext);
			mv.visitLabel(continueLabel);
			expression.generate(mv, gameContext);
			mv.visitJumpInsn(IFEQ, topLabel);
			mv.visitLabel(endLabel);
			gameContext.variableStore.closeLoopScope();
		} else {
			Label topLabel = new Label();
			Label endLabel = new Label();
			gameContext.variableStore.newLoopScope(label, endLabel, topLabel);
			mv.visitLabel(topLabel);
			expression.generate(mv, gameContext);
			mv.visitJumpInsn(IFEQ, endLabel);
			statement.generate(mv, gameContext);
			mv.visitJumpInsn(GOTO, topLabel);
			mv.visitLabel(endLabel);
			gameContext.variableStore.closeLoopScope();
		}
	}
}
