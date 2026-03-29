package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.Map;
import java.util.TreeMap;

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
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerbCommandNode implements StatementNode {

	private String verb;
	private BlockNode block;
	private SourceLocation sourceLocation;

	public static void generateActions(ClassVisitor cv, GameContext gameContext, Map<String, VerbCommandNode> actions) {
		gameContext.variableStore.addVariable("doAction", VariableType.METHOD);
		gameContext.variableStore.newFunctionScope();
		MethodVisitor mv2 = cv.visitMethod(ACC_PUBLIC, "doAction", "(Ljava/lang/String;)V", null, null);
		mv2.visitParameter("arg1", 0);
		mv2.visitCode();
		LocalVariablesSorter mv = new LocalVariablesSorter(Opcodes.ACC_PUBLIC, "(Ljava/lang/String;)V", mv2);
		Label startLabel = new Label();
		Label endLabel = new Label();
		mv.visitLabel(startLabel);
		VariableContext vc = gameContext.variableStore.addVariable("this", VariableType.REFERENCE);
		vc.setIndex(0);

		if (actions != null && !actions.isEmpty()) {
			Map<Integer, VerbCommandNode> hashToAction = new TreeMap<>();
			Map<Integer, Label> hashToLabel = new TreeMap<>();
			for (Map.Entry<String, VerbCommandNode> entry : actions.entrySet()) {
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

			for (Map.Entry<String, VerbCommandNode> entry : actions.entrySet()) {
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

	public static void generateMessages(ClassVisitor cv, GameContext gameContext, Map<String, MessageNode> messages) {
		gameContext.variableStore.addVariable("doMessage", VariableType.METHOD);
		gameContext.variableStore.newFunctionScope();
		MethodVisitor mv2 = cv.visitMethod(ACC_PUBLIC, "doMessage", "(Ljava/lang/String;I)I", null, null);
		mv2.visitParameter("name", 0);
		mv2.visitParameter("arg", 0);
		mv2.visitCode();
		LocalVariablesSorter mv = new LocalVariablesSorter(Opcodes.ACC_PUBLIC, "(Ljava/lang/String;I)I", mv2);
		Label startLabel = new Label();
		Label endLabel = new Label();
		mv.visitLabel(startLabel);
		VariableContext vc = gameContext.variableStore.addVariable("this", VariableType.REFERENCE);
		vc.setIndex(0);
		VariableContext variableContext = gameContext.variableStore.addVariable("name", VariableType.TEXT);
		variableContext.setIndex(1);
		variableContext = gameContext.variableStore.addVariable("arg", VariableType.NUMBER);
		variableContext.setIndex(2);

		if (messages != null && !messages.isEmpty()) {
			Map<Integer, MessageNode> hashToAction = new TreeMap<>();
			Map<Integer, Label> hashToLabel = new TreeMap<>();
			for (Map.Entry<String, MessageNode> entry : messages.entrySet()) {
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

			for (Map.Entry<String, MessageNode> entry : messages.entrySet()) {
				mv.visitLabel(hashToLabel.get(entry.getKey().hashCode()));
				//			mv.visitFrame(Opcodes.F_SAME, 0, null, 0, null);

				entry.getValue().generate(mv, gameContext);

				mv.visitJumpInsn(GOTO, endLabel);
			}
		}

		mv.visitLabel(endLabel);
		mv.visitInsn(ICONST_1);
		mv.visitInsn(IRETURN);
		gameContext.variableStore.closeFunctionScope(mv, startLabel, endLabel);
		mv.visitMaxs(0, 0);
		mv.visitEnd();
	}

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		gameContext.variableStore.newBlockScope();
		Label startLabel = new Label();
		mv.visitLabel(startLabel);
		block.generate(mv, gameContext);
		Label endLabel = new Label();
		mv.visitLabel(endLabel);
		gameContext.variableStore.closeBlockScope(mv, startLabel, endLabel);
	}
}
