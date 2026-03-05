package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

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
public class BasicForStatementNode implements StatementNode {
	private List<StatementNode> init;
	private ExprNode test;
	private List<StatementNode> update;
	private StatementNode statement;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		Label beginLabel = new Label();
		Label testLabel = new Label();
		Label updateLabel = new Label();
		Label endLabel = new Label();

		gameContext.variableStore.newLoopScope(label, updateLabel, endLabel);
		gameContext.variableStore.newBlockScope();

		mv.visitLabel(beginLabel);
		executeList(init, mv, gameContext);

		mv.visitLabel(testLabel);
		test.generate(mv, gameContext);
		mv.visitJumpInsn(IFEQ, endLabel);

		statement.generate(mv, gameContext);

		mv.visitLabel(updateLabel);
		executeList(update, mv, gameContext);
		mv.visitJumpInsn(GOTO, testLabel);
		mv.visitLabel(endLabel);

		gameContext.variableStore.closeBlockScope(mv, beginLabel, endLabel);
		gameContext.variableStore.closeLoopScope();
	}

	private void executeList(List<StatementNode> list, MethodVisitor mv, GameContext gameContext) {
		for (StatementNode statement : list) {
			statement.generate(mv, gameContext);
		}
	}
}
