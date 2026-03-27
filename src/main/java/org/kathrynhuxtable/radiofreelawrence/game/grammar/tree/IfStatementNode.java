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

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class IfStatementNode implements StatementNode {
	private List<ExprNode> expressions;
	private List<StatementNode> thenStatements;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		Label endLabel = new Label();

		for (int i = 0; i < expressions.size(); i++) {
			Label ifNotLabel = new Label();
			expressions.get(i).generate(mv, gameContext);
			mv.visitJumpInsn(IFEQ, ifNotLabel);
			thenStatements.get(i).generate(mv, gameContext);
			mv.visitJumpInsn(GOTO, endLabel);
			mv.visitLabel(ifNotLabel);
		}

		if (thenStatements.size() > expressions.size()) {
			thenStatements.get(expressions.size()).generate(mv, gameContext);
		}

		mv.visitLabel(endLabel);
	}
}
