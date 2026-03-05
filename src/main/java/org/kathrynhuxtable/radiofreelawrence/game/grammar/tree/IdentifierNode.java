package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.Text;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext.VariableScope;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@AllArgsConstructor
public class IdentifierNode implements ExprNode {

	private String name;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		VariableContext variableContext = gameContext.variableStore.getVariable(name);
		if (variableContext == null) {
			throw new GameRuntimeException("undefined variable: " + name);
		} else if (variableContext.getVariableScope() == VariableScope.LOCAL) {
			if (variableContext.getVariableType() == VariableType.NUMBER) {
				mv.visitVarInsn(ILOAD, variableContext.getIndex());
			} else {
				mv.visitVarInsn(ALOAD, variableContext.getIndex());
			}
		} else {
			String className = GameContext.GAME_CLASS_NAME;
			if (variableContext.getVariableScope() == VariableScope.CLASS) {
				className = variableContext.getParentClass();
			}
			String type = switch (variableContext.getVariableType()) {
				case FLAG, STATE, NUMBER -> Type.INT_TYPE.getDescriptor();
				case TEXT -> Type.getDescriptor(String.class);
				case TEXT_NODE -> Type.getDescriptor(Text.class);
				case REFERENCE -> Type.getDescriptor(Object.class);
				case METHOD, LABEL -> null;
			};
			mv.visitVarInsn(ALOAD, 0); // inner class "this"
			if (gameContext.variableStore.getCurrentClass() != null && variableContext.getVariableScope() == VariableScope.GLOBAL) {
				// Need to reference instance variable in outer class
				mv.visitFieldInsn(
						GETFIELD,
						gameContext.variableStore.getCurrentClass(),
						"this$0", // outer class "this"
						GameContext.GAME_CLASS_DESCRIPTOR);
			}
			mv.visitFieldInsn(GETFIELD, className, variableContext.getName(), type);

			if (variableContext.getVariableType() == VariableType.TEXT_NODE) {
				mv.visitMethodInsn(
						INVOKEVIRTUAL,
						Type.getInternalName(Text.class),
						"getText",
						"()" + Type.getDescriptor(String.class),
						false);
			}
		}
	}
}
