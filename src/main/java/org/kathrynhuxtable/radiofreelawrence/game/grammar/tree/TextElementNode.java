package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import org.kathrynhuxtable.radiofreelawrence.game.AsmUtils;
import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TextElementNode implements DeclaratorNode, ExprNode, HasRefno {
	private String text;
	private int index;

	private int refno;
	private SourceLocation sourceLocation;

	@Override
	public void generate(ClassVisitor cv, GameContext gameContext) {
		String name = "textElement" + index;
		AsmUtils.createField(cv, ACC_PUBLIC | ACC_FINAL, name, Type.getDescriptor(String.class));
	}

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		String name = "textElement" + index;
		mv.visitVarInsn(ALOAD, 0); // inner class "this"
		if (gameContext.variableStore.getCurrentClass() != null) {
			// Need to reference instance variable in outer class
			mv.visitFieldInsn(
					GETFIELD,
					gameContext.variableStore.getCurrentClass(),
					"this$0", // outer class "this"
					GameContext.GAME_CLASS_DESCRIPTOR);
		}
		mv.visitFieldInsn(GETFIELD, GameContext.GAME_CLASS_NAME, name, Type.getDescriptor(String.class));
	}

	@Override
	public VariableType getVariableType(GameContext gameContext) {
		return VariableType.TEXT;
	}
}
