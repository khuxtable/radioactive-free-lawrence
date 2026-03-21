package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableScope;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LValueNode implements ExprNode {

	private ExprNode expr;
	private SourceLocation sourceLocation;

	public void generate(MethodVisitor mv, GameContext gameContext) {
		if (expr instanceof IdentifierNode identifierNode) {
			VariableContext variableContext = gameContext.variableStore.getVariable(identifierNode.getName());
			if (variableContext == null) {
				throw new GameRuntimeException("Unknown variable: " + identifierNode.getName());
			} else if (variableContext.getVariableScope() == VariableScope.LOCAL) {
				mv.visitVarInsn(variableContext.getVariableType().reference ? ASTORE : ISTORE, variableContext.getIndex());
			} else if (variableContext.getVariableScope() == VariableScope.GLOBAL) {
				mv.visitVarInsn(ALOAD, 0); // inner class "this"
				if (gameContext.variableStore.getCurrentClass() != null) {
					// Need to reference instance variable in outer class
					mv.visitFieldInsn(
							GETFIELD,
							gameContext.variableStore.getCurrentClass(),
							"this$0", // outer class "this"
							GameContext.GAME_CLASS_DESCRIPTOR);
				}
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, GameContext.GAME_CLASS_NAME, variableContext.getName(), "I");
			} else if (variableContext.getVariableScope() == VariableScope.CLASS) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, variableContext.getParentClass(), variableContext.getName(), "I");
			}
		} else {
			throw new GameRuntimeException("unsupported unary operand type: " + expr.getClass());
		}
	}

	@Override
	public VariableType getVariableType(GameContext gameContext) {
		if (expr instanceof IdentifierNode identifierNode) {
			VariableContext variableContext = gameContext.variableStore.getVariable(identifierNode.getName());
			if (variableContext == null) {
				throw new GameRuntimeException("Unknown variable: " + identifierNode.getName());
			} else {
				return variableContext.getVariableType();
			}
		} else {
			throw new GameRuntimeException("unsupported unary operand type: " + expr.getClass());
		}
	}
}
