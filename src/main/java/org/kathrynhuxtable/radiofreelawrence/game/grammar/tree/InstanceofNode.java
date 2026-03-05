package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.IdentifierType;
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
	public void generate(MethodVisitor mv, GameContext gameContext) {
//		IdentifierType idType = gameData.getIdentifierType(identifier.getName());
//		if (idType == IdentifierType.VARIABLE) {
//			try {
//				int refno = gameData.getIntIdentifierValue(identifier.getName());
//				idType = gameData.getRefnoType(refno);
//			} catch (NumberFormatException e) {
//				// Fall through
//			}
//		}
//		return identifierType == idType ? 1 : 0;
	}
}
