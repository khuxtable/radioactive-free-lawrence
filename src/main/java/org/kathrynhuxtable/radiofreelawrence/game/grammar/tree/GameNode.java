package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.*;

import lombok.Data;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.InternalFunctions;
import org.kathrynhuxtable.radiofreelawrence.game.Text;
import org.kathrynhuxtable.radiofreelawrence.game.TextMethod;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

import static org.objectweb.asm.Opcodes.*;

@Data
public class GameNode implements BaseNode {
	private String name;
	private String version;
	private String date;
	private String author;
	private SourceLocation sourceLocation = new SourceLocation(null, 0, 0);

	Map<String, VocabularyNode> verbs = new LinkedHashMap<>();
	Map<String, BaseNode> identifiers = new LinkedHashMap<>();
	List<TextElementNode> textElements = new ArrayList<>();
	Map<String, Integer> textElementIndexes = new LinkedHashMap<>();
	List<String> noise = new ArrayList<>();
	List<VariableNode> variables = new ArrayList<>();
	List<FlagNode> flags = new ArrayList<>();
	List<ArrayNode> arrays = new ArrayList<>();
	List<ObjectNode> objects = new ArrayList<>(); // TODO Generate these
	List<PlaceNode> places = new ArrayList<>(); // TODO Generate these
	List<TextNode> texts = new ArrayList<>();
	Map<String, ActionNode> actions = new LinkedHashMap<>();
	Map<String, ProcNode> procs = new LinkedHashMap<>();
	List<InitialNode> inits = new ArrayList<>();
	List<RepeatNode> repeats = new ArrayList<>();
	Map<String, StateClauseNode> states = new LinkedHashMap<>();

	public void generate(ClassVisitor cv, GameContext gameContext) {
		cv.visit(V17, ACC_PUBLIC | ACC_SUPER, GameContext.GAME_CLASS_NAME, null, Type.getInternalName(Object.class), null);

		cv.visitField(ACC_PUBLIC, "internalFunctions", Type.getDescriptor(InternalFunctions.class), null, null).visitEnd();

		Set<Integer> seenTextElement =  new HashSet<>();
		for (TextElementNode textElement : textElements) {
			if (!seenTextElement.contains(textElement.getIndex())) {
				textElement.generate(cv, gameContext);
				seenTextElement.add(textElement.getIndex());
			}
		}

		for (TextNode textNode : texts) {
			textNode.generate(cv, gameContext);
		}

		for (FlagNode flagNode : flags) {
			flagNode.generate(cv, gameContext);
		}

		for (StateClauseNode stateClauseNode : states.values()) {
			stateClauseNode.generate(cv, gameContext);
		}

		for (VariableNode variableNode : variables) {
			variableNode.generate(cv, gameContext);
		}

		for (ProcNode proc : procs.values()) {
			proc.generate(cv, gameContext);
		}

		for (ActionNode action : actions.values()) {
			action.generate(cv, gameContext);
		}

		for (InitialNode init : inits) {
			init.generate(cv, gameContext);
		}

		for (RepeatNode repeat : repeats) {
			repeat.generate(cv, gameContext);
		}

		for (ObjectNode objectNode : objects) {
			cv.visitNestMember(GameContext.GAME_CLASS_NAME + "$" + objectNode.getName());
			cv.visitInnerClass(
					GameContext.GAME_CLASS_NAME + "$" + objectNode.getName(),
					GameContext.GAME_CLASS_NAME,
					objectNode.getName(),
					ACC_PUBLIC);
		}

		generateConstructor(cv, gameContext);

		cv.visitEnd();
	}

	private void generateConstructor(ClassVisitor cv, GameContext gameContext) {
		// standard constructor, accepting InternalFunctions parameter
		MethodVisitor mv = cv.visitMethod(ACC_PUBLIC, "<init>", "(" + Type.getDescriptor(InternalFunctions.class) + ")V", null, null);
		mv.visitCode();
		mv.visitVarInsn(ALOAD, 0); //load the first local variable: this
		mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(Object.class), "<init>", "()V", false);

		// declare internalFunctions variable
		mv.visitVarInsn(ALOAD, 0);
		mv.visitVarInsn(ALOAD, 1);
		mv.visitFieldInsn(PUTFIELD, GameContext.GAME_CLASS_NAME, "internalFunctions", Type.getDescriptor(InternalFunctions.class));

