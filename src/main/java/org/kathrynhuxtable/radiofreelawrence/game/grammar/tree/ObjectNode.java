package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;
import java.util.Map;

import lombok.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import org.kathrynhuxtable.radiofreelawrence.game.*;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ObjectNode implements DeclaratorNode, HasRefno, VocabularyNode {
	private String name;
	@Singular
	private List<String> verbs;
	private boolean inVocabulary;

	private String inventoryDescription;
	private String briefDescription;
	private String longDescription;
	private List<VariableNode> variables;
	private Map<String, VerbCommandNode> commands;
	private Map<String, ProcNode> procs;

	private int refno;
	private SourceLocation sourceLocation;

	@Override
	public void generate(ClassVisitor cv, GameContext gameContext) {
		String innerClassInternalName = GameContext.GAME_CLASS_NAME + "$" + name;
		cv.visit(V17, ACC_PUBLIC, innerClassInternalName, null, Type.getInternalName(Object.class),
				new String[] { Type.getInternalName(GameObject.class), Type.getInternalName(GameAction.class) });
		cv.visitNestHost(GameContext.GAME_CLASS_NAME);
		cv.visitInnerClass(innerClassInternalName, GameContext.GAME_CLASS_NAME, name, ACC_PUBLIC);
		AsmUtils.createField(cv, ACC_FINAL | ACC_SYNTHETIC, "this$0", GameContext.GAME_CLASS_DESCRIPTOR);
		gameContext.variableStore.newClassScope(innerClassInternalName);

		gameContext.variableStore.addVariable("inventorydescription", VariableType.TEXT);
		AsmUtils.createField(cv, ACC_PUBLIC, "inventorydescription", Type.getDescriptor(String.class));
		gameContext.variableStore.addVariable("briefdescription", VariableType.TEXT);
		AsmUtils.createField(cv, ACC_PUBLIC, "briefdescription", Type.getDescriptor(String.class));
		gameContext.variableStore.addVariable("longdescription", VariableType.TEXT);
		AsmUtils.createField(cv, ACC_PUBLIC, "longdescription", Type.getDescriptor(String.class));
		gameContext.variableStore.addVariable("location", VariableType.PLACE);
		AsmUtils.createField(cv, ACC_PUBLIC, "location", Type.getDescriptor(GamePlace.class));

		for (VariableNode variableNode : variables) {
			variableNode.generate(cv, gameContext);
		}

		for (ProcNode procNode : procs.values()) {
			gameContext.variableStore.addVariable(procNode.getName(), VariableType.METHOD);
		}

		VerbCommandNode.generateActions(cv, gameContext, commands);

		for (ProcNode proc : procs.values()) {
			proc.generate(cv, gameContext);
		}

		generateConstructor(cv, gameContext);

		gameContext.variableStore.closeClassScope();
		cv.visitEnd();
	}

	private void generateConstructor(ClassVisitor cv, GameContext gameContext) {
		String innerClassInternalName = GameContext.GAME_CLASS_NAME + "$" + name;

		// standard constructor, accepting InternalFunctions parameter
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "(" + GameContext.GAME_CLASS_DESCRIPTOR + ")V", null, null);
		mv.visitParameter("this$0", ACC_FINAL | ACC_MANDATED);
		mv.visitCode();

		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitFieldInsn(PUTFIELD, innerClassInternalName, "this$0", GameContext.GAME_CLASS_DESCRIPTOR);

		mv.visitVarInsn(ALOAD, 0); //load the first local variable: this
		mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

		if (inventoryDescription != null) {
			AsmUtils.assignVariable(mv,
					innerClassInternalName,
					"inventorydescription",
					Type.getDescriptor(String.class),
					inventoryDescription);
		}
		if (briefDescription != null) {
			AsmUtils.assignVariable(mv,
					innerClassInternalName,
					"briefdescription",
					Type.getDescriptor(String.class),
					briefDescription);
		}
		if (longDescription != null || briefDescription != null) {
			AsmUtils.assignVariable(mv,
					innerClassInternalName,
					"longdescription",
					Type.getDescriptor(String.class),
					longDescription == null ? briefDescription : longDescription);
		}

		for (VariableNode variableNode : variables) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTFIELD, innerClassInternalName, variableNode.getVariable(), "I");
		}

		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}
}
