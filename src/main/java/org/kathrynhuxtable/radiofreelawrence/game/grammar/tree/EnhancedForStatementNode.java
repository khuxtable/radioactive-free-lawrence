package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnhancedForStatementNode implements StatementNode {
	private IdentifierNode identifier;
	private ExprNode expression;
	private StatementNode statement;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		gameContext.variableStore.newBlockScope();
		Label beginLabel = new Label();
		Label endLabel = new Label();

		mv.visitLabel(beginLabel);
		VariableContext variableContext = gameContext.variableStore.addVariable(
				identifier.getName(),
				VariableType.REFERENCE);

		expression.generate(mv, gameContext);

		// Need to know the type being iterated over.
		variableContext.setIndex(((LocalVariablesSorter) mv).newLocal(Type.getObjectType("foo")));
//				NumberLiteralNode.builder()
//						.number(0)
//						.sourceLocation(sourceLocation)
//						.build());
		List<Integer> refnos = new ArrayList<>();
		// If exp is an object
//		if (expr >= gameData.fobj && expr < gameData.lobj) {
//			ObjectNode objectNode = gameData.objects[expr - gameData.fobj];
//			for (String verb : objectNode.getCommands().keySet()) {
//				if (gameData.gameNode.verbs.containsKey(verb)) {
//					VocabularyNode vocabularyNode = gameData.gameNode.verbs.get(verb);
//					refnos.add(((HasRefno) vocabularyNode).getRefno());
//				}
//			}
//			// If expr is a place
//		} else if (expr >= gameData.floc && expr < gameData.lloc) {
//			for (int obj = gameData.fobj; obj < gameData.lobj; obj++) {
//				if (gameData.locations[obj - gameData.fobj] == expr) {
//					refnos.add(obj);
//				}
//			}
//		}

//		for (Integer refno : refnos) {
//			try {
//				gameData.localVariables.setLocalVariableValue(identifier.getName(), new ReferenceLiteral(refno));
//				statement.execute(gameData);
//			} catch (BreakException e) {
//				if (e.ignore(label)) {
//					throw e;
//				} else {
//					break;
//				}
//			} catch (ContinueException e) {
//				if (e.ignore(label)) {
//					throw e;
//				}
//				// Implicit continue
//			}
//		}

		mv.visitLabel(endLabel);
		gameContext.variableStore.closeBlockScope(mv, beginLabel, endLabel);
	}
}