		generateTextElements(mv);
		generateTexts(mv);
		generateFlags(mv);
		generateStates(gameContext, mv);
		generateVariables(mv);

		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private void generateTextElements(MethodVisitor mv) {
		Set<Integer> seenTextElement =  new HashSet<>();
		for (TextElementNode textElementNode : textElements) {
			if (!seenTextElement.contains(textElementNode.getIndex())) {
				String name = "textElement" + textElementNode.getIndex();
				mv.visitVarInsn(ALOAD, 0);
				mv.visitLdcInsn(textElementNode.getText());
				mv.visitFieldInsn(PUTFIELD, GameContext.GAME_CLASS_NAME, name, Type.getDescriptor(String.class));
				seenTextElement.add(textElementNode.getIndex());
			}
		}
	}

	private void generateTexts(MethodVisitor mv) {
		for (TextNode textNode : texts) {
			mv.visitVarInsn(ALOAD, 0);

			mv.visitTypeInsn(NEW, Type.getInternalName(Text.class));
			mv.visitInsn(DUP);

			String enumInternalName = Type.getInternalName(TextMethod.class);
			String constantName = textNode.getMethod() == null ? TextMethod.ASSIGNED.name() : textNode.getMethod().name();
			String fieldDescriptor = Type.getDescriptor(TextMethod.class);
			mv.visitFieldInsn(GETSTATIC, enumInternalName, constantName, fieldDescriptor);

			mv.visitIntInsn(SIPUSH, textNode.getTextNodes().size());
			mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(String.class));
			for  (int index = 0; index < textNode.getTextNodes().size(); index++) {
				String name = "textElement" + textNode.getTextNodes().get(index).getIndex();
				mv.visitInsn(DUP);
				mv.visitIntInsn(SIPUSH, index);
				mv.visitVarInsn(ALOAD, 0);
				mv.visitFieldInsn(GETFIELD, GameContext.GAME_CLASS_NAME, name, Type.getDescriptor(String.class));
				mv.visitInsn(AASTORE);
			}

			mv.visitMethodInsn(
					INVOKESPECIAL,
					Type.getInternalName(Text.class),
					"<init>",
					"(" + Type.getDescriptor(TextMethod.class) + Type.getDescriptor(String[].class) + ")V",
					false);

			mv.visitFieldInsn(PUTFIELD, GameContext.GAME_CLASS_NAME, textNode.getName(), Type.getDescriptor(Text.class));;
		}
	}

	private void generateFlags(MethodVisitor mv) {
		for (FlagNode flagNode : flags) {
			int bitValue = 0;
			for (String flag : flagNode.getFlags()) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitLdcInsn(1<<bitValue++);
				mv.visitFieldInsn(PUTFIELD, GameContext.GAME_CLASS_NAME, flag, "I");
			}
		}
	}

	private void generateStates(GameContext gameContext, MethodVisitor mv) {
		int nextStateValue = 0;
		String previousState = null;
		for (StateClauseNode stateClauseNode : states.values()) {
			mv.visitVarInsn(ALOAD, 0);
			if (stateClauseNode.getValue() == null) {
				if (previousState != null) {
					mv.visitVarInsn(ALOAD, 0);
					mv.visitFieldInsn(GETFIELD, GameContext.GAME_CLASS_NAME, previousState, "I");
					mv.visitInsn(ICONST_1);
					mv.visitInsn(IADD);
				}else {
					mv.visitLdcInsn(nextStateValue++);
				}
			} else {
				stateClauseNode.getValue().generate(mv, gameContext);
			}
			mv.visitFieldInsn(PUTFIELD, GameContext.GAME_CLASS_NAME, stateClauseNode.getState(), "I");
			previousState = stateClauseNode.getState();
		}
	}

	private void generateVariables(MethodVisitor mv) {
		for (VariableNode variableNode : variables) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitInsn(ICONST_0);
			mv.visitFieldInsn(PUTFIELD, GameContext.GAME_CLASS_NAME, variableNode.getVariable(), "I");
		}
	}

	public int getTextElementIndex(String text) {
		int index;
		if (textElementIndexes.containsKey(text)) {
			index = textElementIndexes.get(text);
		} else {
			index = textElementIndexes.size();
			textElementIndexes.put(text, index);
		}
		return index;
	}
}
