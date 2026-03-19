package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.*;

import lombok.Data;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import org.kathrynhuxtable.radiofreelawrence.game.*;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

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
	List<FlagNode> variableFlags = new ArrayList<>();
	List<FlagNode> placeFlags = new ArrayList<>();
	List<FlagNode> objectFlags = new ArrayList<>();
	List<ArrayNode> arrays = new ArrayList<>();
	List<ObjectNode> objects = new ArrayList<>();
	List<PlaceNode> places = new ArrayList<>(); // TODO Generate these
	List<TextNode> texts = new ArrayList<>();
	Map<String, ActionNode> actions = new LinkedHashMap<>();
	Map<String, ProcNode> procs = new LinkedHashMap<>();
	List<InitialNode> inits = new ArrayList<>();
	List<RepeatNode> repeats = new ArrayList<>();
	Map<String, StateClauseNode> states = new LinkedHashMap<>();

	public void generate(ClassVisitor cv, GameContext gameContext) {
		cv.visit(V17, ACC_PUBLIC | ACC_SUPER, GameContext.GAME_CLASS_NAME, null, Type.getInternalName(Object.class), null);

		AsmUtils.createField(cv, ACC_PUBLIC, "internalFunctions", Type.getDescriptor(InternalFunctions.class));

		Set<Integer> seenTextElement = new HashSet<>();
		for (TextElementNode textElement : textElements) {
			if (!seenTextElement.contains(textElement.getIndex())) {
				textElement.generate(cv, gameContext);
				seenTextElement.add(textElement.getIndex());
			}
		}

		for (TextNode textNode : texts) {
			textNode.generate(cv, gameContext);
		}

		for (FlagNode flagNode : variableFlags) {
			flagNode.generate(cv, gameContext);
		}
		for (FlagNode flagNode : placeFlags) {
			flagNode.generate(cv, gameContext);
		}
		for (FlagNode flagNode : objectFlags) {
			flagNode.generate(cv, gameContext);
		}

		for (StateClauseNode stateClauseNode : states.values()) {
			stateClauseNode.generate(cv, gameContext);
		}

		for (VariableNode variableNode : variables) {
			variableNode.generate(cv, gameContext);
		}

		for (ProcNode procNode : procs.values()) {
			gameContext.variableStore.addVariable(procNode.getName(), VariableType.METHOD);
			procNode.generate(cv, gameContext);
		}

		ActionNode.generateActions(cv, gameContext, actions);

		for (ObjectNode objectNode : objects) {
			cv.visitNestMember(GameContext.GAME_CLASS_NAME + "$" + objectNode.getName());
			cv.visitInnerClass(
					GameContext.GAME_CLASS_NAME + "$" + objectNode.getName(),
					GameContext.GAME_CLASS_NAME,
					objectNode.getName(),
					ACC_PUBLIC);
		}

		AsmUtils.createField(cv,
				ACC_PUBLIC,
				"places",
				"Ljava/util/Map;",
				"Ljava/util/Map<" + Type.getDescriptor(String.class) + Type.getDescriptor(GamePlace.class) + ">;");

		AsmUtils.createField(cv,
				ACC_PUBLIC,
				"objects",
				"Ljava/util/Map;",
				"Ljava/util/Map<" + Type.getDescriptor(String.class) + "Ljava/util/List<" + Type.getDescriptor(GameObject.class) + ">;>;");

		for (PlaceNode placeNode : places) {
			cv.visitNestMember(GameContext.GAME_CLASS_NAME + "$" + placeNode.getName());
			cv.visitInnerClass(
					GameContext.GAME_CLASS_NAME + "$" + placeNode.getName(),
					GameContext.GAME_CLASS_NAME,
					placeNode.getName(),
					ACC_PUBLIC);
		}

		for (InitialNode init : inits) {
			init.generate(cv, gameContext);
		}

		for (RepeatNode repeat : repeats) {
			repeat.generate(cv, gameContext);
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

		// Inject game object into internalFunctions
		mv.visitVarInsn(ALOAD, 1);
		mv.visitVarInsn(ALOAD, 0);
		mv.visitMethodInsn(
				INVOKEVIRTUAL,
				Type.getInternalName(InternalFunctions.class),
				"setGame",
				"(" + Type.getDescriptor(Object.class) + ")V",
				false);

		generateTextElements(mv);
		generateTexts(mv);
		generateFlags(mv);
		generateStates(gameContext, mv);
		generateVariables(mv);

		generatePlaceAssignments(mv);

		// Construct objects hashmap
		mv.visitVarInsn(ALOAD, 0);
		mv.visitTypeInsn(NEW, Type.getInternalName(HashMap.class));
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(HashMap.class), "<init>", "()V", false);
		mv.visitFieldInsn(PUTFIELD, GameContext.GAME_CLASS_NAME, "objects", Type.getDescriptor(Map.class));
//		// Add something
//		mv.visitVarInsn(ALOAD, 0);
//		mv.visitFieldInsn(GETFIELD, GameContext.GAME_CLASS_NAME, "objects", Type.getDescriptor(Map.class));
//
//		mv.visitLdcInsn("axe");
//
//		mv.visitIntInsn(SIPUSH, 1);
//		mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(GameObject.class));
//		mv.visitInsn(DUP);
//		mv.visitIntInsn(SIPUSH, 0);
//
//		mv.visitTypeInsn(NEW, GameContext.GAME_CLASS_NAME + "$" + "axe");
//		mv.visitInsn(DUP);
//		mv.visitVarInsn(ALOAD, 0);
//		mv.visitMethodInsn(
//				INVOKESPECIAL,
//				GameContext.GAME_CLASS_NAME + "$" + "axe",
//				"<init>",
//				"(" + GameContext.GAME_CLASS_DESCRIPTOR + ")V",
//				false);
//
//		mv.visitInsn(AASTORE);
//
//		mv.visitMethodInsn(
//				INVOKESTATIC,
//				Type.getInternalName(Arrays.class),
//				"asList",
//				"([" + Type.getDescriptor(Object.class) + ")Ljava/util/List;",
//				false
//		);
//
//		mv.visitMethodInsn(
//				INVOKEINTERFACE,
//				Type.getInternalName(Map.class),
//				"put",
//				"(" + Type.getDescriptor(Object.class) + Type.getDescriptor(Object.class) + ")" + Type.getDescriptor(Object.class),
//				true);
//		mv.visitInsn(POP);

		mv.visitInsn(RETURN);
		mv.visitMaxs(1, 1);
		mv.visitEnd();
	}

	private void generateTextElements(MethodVisitor mv) {
		Set<Integer> seenTextElement = new HashSet<>();
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
			for (int index = 0; index < textNode.getTextNodes().size(); index++) {
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

			mv.visitFieldInsn(PUTFIELD, GameContext.GAME_CLASS_NAME, textNode.getName(), Type.getDescriptor(Text.class));
		}
	}

	private void generateFlags(MethodVisitor mv) {
		int bitValue = 0;
		for (FlagNode flagNode : variableFlags) {
			for (String flag : flagNode.getFlags()) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitLdcInsn(1 << bitValue++);
				mv.visitFieldInsn(PUTFIELD, GameContext.GAME_CLASS_NAME, flag, "I");
			}
		}
		bitValue = 0;
		for (FlagNode flagNode : placeFlags) {
			for (String flag : flagNode.getFlags()) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitLdcInsn(1 << bitValue++);
				mv.visitFieldInsn(PUTFIELD, GameContext.GAME_CLASS_NAME, flag, "I");
			}
		}
		bitValue = 0;
		for (FlagNode flagNode : objectFlags) {
			for (String flag : flagNode.getFlags()) {
				mv.visitVarInsn(ALOAD, 0);
				mv.visitLdcInsn(1 << bitValue++);
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
				} else {
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

	private void generatePlaceAssignments(MethodVisitor mv) {
		// Construct places hashmap
		mv.visitVarInsn(ALOAD, 0);
		mv.visitTypeInsn(NEW, Type.getInternalName(HashMap.class));
		mv.visitInsn(DUP);
		mv.visitMethodInsn(INVOKESPECIAL, Type.getInternalName(HashMap.class), "<init>", "()V", false);
		mv.visitFieldInsn(PUTFIELD, GameContext.GAME_CLASS_NAME, "places", Type.getDescriptor(Map.class));

		for (PlaceNode placeNode : places) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(GETFIELD, GameContext.GAME_CLASS_NAME, "places", Type.getDescriptor(Map.class));
			mv.visitLdcInsn(placeNode.getName());
			mv.visitTypeInsn(NEW, GameContext.GAME_CLASS_NAME + "$" + placeNode.getName());
			mv.visitInsn(DUP);
			mv.visitVarInsn(ALOAD, 0);
			mv.visitMethodInsn(
					INVOKESPECIAL,
					GameContext.GAME_CLASS_NAME + "$" + placeNode.getName(),
					"<init>",
					"(" + GameContext.GAME_CLASS_DESCRIPTOR + ")V",
					false);
			mv.visitMethodInsn(
					INVOKEINTERFACE,
					Type.getInternalName(Map.class),
					"put",
					"(" + Type.getDescriptor(Object.class) + Type.getDescriptor(Object.class) + ")" + Type.getDescriptor(Object.class),
					true);
			mv.visitInsn(POP);
		}
	}

	public void createDefaultElements() {
		createState("badword", -2);
		createState("ambigword", -3);
		createState("badsyntax", -1);

		createVariable("arg1");
		createVariable("arg2");
		createVariable("status");
		createVariable("here");
		createVariable("there");

		createPlace("inhand", "inventory", "Inventory");
		createPlace("ylem", "ylem", "Ylem");
	}

	private void createState(String name, int value) {
		StateClauseNode node = new StateClauseNode(name, NumberLiteralNode.builder()
				.number(value)
				.sourceLocation(new SourceLocation(null, 0, 0))
				.build(),
				new SourceLocation(null, 0, 0));
		getStates().put(name, node);
		getIdentifiers().put(name, node);
	}

	private void createVariable(String name) {
		VariableNode node = VariableNode.builder()
				.variable(name)
				.sourceLocation(new SourceLocation(null, 0, 0))
				.build();
		getVariables().add(node);
		getIdentifiers().put(name, node);
	}

	private void createPlace(String name, String briefDescription, String longDescription) {
		PlaceNode node = PlaceNode.builder()
				.name(name)
				.verbs(new HashSet<>())
				.briefDescription(briefDescription)
				.longDescription(longDescription)
				.variables(new ArrayList<>())
				.commands(new LinkedHashMap<>())
				.procs(new LinkedHashMap<>())
				.sourceLocation(new SourceLocation(null, 0, 0))
				.build();
		// Add name to vocabulary.
		getVerbs().put(node.getName(), node);
		getIdentifiers().put(node.getName(), node);
		getPlaces().add(node);
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
