package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.ArrayList;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.exception.BreakException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.ContinueException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnhancedForStatementNode implements StatementNode {
	private IdentifierNode identifier;
	private ExprNode expression;
	private StatementNode statement;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void execute(GameData gameData) throws GameRuntimeException {
		gameData.localVariables.newBlockScope();
		try {
			int expr = expression.evaluate(gameData);
			List<Integer> objects = new ArrayList<>();
			if (expr >= gameData.floc && expr < gameData.lloc) {
				for (int obj = gameData.fobj; obj < gameData.lobj; obj++) {
					if (gameData.locations[obj - gameData.fobj] == expr) {
						objects.add(obj);
					}
				}
			}
			gameData.localVariables.addVariable(
					identifier.getName(),
					NumberLiteralNode.builder()
							.number(0)
							.sourceLocation(sourceLocation)
							.build());
			for (Integer obj : objects) {
				try {
					gameData.localVariables.setLocalVariableValue(identifier.getName(), obj);
					statement.execute(gameData);
				} catch (BreakException e) {
					if (e.ignore(label)) {
						throw e;
					} else {
						break;
					}
				} catch (ContinueException e) {
					if (e.ignore(label)) {
						throw e;
					}
					// Implicit continue
				}
			}
		} finally {
			gameData.localVariables.closeBlockScope();
		}
	}

/*
ITOBJ varname [{placename|varname*}] [objflag]
Execute the following code up to the matching FIN repeatedly, with
the value of the nominated "loop variable" becoming a reference to
objects satisfying the optional location and/of flag/state constraints (or
all object, if no constraints specified) in the order of their declaration.

ITPLACE varname [{placename1|varname1*}, {placename2|varname2*}]
Execute the following code up to the matching FIN repeatedly, with
the value of the nominated "loop variable" running through the
specified range of locations (default all declared locations) in the
order of their declaration.

ITERATE varname, {entname1|varname1*|const1}, {entname2|varname2*|const2}
Execute the following code up to the matching FIN repeatedly, with
the value of the nominated "loop variable" running through all values
from entname1 to entname2 inclusive. If either of the two range
delimiting entnames is a variable, its value is used as the appropriate
loop boundary (this may but need not be the reference number of
some other entity). If either is a constant, the value of the constant is
used. Otherwise, the reference number of the nominated entity is used.

DOALL [{placename|varname*}] [objflag]
DOALL starts off a do-all loop, in which the REPEAT cycle is
repeated, but instead of querying the player for input, input is
constructed out of the verb in ARG1 and the next object fitting the
specified criteria. The loop is terminated either when no more objects
fit the criteria or when the FLUSH directive is executed.

FLUSH
Abort the do-all loop if one executing and flush the command line buffer.
 */
}
