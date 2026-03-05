package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.exception.BreakException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.ControlType;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreakStatementNode implements StatementNode {
	private String identifier;
	private ControlType controlType;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		if (controlType != ControlType.CODE) {
//			Generate: throw new BreakException(controlType);
			throwException(mv, Type.getInternalName(BreakException.class));
		} else {
			mv.visitJumpInsn(GOTO, gameContext.getVariableStore().getBreakLabel(identifier));
		}
	}

	private void throwException(MethodVisitor mv, String exceptionTypeName) {
		String controlTypeName = Type.getInternalName(ControlType.class);
		String controlTypeDescriptor = Type.getDescriptor(ControlType.class);

		mv.visitTypeInsn(NEW, exceptionTypeName);
		mv.visitInsn(DUP);
		mv.visitLdcInsn("Invalid control type. Should not happen");
		mv.visitFieldInsn(GETSTATIC, controlTypeName, controlType.name(), controlTypeDescriptor);
		mv.visitMethodInsn(
				INVOKESPECIAL,
				exceptionTypeName,
				"<init>",
				"(" + controlTypeDescriptor + ")V",
				false);
		mv.visitInsn(ATHROW);
	}
}
