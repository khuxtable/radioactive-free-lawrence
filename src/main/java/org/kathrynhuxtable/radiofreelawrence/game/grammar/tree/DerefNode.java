package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

@Data
@AllArgsConstructor
public class DerefNode implements ExprNode {

	private IdentifierNode identifier;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
//		int refno = gameData.getIntIdentifierValue(identifier.getName());
//		return gameData.getIntIdentifierValue(refno);
	}

	@Override
	public VariableType getVariableType(GameContext gameContext) {
		return VariableType.NUMBER;
	}
}
