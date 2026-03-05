package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.LocalVariablesSorter;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext.VariableType;

import static org.objectweb.asm.Opcodes.*;


@Data
public class ActionNode implements DeclaratorNode {
	private String arg1;
	private List<ActionCode> actionCodes = new ArrayList<>();
	private SourceLocation sourceLocation;

	public static String makeName(String arg1, String arg2) {
		StringBuilder sb = new StringBuilder("action");
		sb.append(arg1.substring(0, 1).toUpperCase());
		sb.append(arg1.substring(1));
		if (arg2 != null && !arg2.isEmpty()) {
			sb.append(arg2.substring(0, 1).toUpperCase());
			sb.append(arg2.substring(1));
		}
		return sb.toString();
	}

	@Override
	public void generate(ClassVisitor cw, GameContext gameContext) {
		for (ActionCode actionCode : actionCodes) {
			actionCode.generate(cw, gameContext);
		}
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ActionCode implements DeclaratorNode {
		private String arg2;
		private BlockNode code;
		private SourceLocation sourceLocation;
		private String name;

		@Override
		public void generate(ClassVisitor cv, GameContext gameContext) {
			gameContext.variableStore.addVariable(name, VariableType.METHOD);
			gameContext.variableStore.newFunctionScope();
			MethodVisitor mv2 = cv.visitMethod(ACC_PUBLIC, name, "()V", null, null);
			mv2.visitCode();
			LocalVariablesSorter mv = new LocalVariablesSorter(Opcodes.ACC_PUBLIC, "()V", mv2);
			Label startLabel = new Label();
			mv.visitLabel(startLabel);
			code.generate(mv, gameContext);
			Label endLabel = new Label();
			mv.visitLabel(endLabel);
			mv.visitInsn(RETURN);
			gameContext.variableStore.closeFunctionScope(mv, startLabel, endLabel);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		}
	}
}
