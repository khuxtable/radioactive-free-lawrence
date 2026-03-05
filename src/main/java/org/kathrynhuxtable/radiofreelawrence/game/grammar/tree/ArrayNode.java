package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.ClassVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArrayNode implements DeclaratorNode {
	private String name;
	private int size;
	private SourceLocation sourceLocation;

	@Override
	public void generate(ClassVisitor cw, GameContext gameContext) {

	}
}
