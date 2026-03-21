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
public class FlagStatement implements StatementNode {

	private boolean set;
	private FlagExpressionNode flagExpression;

	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		flagExpression.getFlagValue(mv, gameContext);
		flagExpression.getFlag().generate(mv, gameContext);

		if (set) {
			mv.visitInsn(IOR);
		} else {
			mv.visitInsn(ICONST_M1);
			mv.visitInsn(IXOR);
			mv.visitInsn(IAND);
		}

		flagExpression.setFlagValue(mv, gameContext);
	}
}
