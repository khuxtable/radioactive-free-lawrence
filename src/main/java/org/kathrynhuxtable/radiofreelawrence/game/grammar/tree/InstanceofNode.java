package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.GameData.IdentifierType;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InstanceofNode implements ExprNode {

	private IdentifierNode identifier;
	private IdentifierType identifierType;
	private SourceLocation sourceLocation;

	@Override
	public int evaluate(GameData gameData) {
		IdentifierType idType = gameData.getIdentifierType(identifier.getName());
		if (idType == IdentifierType.VARIABLE) {
			try {
				int refno = gameData.getIntIdentifierValue(identifier.getName());
				idType = gameData.getRefnoType(refno);
			} catch (NumberFormatException e) {
				// Fall through
			}
		}
		return identifierType == idType ? 1 : 0;
	}
}
