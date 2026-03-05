package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

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
public class StatementExpressionListNode implements StatementNode {
	private List<StatementNode> statements;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		for (StatementNode statement : statements) {
			statement.generate(mv, gameContext);
		}
	}
}
