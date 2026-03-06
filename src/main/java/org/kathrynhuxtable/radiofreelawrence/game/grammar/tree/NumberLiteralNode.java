package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NumberLiteralNode implements ExprNode {
	private int number;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		mv.visitLdcInsn(number);
	}

	@Override
	public VariableType getVariableType(GameContext gameContext) {
		return VariableType.NUMBER;
	}
}
