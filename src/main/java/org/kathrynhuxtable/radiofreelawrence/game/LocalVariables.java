package org.kathrynhuxtable.radiofreelawrence.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.BaseNode;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.NumberLiteralNode;

public class LocalVariables {

	private final List<List<Map<String, BaseNode>>> localVariables = new ArrayList<>();

	public void newFunctionScope() {
		localVariables.add(new ArrayList<>());
		// Always start a new block scope with a function.
		newBlockScope();
	}

	public void closeFunctionScope() {
		if (!localVariables.isEmpty()) {
			localVariables.remove(localVariables.size() - 1);
		}
	}

	private List<Map<String, BaseNode>> getFunctionScope() {
		return localVariables.get(localVariables.size() - 1);
	}

	public void newBlockScope() {
		getFunctionScope().add(new HashMap<>());
	}

	public void closeBlockScope() {
		List<Map<String, BaseNode>> functionScope = getFunctionScope();
		functionScope.remove(functionScope.size() - 1);
	}

	public void addVariable(String identifier, BaseNode value) {
		if (localVariables.isEmpty() || localVariables.get(localVariables.size() - 1).isEmpty()) {
			throw new GameRuntimeException("No block scope defined");
		}

		List<Map<String, BaseNode>> functionScope = getFunctionScope();
		// Get innermost scope.
		Map<String, BaseNode> blockScope = functionScope.get(functionScope.size() - 1);

		blockScope.put(identifier, value);
	}

	public BaseNode getLocalVariableValue(String variable) {
		if (!localVariables.isEmpty()) {
			List<Map<String, BaseNode>> functionScope = getFunctionScope();
			if (!functionScope.isEmpty()) {
				for (int i = functionScope.size() - 1; i >= 0; i--) {
					if (functionScope.get(i).containsKey(variable)) {
						return functionScope.get(i).get(variable);
					}
				}
			}
		}
		return null;
	}

	public boolean setLocalVariableValue(String variable, int value) {
		if (!localVariables.isEmpty()) {
			if (!getFunctionScope().isEmpty()) {
				for (int i = getFunctionScope().size() - 1; i >= 0; i--) {
					Map<String, BaseNode> blockScope = getFunctionScope().get(i);
					if (blockScope.containsKey(variable)) {
						BaseNode node = blockScope.get(variable);
						if (node instanceof NumberLiteralNode numberLiteralNode) {
							numberLiteralNode.setNumber(value);
						} else {
							throw new GameRuntimeException("Invalid type for variable " + variable);
						}
						return true;
					}
				}
			}
		}
		return false;
	}
}
