package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.*;

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
public class ActionNode implements StatementNode {
	private String arg1;
	private List<ActionCode> actionCodes = new ArrayList<>();
	private SourceLocation sourceLocation;

	public static void generateActions(MyClassVisitor cv, GameContext gameContext, Map<String, ActionNode> actions) {
		gameContext.variableStore.addVariable("doAction", VariableType.METHOD);
		gameContext.variableStore.newFunctionScope();
		MethodVisitor mv2 = cv.visitMethod(ACC_PUBLIC, "doAction", "(Ljava/lang/String;Ljava/lang/String;)V", null, null);
		mv2.visitParameter("arg1", 1);
		mv2.visitParameter("arg2", 2);
		mv2.visitCode();
		LocalVariablesSorter mv = new LocalVariablesSorter(Opcodes.ACC_PUBLIC, "(Ljava/lang/String;Ljava/lang/String;)V", mv2);
		Label startLabel = new Label();
		Label endLabel = new Label();
		mv.visitLabel(startLabel);

		if (actions != null && !actions.isEmpty()) {
			Map<Integer, ActionNode> hashToAction = new TreeMap<>();
			Map<Integer, Label> hashToLabel = new TreeMap<>();
			for (Map.Entry<String, ActionNode> entry : actions.entrySet()) {
				int hashCode = entry.getKey().hashCode();
				hashToAction.put(hashCode, entry.getValue());
				hashToLabel.put(hashCode, new Label());
			}

			mv.visitVarInsn(ALOAD, 1);
			mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
			mv.visitLookupSwitchInsn(
					endLabel,
					hashToAction.keySet().stream()
							.mapToInt(Integer::intValue)
							.toArray(),
					hashToLabel.values().toArray(new Label[0]));

			for (Map.Entry<String, ActionNode> entry : actions.entrySet()) {
				mv.visitLabel(hashToLabel.get(entry.getKey().hashCode()));
	//			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

				entry.getValue().generate(mv, gameContext);

				mv.visitJumpInsn(GOTO, endLabel);
			}
		}

		mv.visitLabel(endLabel);
		mv.visitInsn(RETURN);
		gameContext.variableStore.closeFunctionScope(mv, startLabel, endLabel);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		Map<Integer, ActionCode> hashToAction = new TreeMap<>();
		Map<Integer, Label> hashToLabel = new TreeMap<>();
		for (ActionCode actionCode : actionCodes) {
			int hashCode = actionCode.arg2 == null ? 0 : actionCode.arg2.hashCode();
			hashToAction.put(hashCode, actionCode);
			hashToLabel.put(hashCode, new Label());
		}

		Label endLabel = new Label();

		mv.visitVarInsn(ALOAD, 2);
		mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "hashCode", "()I", false);
		mv.visitLookupSwitchInsn(
				endLabel,
				hashToAction.keySet().stream()
						.mapToInt(Integer::intValue)
						.toArray(),
				hashToLabel.values().toArray(new Label[0]));

		for (ActionCode actionCode : actionCodes) {
			mv.visitLabel(hashToLabel.get(actionCode.arg2 == null ? 0 : actionCode.arg2.hashCode()));
//			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

			actionCode.generate(mv, gameContext);

			mv.visitJumpInsn(GOTO, endLabel);
		}

		mv.visitLabel(endLabel);
	}

	@Data
	@Builder
	@AllArgsConstructor
	@NoArgsConstructor
	public static class ActionCode implements StatementNode {
		private String arg2;
		private BlockNode code;
		private SourceLocation sourceLocation;

		@Override
		public void generate(MethodVisitor mv, GameContext gameContext) {
			gameContext.variableStore.newBlockScope();
			Label startLabel = new Label();
			mv.visitLabel(startLabel);

			code.generate(mv, gameContext);

			Label endLabel = new Label();
			mv.visitLabel(endLabel);
			gameContext.variableStore.closeBlockScope(mv, startLabel, endLabel);
		}
	}
}
