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

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.RETURN;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerbCommandNode implements DeclaratorNode {

	private String verb;
	private BlockNode block;
	private SourceLocation sourceLocation;

	public static String makeName(String verb) {
		StringBuilder sb = new StringBuilder("action");
		sb.append(verb.substring(0, 1).toUpperCase());
		sb.append(verb.substring(1));
		return sb.toString();
	}

	@Override
	public void generate(MyClassVisitor cv, GameContext gameContext) {
		String name = makeName(verb);
		gameContext.variableStore.addVariable(name, VariableType.METHOD);
		gameContext.variableStore.newFunctionScope();
		MethodVisitor mv2 = cv.visitMethod(ACC_PUBLIC, name, "()V", null, null);
		mv2.visitCode();
		LocalVariablesSorter mv = new LocalVariablesSorter(Opcodes.ACC_PUBLIC, "()V", mv2);
		Label startLabel = new Label();
		mv.visitLabel(startLabel);
		block.generate(mv, gameContext);
		Label endLabel = new Label();
		mv.visitLabel(endLabel);
		mv.visitInsn(RETURN);
		gameContext.variableStore.closeFunctionScope(mv, startLabel, endLabel);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}
}
