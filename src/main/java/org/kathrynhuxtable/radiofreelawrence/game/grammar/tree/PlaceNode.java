package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;
import java.util.Map;

import lombok.*;
import org.objectweb.asm.*;

import org.kathrynhuxtable.radiofreelawrence.game.*;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceNode implements DeclaratorNode, VocabularyNode {
	private String name;
	@Singular
	private List<String> verbs;

	private String briefDescription;
	private String longDescription;
	private List<VariableNode> variables;
	private Map<String, VerbCommandNode> commands;
	private Map<String, ProcNode> procs;

	private SourceLocation sourceLocation;

	@Override
	public void generate(ClassVisitor cv, GameContext gameContext) {
		String innerClassInternalName = GameContext.GAME_CLASS_NAME + "$" + name;
		String innerClassDescriptor = "L" +  innerClassInternalName + ";";
		cv.visit(V17, ACC_PUBLIC, innerClassInternalName, "Ljava/lang/Object;Lorg/kathrynhuxtable/radiofreelawrence/game/GamePlace;Ljava/lang/Object;Lorg/kathrynhuxtable/radiofreelawrence/game/GameAction;Ljava/lang/Iterable<Ljava/lang/Object;>;", Type.getInternalName(Object.class),
				new String[] { Type.getInternalName(GamePlace.class), Type.getInternalName(GameAction.class), Type.getInternalName(Iterable.class) });
		cv.visitNestHost(GameContext.GAME_CLASS_NAME);
		cv.visitInnerClass(innerClassInternalName, GameContext.GAME_CLASS_NAME, name, ACC_PUBLIC);
		AsmUtils.createField(cv, ACC_FINAL | ACC_SYNTHETIC, "this$0", GameContext.GAME_CLASS_DESCRIPTOR);
		gameContext.variableStore.newClassScope(innerClassInternalName);

		gameContext.variableStore.addVariable("name", VariableType.TEXT);
		AsmUtils.createField(cv, ACC_PUBLIC, "name", Type.getDescriptor(String.class));
		gameContext.variableStore.addVariable("briefdescription", VariableType.TEXT);
		AsmUtils.createField(cv, ACC_PUBLIC, "briefdescription", Type.getDescriptor(String.class));
		gameContext.variableStore.addVariable("longdescription", VariableType.TEXT);
		AsmUtils.createField(cv, ACC_PUBLIC, "longdescription", Type.getDescriptor(String.class));

		AsmUtils.createField(cv, ACC_PUBLIC, "actions", "Ljava/util/List;",
				"Ljava/util/List<Ljava/lang/String;>;");
		AsmUtils.createField(cv, ACC_PUBLIC, "verbs", "Ljava/util/List;",
				"Ljava/util/List<Ljava/lang/String;>;");

		gameContext.variableStore.addVariable("flags", VariableType.NUMBER);
		AsmUtils.createField(cv, ACC_PUBLIC, "flags", Type.INT_TYPE.getDescriptor());

		for (VariableNode variableNode : variables) {
			variableNode.generate(cv, gameContext);
		}

		for (ProcNode procNode : procs.values()) {
			gameContext.variableStore.addVariable(procNode.getName(), VariableType.METHOD);
		}

		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getName", "name", Type.getDescriptor(String.class));
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getBriefDescription", "briefdescription", Type.getDescriptor(String.class));
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getLongDescription", "longdescription", Type.getDescriptor(String.class));
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getActions", "actions", Type.getDescriptor(List.class), "Ljava/util/List<Ljava/lang/String;>;");
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getVerbs", "verbs", Type.getDescriptor(List.class), "Ljava/util/List<Ljava/lang/String;>;");
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getFlags", "flags", Type.INT_TYPE.getDescriptor());
		AsmUtils.createSetter(cv, innerClassInternalName, ACC_PUBLIC, "setFlags", "flags", Type.INT_TYPE.getDescriptor());

		VerbCommandNode.generateActions(cv, gameContext, commands);

		for (ProcNode proc : procs.values()) {
			proc.generate(cv, gameContext);
		}

		generateIterator(cv, gameContext);

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

		AsmUtils.assignVariable(mv,
				innerClassInternalName,
				"name",
				Type.getDescriptor(String.class),
				name);

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

		AsmUtils.createList(mv, innerClassInternalName, "actions", commands.keySet());
		AsmUtils.createList(mv, innerClassInternalName, "verbs", verbs);

		for (VariableNode variableNode : variables) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTFIELD, innerClassInternalName, variableNode.getVariable(), "I");
		}

		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private void generateIterator(ClassVisitor cv, GameContext gameContext) {
		String innerClassInternalName = GameContext.GAME_CLASS_NAME + "$" + name;
		String innerClassDescriptor = "L" + GameContext.GAME_CLASS_NAME + "$" + name + ";";
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "iterator", "()Ljava/util/Iterator;", "()Ljava/util/Iterator<Ljava/lang/Object;>;", null);
		mv.visitCode();
		Label label0 = new Label();
		mv.visitLabel(label0);
		mv.visitLineNumber(35, label0);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, innerClassInternalName, "this$0", GameContext.GAME_CLASS_DESCRIPTOR);
		mv.visitFieldInsn(GETFIELD, GameContext.GAME_CLASS_NAME, "objects", "Ljava/util/Map;");
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(InternalFunctions.class), "iterator", "(Ljava/util/Map;Lorg/kathrynhuxtable/radiofreelawrence/game/GamePlace;)Ljava/util/Iterator;", false);
		mv.visitInsn(ARETURN);
		Label label1 = new Label();
		mv.visitLabel(label1);
		mv.visitLocalVariable("this", innerClassDescriptor, null, label0, label1, 0);
		mv.visitMaxs(2, 1);
		mv.visitEnd();
	}
}
