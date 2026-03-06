package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

@Data
@AllArgsConstructor
public class RefNode implements ExprNode {

	private IdentifierNode identifier;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
//		return gameData.getIdentifierRefno(identifier.getName());
	}

	@Override
	public VariableType getVariableType(GameContext gameContext) {
		return VariableType.REFERENCE;
	}
}
