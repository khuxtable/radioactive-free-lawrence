package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lombok.*;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
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
public class ObjectNode implements DeclaratorNode, VocabularyNode {
	private String name;
	@Singular
	private List<String> verbs;

	private String inventoryDescription;
	private String briefDescription;
	private String longDescription;
	private List<VariableNode> variables;
	private List<InitialNode> inits = new ArrayList<>();
	private Map<String, VerbCommandNode> commands;
	private Map<String, ProcNode> procs;
	private Map<String, MessageNode> messages;

	private SourceLocation sourceLocation;

	@Override
	public void generate(ClassVisitor cv, GameContext gameContext) {
		String innerClassInternalName = GameContext.GAME_CLASS_NAME + "$" + name;
		String innerClassDescriptor = "L" + GameContext.GAME_CLASS_NAME + "$" + name + ";";
		cv.visit(V17, ACC_PUBLIC, innerClassInternalName, null, Type.getInternalName(Object.class),
				new String[] { Type.getInternalName(GameObject.class), Type.getInternalName(GameAction.class), Type.getInternalName(Iterable.class) });
		cv.visitNestHost(GameContext.GAME_CLASS_NAME);
		cv.visitInnerClass(innerClassInternalName, GameContext.GAME_CLASS_NAME, name, ACC_PUBLIC);
		AsmUtils.createField(cv, ACC_FINAL | ACC_SYNTHETIC, "this$0", GameContext.GAME_CLASS_DESCRIPTOR);
		gameContext.variableStore.newClassScope(innerClassInternalName);

		gameContext.variableStore.addVariable("name", VariableType.TEXT);
		AsmUtils.createField(cv, ACC_PUBLIC, "name", Type.getDescriptor(String.class));
		gameContext.variableStore.addVariable("inventorydescription", VariableType.TEXT);
		AsmUtils.createField(cv, ACC_PUBLIC, "inventorydescription", Type.getDescriptor(String.class));
		gameContext.variableStore.addVariable("briefdescription", VariableType.TEXT);
		AsmUtils.createField(cv, ACC_PUBLIC, "briefdescription", Type.getDescriptor(String.class));
		gameContext.variableStore.addVariable("longdescription", VariableType.TEXT);
		AsmUtils.createField(cv, ACC_PUBLIC, "longdescription", Type.getDescriptor(String.class));
		gameContext.variableStore.addVariable("location", VariableType.PLACE);
		AsmUtils.createField(cv, ACC_PUBLIC, "location", Type.getDescriptor(GamePlace.class));

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

		VerbCommandNode.generateActions(cv, gameContext, commands);
		VerbCommandNode.generateMessages(cv, gameContext, messages);

		for (ProcNode proc : procs.values()) {
			proc.generate(cv, gameContext);
		}

		for (InitialNode init : inits) {
			init.generate(cv, gameContext);
		}

		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getName", "name", Type.getDescriptor(String.class));
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getBriefDescription", "briefdescription", Type.getDescriptor(String.class));
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getLongDescription", "longdescription", Type.getDescriptor(String.class));
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getInventoryDescription", "inventorydescription", Type.getDescriptor(String.class));
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getActions", "actions", Type.getDescriptor(List.class), "Ljava/util/List<Ljava/lang/String;>;");
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getVerbs", "verbs", Type.getDescriptor(List.class), "Ljava/util/List<Ljava/lang/String;>;");
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getLocation", "location", Type.getDescriptor(GamePlace.class));
		AsmUtils.createSetter(cv, innerClassInternalName, ACC_PUBLIC, "setLocation", "location", Type.getDescriptor(GamePlace.class));
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "getFlags", "flags", Type.INT_TYPE.getDescriptor());
		AsmUtils.createSetter(cv, innerClassInternalName, ACC_PUBLIC, "setFlags", "flags", Type.INT_TYPE.getDescriptor());
		AsmUtils.createGetter(cv, innerClassInternalName, ACC_PUBLIC, "toString", "name", Type.getDescriptor(String.class));

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

		AsmUtils.createList(mv, innerClassInternalName, "actions", commands.keySet());
		AsmUtils.createList(mv, innerClassInternalName, "verbs", verbs);

		for (VariableNode variableNode : variables) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTFIELD, innerClassInternalName, variableNode.getVariable(), "I");
		}

		for (InitialNode init : inits) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(INVOKEVIRTUAL,
					innerClassInternalName,
					"initialProc" + init.index,
					"()I",
					false);
			mv.visitInsn(POP);
		}

		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private void generateIterator(ClassVisitor cv, GameContext gameContext) {
		String innerClassInternalName = GameContext.GAME_CLASS_NAME + "$" + name;
		String innerClassDescriptor = "L" + innerClassInternalName + ";";
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "iterator", "()Ljava/util/Iterator;", "()Ljava/util/Iterator<Ljava/lang/Object;>;", null);
		mv.visitCode();
		Label beginLabel = new Label();
		mv.visitLabel(beginLabel);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(INVOKESTATIC, Type.getInternalName(InternalFunctions.class), "iterator", "(Lorg/kathrynhuxtable/radiofreelawrence/game/GameObject;)Ljava/util/Iterator;", false);
		mv.visitInsn(ARETURN);
		Label endLabel = new Label();
		mv.visitLabel(endLabel);
		mv.visitLocalVariable("this", innerClassDescriptor, null, beginLabel, endLabel, 0);
		mv.visitMaxs(2, 1);
		mv.visitEnd();
	}
}
