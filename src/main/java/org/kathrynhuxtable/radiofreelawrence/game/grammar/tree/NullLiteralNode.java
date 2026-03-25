package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.ACONST_NULL;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NullLiteralNode implements ExprNode {
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		mv.visitInsn(ACONST_NULL);
	}

	@Override
	public VariableType getVariableType(GameContext gameContext) {
		return VariableType.REFERENCE;
	}
}
