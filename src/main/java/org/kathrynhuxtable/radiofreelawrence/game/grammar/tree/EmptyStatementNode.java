package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.Data;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
public class EmptyStatementNode implements StatementNode {
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		// Nothing to do here.
	}
}
