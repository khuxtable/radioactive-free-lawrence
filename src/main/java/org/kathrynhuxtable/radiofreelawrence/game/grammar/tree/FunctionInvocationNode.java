package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.LoopControlException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionInvocationNode implements ExprNode {
	private IdentifierNode identifier;
	private String internalFunction;
	private String verbFunction;
	private List<ExprNode> parameters;
	private SourceLocation sourceLocation;

	@Override
	public int evaluate(GameData gameData) {
		String functionName;
		if (identifier != null) {
			functionName = identifier.getName();
		} else if (internalFunction != null) {
			functionName = internalFunction;
		} else if (verbFunction != null) {
			functionName = verbFunction;
		} else {
			throw new GameRuntimeException("Unknown function invocation");
		}

		try {
			return gameData.callFunction(functionName, parameters);
		} catch (Exception e) {
			if (e instanceof LoopControlException) {
				throw e;
			} else {
				throw new GameRuntimeException(sourceLocation.toString(), e);
			}
		}
	}
}
