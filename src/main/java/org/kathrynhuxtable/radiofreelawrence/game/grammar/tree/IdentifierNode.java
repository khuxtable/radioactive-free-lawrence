package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@AllArgsConstructor
public class IdentifierNode implements ExprNode {
	private String name;
	private SourceLocation sourceLocation;

	@Override
	public int evaluate(GameData gameData) {
		return gameData.getIntIdentifierValue(name);
	}

	public void setIntValue(int value, GameData gameData) {
		gameData.setIntIdentifierValue(name, value);
	}
}
