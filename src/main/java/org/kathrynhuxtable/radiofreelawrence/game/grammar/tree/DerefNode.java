package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@AllArgsConstructor
public class DerefNode implements ExprNode {

	private IdentifierNode identifier;
	private SourceLocation sourceLocation;

	@Override
	public int evaluate(GameData gameData) {
		int refno = gameData.getIntIdentifierValue(identifier.getName());
		return gameData.getIntIdentifierValue(refno);
	}
}
