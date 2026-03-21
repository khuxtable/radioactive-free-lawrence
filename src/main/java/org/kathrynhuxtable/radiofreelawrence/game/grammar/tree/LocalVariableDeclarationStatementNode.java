package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

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

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LocalVariableDeclarationStatementNode implements StatementNode {
	private LocalVariableDeclarationNode localVariableDeclarationNode;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		boolean reference = localVariableDeclarationNode.isReference();
		for (VariableDeclaratorNode variableDeclaratorNode : localVariableDeclarationNode.getDeclarators()) {
			String identifier = variableDeclaratorNode.getIdentifier().getName();

			VariableContext variableContext = gameContext.variableStore.addVariable(identifier, reference ? VariableType.REFERENCE : VariableType.NUMBER);
			variableContext.setIndex(((LocalVariablesSorter) mv).newLocal(reference ? Type.getType(Object.class) : Type.INT_TYPE));

			if (variableDeclaratorNode.getExpression() != null) {
				variableDeclaratorNode.getExpression().generate(mv, gameContext);
			} else {
				mv.visitInsn(reference ? ACONST_NULL : ICONST_0);
			}

			mv.visitVarInsn(reference ? ASTORE : ISTORE, variableContext.getIndex());

		}
	}
}
