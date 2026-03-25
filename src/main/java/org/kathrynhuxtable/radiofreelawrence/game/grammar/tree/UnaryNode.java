package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Label;
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
public class UnaryNode implements ExprNode {
	public enum UnaryOperator {
		MINUS, NOT, BITNOT, PREINC, PREDEC, POSTINC, POSTDEC
	}

	private UnaryOperator operator;
	private ExprNode expression;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		expression.generate(mv, gameContext);

		switch (operator) {
		case MINUS -> mv.visitInsn(INEG);
		case NOT -> {
			Label zeroLabel = new Label();
			Label endLabel = new Label();
			mv.visitJumpInsn(IFEQ, zeroLabel);
			mv.visitInsn(ICONST_0);
			mv.visitJumpInsn(GOTO, endLabel);
			mv.visitLabel(zeroLabel);
			mv.visitInsn(ICONST_1);
			mv.visitLabel(endLabel);
		}
		case BITNOT -> {
			mv.visitInsn(ICONST_M1);
			mv.visitInsn(IXOR);
		}
		case PREINC -> {
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			mv.visitInsn(DUP);
			saveValue(mv, gameContext);
		}
		case PREDEC -> {
			mv.visitInsn(ICONST_1);
			mv.visitInsn(ISUB);
			mv.visitInsn(DUP);
			saveValue(mv, gameContext);
		}
		case POSTINC -> {
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(IADD);
			saveValue(mv, gameContext);
		}
		case POSTDEC -> {
			mv.visitInsn(DUP);
			mv.visitInsn(ICONST_1);
			mv.visitInsn(ISUB);
			saveValue(mv, gameContext);
		}
		}
	}

	private void saveValue(MethodVisitor mv, GameContext gameContext) {
		if (expression instanceof IdentifierNode identifierNode) {
			VariableContext variableContext = gameContext.variableStore.getVariable(identifierNode.getName());
			if (variableContext == null) {
				throw new GameRuntimeException("Unknown variable: " + identifierNode.getName());
			} else if (variableContext.getVariableScope() == VariableScope.LOCAL) {
				mv.visitVarInsn(ISTORE, variableContext.getIndex());
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
			} else {
				mv.visitVarInsn(ALOAD, 0); // inner class "this"
				mv.visitInsn(SWAP);
				mv.visitFieldInsn(PUTFIELD, variableContext.getParentClass(), variableContext.getName(), "I");
			}
		} else {
			throw new GameRuntimeException("unsupported unary operand type: " + expression.getClass());
		}
	}

	@Override
	public VariableType getVariableType(GameContext gameContext) {
		return VariableType.NUMBER;
	}
}
