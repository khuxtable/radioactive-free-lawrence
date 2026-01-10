package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionInvocationNode implements ExprNode {
	private IdentifierNode identifier;
	private String internalFunction;
	private String verbFunction;
	private List<ExprNode> parameters;
	private boolean internal;

	@Override
	public int evaluate(GameData gameData) {
		String functionName = internalFunction != null ? internalFunction : identifier.getName();
		return gameData.callFunction(functionName, internal, parameters);
	}
}
