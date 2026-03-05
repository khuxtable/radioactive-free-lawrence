package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

import static org.objectweb.asm.Opcodes.POP;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExpressionStatementNode implements StatementNode {
	private ExprNode expression;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		expression.generate(mv, gameContext);
		mv.visitInsn(POP);
	}
}
