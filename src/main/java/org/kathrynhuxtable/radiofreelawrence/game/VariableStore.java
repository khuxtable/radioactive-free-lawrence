package org.kathrynhuxtable.radiofreelawrence.game;

import java.util.*;

import lombok.Getter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.springframework.stereotype.Component;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext.VariableScope;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext.VariableType;

@Component
public class VariableStore {

	public static class LoopContext {
		final String label;
		final Label breakLabel;
		final Label continueLabel;

		public LoopContext(String label, Label breakLabel, Label continueLabel) {
			this.label = label;
			this.breakLabel = breakLabel;
			this.continueLabel = continueLabel;
		}
	}

	@Getter
	private String currentClass = null;
	private int methodStackSize = 0;

	private final Map<String, VariableContext> globalContext = new HashMap<>();
	private final Map<String, VariableContext> classContext = new HashMap<>();
	private final List<Map<String, VariableContext>> functionContext = new ArrayList<>();
	private final List<LoopContext> loopContexts = new ArrayList<>();

	public void newClassScope(String className) {
		this.currentClass = className;
		this.classContext.clear(); // Just making sure
	}

	public void closeClassScope() {
		this.currentClass = null;
		this.classContext.clear();
	}

	// TODO Do we need to do something about variable scope here? What about paremeters?
	public void newFunctionScope() {
		functionContext.clear(); // Just making sure
		// Always start a new block scope with a function.
		newBlockScope();
		methodStackSize = 0;
	}

	public int closeFunctionScope(MethodVisitor mv, Label startLabel, Label endLabel) {
		closeBlockScope(mv, startLabel, endLabel);
		return methodStackSize;
	}

	public void newLoopScope(String label, Label breakLabel, Label continueLabel) {
		loopContexts.add(new LoopContext(label, breakLabel, continueLabel));
	}

	public void closeLoopScope() {
		loopContexts.remove(loopContexts.size() - 1);
	}

	// TODO Need to do something about variable assignments and labels.
	public void newBlockScope() {
		functionContext.add(new HashMap<>());
	}

	// TODO Need to do something about variable assignments and clearing them.
	public void closeBlockScope(MethodVisitor mv, Label startLabel, Label endLabel) {
		Collection<VariableContext> vars = functionContext.remove(functionContext.size() - 1).values();
		for (VariableContext var : vars) {
			String type = switch (var.getVariableType()) {
				case FLAG, STATE, NUMBER -> Type.INT_TYPE.getDescriptor();
				case TEXT -> Type.getDescriptor(String.class);
				case TEXT_NODE ->  Type.getDescriptor(Text.class);
				case REFERENCE -> Type.getDescriptor(Object.class);
				case LABEL, METHOD -> null;
			};
			mv.visitLocalVariable(var.getName(), type, null, startLabel, endLabel, var.getIndex());
		}
	}

	public VariableContext addVariable(String identifier, VariableType variableType) {
		if (functionContext.isEmpty()) {
			if (currentClass != null) {
				VariableContext variableContext = VariableContext.builder()
						.name(identifier)
						.variableScope(VariableScope.CLASS)
						.parentClass(currentClass)
						.variableType(variableType)
						.build();
				classContext.put(identifier, variableContext);
				return variableContext;
			} else {
				VariableContext variableContext = VariableContext.builder()
						.name(identifier)
						.variableScope(VariableScope.GLOBAL)
						.variableType(variableType)
						.parentClass(currentClass)
						.build();
				globalContext.put(identifier, variableContext);
				return variableContext;
			}
		} else {
			++methodStackSize;

			// Get innermost scope.
			Map<String, VariableContext> blockScope = functionContext.get(functionContext.size() - 1);

			VariableContext variableContext = VariableContext.builder()
					.name(identifier)
					.variableScope(VariableScope.LOCAL)
					.variableType(variableType)
					.index(-1)
					.build();
			blockScope.put(identifier, variableContext);
			return variableContext;
		}
	}

	public Label getBreakLabel(String label) {
		LoopContext loopContext = getLoopContext(label);
		return loopContext.breakLabel;
	}

	public Label getContinueLabel(String label) {
		LoopContext loopContext = getLoopContext(label);
		return loopContext.continueLabel;
	}

	public LoopContext getLoopContext(String label) {
		if (loopContexts.isEmpty()) {
			throw new RuntimeException("Cannot get break or continue destination: not in loop");
		}

		if (label == null) {
			return loopContexts.get(loopContexts.size() - 1);
		}

		for (int i = loopContexts.size() - 1; i >= 0; i--) {
			if (label.equals(loopContexts.get(i).label)) {
				return loopContexts.get(i);
			}
		}

		throw new RuntimeException("Cannot find loop for label " + label);
	}

	public VariableContext getVariable(String identifier) {
		for (LoopContext loopContext : loopContexts) {
			if (identifier.equals(loopContext.label)) {
				throw new RuntimeException(String.format("variable is loop label: %s", identifier));
			}
		}

		for (int i = functionContext.size() - 1; i >= 0; i--) {
			if (functionContext.get(i).containsKey(identifier)) {
				return functionContext.get(i).get(identifier);
			}
		}

		if (classContext.containsKey(identifier)) {
			return classContext.get(identifier);
		}

		if (globalContext.containsKey(identifier)) {
			return globalContext.get(identifier);
		}

		return null;
	}
}
