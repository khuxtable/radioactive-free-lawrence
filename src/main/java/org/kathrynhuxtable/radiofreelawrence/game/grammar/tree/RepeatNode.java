package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.LocalVariablesSorter;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.MyClassVisitor;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RepeatNode implements DeclaratorNode {
	private int index;
	private BlockNode code;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MyClassVisitor cv, GameContext gameContext) {
		String name = "repeatProc" + index;
		gameContext.variableStore.addVariable(name, VariableType.METHOD);
		gameContext.variableStore.newFunctionScope();
		MethodVisitor mv2 = cv.visitMethod(ACC_PUBLIC, name, "()I", null, null);
		mv2.visitCode();
		LocalVariablesSorter mv = new LocalVariablesSorter(Opcodes.ACC_PUBLIC, "()I", mv2);
		Label startLabel = new Label();
		mv.visitLabel(startLabel);
		code.generate(mv, gameContext);
		Label endLabel = new Label();
		mv.visitLabel(endLabel);
		mv.visitInsn(ICONST_0);
		mv.visitInsn(IRETURN);
		gameContext.variableStore.closeFunctionScope(mv, startLabel, endLabel);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}
}
