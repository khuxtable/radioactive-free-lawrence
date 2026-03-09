package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.MyClassVisitor;
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
	public void generate(MyClassVisitor cw, GameContext gameContext) {

	}
}
