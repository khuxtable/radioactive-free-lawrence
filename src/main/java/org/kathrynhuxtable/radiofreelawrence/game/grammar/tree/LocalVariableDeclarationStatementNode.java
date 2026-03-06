package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ISTORE;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalVariableDeclarationStatementNode implements StatementNode {
	private List<VariableDeclaratorNode> declarators;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		for (VariableDeclaratorNode variableDeclaratorNode : declarators) {
			String identifier = variableDeclaratorNode.getIdentifier().getName();

			VariableContext variableContext = gameContext.variableStore.addVariable(identifier, VariableType.NUMBER);
			variableContext.setIndex(((LocalVariablesSorter) mv).newLocal(Type.INT_TYPE));

			if (variableDeclaratorNode.getExpression() != null) {
				variableDeclaratorNode.getExpression().generate(mv, gameContext);
			} else {
				mv.visitInsn(ICONST_0);
			}

			mv.visitVarInsn(ISTORE, variableContext.getIndex());

		}
	}
}
