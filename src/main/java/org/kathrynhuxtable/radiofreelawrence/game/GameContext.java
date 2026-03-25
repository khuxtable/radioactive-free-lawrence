package org.kathrynhuxtable.radiofreelawrence.game;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.ErrorReporter;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.GameNode;

@Data
@AllArgsConstructor
public class GameContext {
	public static final String GAME_CLASS_PACKAGE = "org/kathrynhuxtable/radiofreelawrence/game";
	public static final String GAME_CLASS_NAME = GAME_CLASS_PACKAGE + "/Game";
	public static final String GAME_CLASS_DESCRIPTOR = "L" +  GAME_CLASS_NAME + ";";

	public final VariableStore variableStore = new VariableStore();

	public final GameNode gameNode = new GameNode();
	public final ErrorReporter errorReporter = new ErrorReporter();

	public final InternalFunctions internalFunctions = new InternalFunctions(this);
}
