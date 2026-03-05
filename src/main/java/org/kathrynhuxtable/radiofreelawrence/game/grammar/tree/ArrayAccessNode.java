package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArrayAccessNode implements ExprNode {
	private String arrayName;
	private ExprNode index;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor cw, GameContext gameContext) {

	}
}
