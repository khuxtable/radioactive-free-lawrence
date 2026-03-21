package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.GameFlag;
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
public class FlagExpressionNode implements ExprNode {

	private IdentifierNode identifierNode;
	private ExprNode flag;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		getFlagValue(mv, gameContext);

		flag.generate(mv, gameContext);
		mv.visitInsn(IAND);
	}

	public void getFlagValue(MethodVisitor mv, GameContext gameContext) {
		VariableContext variableContext = gameContext.getVariableStore().getVariable(identifierNode.getName());
		if (variableContext == null) {
			throw new GameRuntimeException("Unknown identifier: " + identifierNode.getName());
		}
		VariableType variableType = variableContext.getVariableType();
		if (variableType == VariableType.PLACE || variableType == VariableType.OBJECT || variableType == VariableType.REFERENCE) {
			identifierNode.generate(mv, gameContext);
			mv.visitMethodInsn(
					INVOKEINTERFACE,
					Type.getInternalName(GameFlag.class),
					"getFlags",
					"()I",
					true);
		} else if (variableContext.getVariableScope() == VariableScope.GLOBAL) {
			mv.visitVarInsn(ALOAD, 0);
			if (gameContext.variableStore.getCurrentClass() != null) {
				// Need to reference instance variable in outer class
				mv.visitFieldInsn(
						GETFIELD,
						gameContext.variableStore.getCurrentClass(),
						"this$0", // outer class "this"
						GameContext.GAME_CLASS_DESCRIPTOR);
			}
			mv.visitFieldInsn(GETFIELD, GameContext.GAME_CLASS_NAME, "variableFlags", Type.getDescriptor(Map.class));
			mv.visitLdcInsn(variableContext.getName());
			mv.visitMethodInsn(
					INVOKEINTERFACE,
					Type.getInternalName(Map.class),
					"get",
					"(" + Type.getDescriptor(Object.class) + ")" + Type.getDescriptor(Object.class),
					true);
			mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
		} else {
			throw new GameRuntimeException("Invalid variable type for flag reference: " + variableType + " for " + identifierNode.getName());
		}
	}

	public void setFlagValue(MethodVisitor mv, GameContext gameContext) {
		VariableContext variableContext = gameContext.getVariableStore().getVariable(identifierNode.getName());
		if (variableContext == null) {
			throw new GameRuntimeException("Unknown identifier: " + identifierNode.getName());
		}
		VariableType variableType = variableContext.getVariableType();
		if (variableType == VariableType.PLACE || variableType == VariableType.OBJECT || variableType == VariableType.REFERENCE) {
			identifierNode.generate(mv, gameContext);
			mv.visitInsn(SWAP);
			mv.visitMethodInsn(
					INVOKEINTERFACE,
					Type.getInternalName(GameFlag.class),
					"setFlags",
					"(I)V",
					true);
		} else if (variableContext.getVariableScope() == VariableScope.GLOBAL) {
			mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
			mv.visitVarInsn(ALOAD, 0);
			if (gameContext.variableStore.getCurrentClass() != null) {
				// Need to reference instance variable in outer class
				mv.visitFieldInsn(
						GETFIELD,
						gameContext.variableStore.getCurrentClass(),
						"this$0", // outer class "this"
						GameContext.GAME_CLASS_DESCRIPTOR);
			}
			mv.visitFieldInsn(GETFIELD, GameContext.GAME_CLASS_NAME, "variableFlags", Type.getDescriptor(Map.class));
			mv.visitInsn(SWAP);
			mv.visitLdcInsn(variableContext.getName());
			mv.visitInsn(SWAP);
			mv.visitMethodInsn(
					INVOKEINTERFACE,
					"java/util/Map",
					"put",
					"(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;",
					true);
			mv.visitInsn(POP);
		} else {
			throw new GameRuntimeException("Invalid variable type for flag reference: " + variableType + " for " + identifierNode.getName());
		}
	}

	@Override
	public VariableType getVariableType(GameContext gameContext) {
		return VariableType.NUMBER;
	}
}
