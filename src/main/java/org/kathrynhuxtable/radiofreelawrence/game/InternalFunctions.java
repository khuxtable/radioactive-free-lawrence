package org.kathrynhuxtable.radiofreelawrence.game;

import java.lang.reflect.Method;
import java.util.*;

import lombok.RequiredArgsConstructor;

import org.kathrynhuxtable.gdesc.parser.GameInfo;
import org.kathrynhuxtable.gdesc.parser.InternalFunction;
import org.kathrynhuxtable.radiofreelawrence.game.exception.BreakException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.LoopControlException.ControlType;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.*;

@RequiredArgsConstructor
public class InternalFunctions {

	private final GameData gameData;

	private Map<String, Method> internalFunctions;

	public Method getInternalFunction(String name) {
		return getInternalFunctions().get(name);
	}

	private Map<String, Method> getInternalFunctions() {
		if (internalFunctions == null) {
			internalFunctions = new HashMap<>();
			for (Method method : InternalFunctions.class.getDeclaredMethods()) {
				if (method.isAnnotationPresent(InternalFunction.class)) {
					InternalFunction annotation = method.getAnnotation(InternalFunction.class);
					internalFunctions.put(annotation.name(), method);
				}
			}
		}
		return internalFunctions;
	}

	public void validateGrammar() {
		Set<String> internalFunctions = getInternalFunctions().keySet();
		Set<String> grammarInternalFunctions = new GameInfo().getInternalFunctionNames();
		if (!internalFunctions.containsAll(grammarInternalFunctions)) {
			Set<String> missingInternalFunctions = new HashSet<>(grammarInternalFunctions);
			missingInternalFunctions.removeAll(internalFunctions);
			throw new GameRuntimeException("Internal functions does not contain all grammar functions: " + missingInternalFunctions);
		} else if (!grammarInternalFunctions.containsAll(internalFunctions)) {
			Set<String> missingGrammarInternalFunctions = new HashSet<>(internalFunctions);
			missingGrammarInternalFunctions.removeAll(grammarInternalFunctions);
			throw new GameRuntimeException("Grammar functions does not contain all internal functions: " + missingGrammarInternalFunctions);
		}
	}

	// Internal functions here.

	@InternalFunction(name = "input")
	public int input(ExprNode... parameters) {
		gameData.clearFlag(gameData.getIdentifierRefno("status"), gameData.getIntIdentifierValue("moved"));
		gameData.getInput().input();
		return 0;
	}

	@InternalFunction(name = "in")
	public int inrange(ExprNode... parameters) {
		int value = parameters[0].evaluate(gameData);
		return (value >= parameters[1].evaluate(gameData) && value <= parameters[2].evaluate(gameData)) ? 1 : 0;
	}

	@InternalFunction(name = "chance")
	public int ischance(ExprNode... parameters) {
		return Math.random() * 100 < parameters[0].evaluate(gameData) ? 1 : 0;
	}

	@InternalFunction(name = "getrandom")
	public int random(ExprNode... parameters) {
		int min;
		int max;
		if (parameters.length == 0) {
			min = 0;
			max = 100;
		} else if (parameters.length == 1) {
			min = 0;
			max = parameters[0].evaluate(gameData);
		} else {
			min = parameters[0].evaluate(gameData);
			max = parameters[1].evaluate(gameData);
		}
		return (int) (Math.random() * (max - min + 1)) + min;
	}

	@InternalFunction(name = "have")
	public int ishave(ExprNode... parameters) {
		int refno = parameters[0].evaluate(gameData);
		// The inhand location (inventory) is always refno floc.
		if (refno >= gameData.fobj && refno < gameData.lobj) {
			if (gameData.locations[refno - gameData.fobj] == gameData.floc) {
				return 1;
			}
		}
		return 0;
	}

	@InternalFunction(name = "ishere")
	public int ishere(ExprNode... parameters) {
		int refno = parameters[0].evaluate(gameData);
		if (refno >= gameData.fobj && refno < gameData.lobj) {
			if (gameData.locations[refno - gameData.fobj] == gameData.getIntIdentifierValue("here")) {
				return 1;
			}
		}
		return 0;
	}

	@InternalFunction(name = "isnear")
	public int isnear(ExprNode... parameters) {
		return ishave(parameters) != 0 || ishere(parameters) != 0 ? 1 : 0;
	}

	@InternalFunction(name = "isflag")
	public int isflag(ExprNode... parameters) {
		long flag = parameters[1].evaluate(gameData);
		return gameData.testFlag(parameters[0], flag) ? 1 : 0;
	}

	@InternalFunction(name = "setflag")
	public int setflag(ExprNode... parameters) {
		int refno = parameters[0].evaluate(gameData);
		long flag = parameters[1].evaluate(gameData);
		gameData.setFlag(refno, flag);
		return 0;
	}

	@InternalFunction(name = "clearflag")
	public int clearflag(ExprNode... parameters) {
		int refno = parameters[0].evaluate(gameData);
		long flag = parameters[1].evaluate(gameData);
		gameData.clearFlag(refno, flag);
		return 0;
	}

	@InternalFunction(name = "isat")
	public int isat(ExprNode... parameters) {
		int here = gameData.getIntIdentifierValue("here");
		for (ExprNode node : parameters) {
			int place = node.evaluate(gameData);
			if (place == here) {
				return 1;
			}
		}
		return 0;
	}

	@InternalFunction(name = "atplace")
	public int atplace(ExprNode... parameters) {
		int obj = gameData.getIntIdentifierValue("here");
		int loc = gameData.locations[obj - gameData.fobj];
		for (int i = 1; i < parameters.length; i++) {
			int place = parameters[i].evaluate(gameData);
			if (place == loc) {
				return 1;
			}
		}
		return 0;
	}

	@InternalFunction(name = "varis")
	public int varis(ExprNode... parameters) {
		int refno = parameters[0].evaluate(gameData);
		if (refno < gameData.fvar || refno >= gameData.lvar) {
			return 0;
		}
		int value = gameData.variables[refno - gameData.fvar];
		for (int i = 1; i < parameters.length; i++) {
			int other = parameters[i].evaluate(gameData);
			if (other == refno) {
				return 1;
			}
		}
		return 0;
	}

	@InternalFunction(name = "key")
	public int iskey(ExprNode... parameters) {
		int refno = gameData.getIntIdentifierValue("arg1");
		if (refno == 0) {
			return 0;
		}
		for (ExprNode parameter : parameters) {
			if (parameter instanceof TextElementNode textElementNode) {
				VocabularyNode node = (VocabularyNode) gameData.getRefnoNode(refno);
				if (!textElementNode.getText().equals(node.getName())) {
					return 0;
				}
			} else if (parameter instanceof IdentifierNode identifierNode) {
				int idVal = gameData.getIntIdentifierValue(identifierNode.getName());
				if (idVal == 0) {
					// Unrecognized value -- ignore
					continue;
				}
				BaseNode node = gameData.getRefnoNode(idVal);
				if (node instanceof TextNode textNode) {
					VocabularyNode vnode = (VocabularyNode) gameData.getRefnoNode(refno);
					if (!textNode.getTexts().get(0).equals(vnode.getName())) {
						return 0;
					}
				} else if (idVal != refno) {
					return 0;
				}
			} else {
				int value = parameter.evaluate(gameData);
				if (value != refno) {
					return 0;
				}
			}
		}
		return 1;
	}

	@InternalFunction(name = "anyof")
	public int anyof(ExprNode... parameters) {
		int verb = gameData.getIntIdentifierValue("arg1");
		for (ExprNode parameter : parameters) {
			int value = parameter.evaluate(gameData);
			if (value == verb) {
				return 1;
			}
		}
		return 0;
	}

	@InternalFunction(name = "query")
	public int getquery(ExprNode... parameters) {
		// FIXME Implement this
		return 0;
	}

	@InternalFunction(name = "usertyped")
	public int usertyped(ExprNode... parameters) {
		// FIXME Implement this
		return 0;
	}

	@InternalFunction(name = "needcmd")
	public int needcmd(ExprNode... parameters) {
		// FIXME Implement this
		return 0;
	}

	/*
	 * Abort the do-all loop if one executing and flush the command line buffer.
	 */
	@InternalFunction(name = "flush")
	public int flushinput(ExprNode... parameters) {
		// FIXME Implement this
		return 0;
	}

	@InternalFunction(name = "apport")
	public int apport(ExprNode... parameters) {
		int object = parameters[0] instanceof IdentifierNode identifierNode ?
				gameData.getIntIdentifierValue(identifierNode.getName().toLowerCase()) :
				parameters[0].evaluate(gameData);
		int place = parameters[1] instanceof IdentifierNode identifierNode ?
				gameData.getIntIdentifierValue(identifierNode.getName().toLowerCase()) :
				parameters[1].evaluate(gameData);
		gameData.locations[object - gameData.fobj] = place;
		return 0;
	}

	@InternalFunction(name = "get")
	public int iget(ExprNode... parameters) {
		int object = parameters[0] instanceof TextElementNode ?
				gameData.getIntIdentifierValue(((TextElementNode) parameters[0]).getText().toLowerCase()) :
				parameters[0].evaluate(gameData);
		if (object > 0) {
			gameData.locations[object - gameData.fobj] = gameData.floc;
		}
		return 0;
	}

	@InternalFunction(name = "drop")
	public int idrop(ExprNode... parameters) {
		int object = parameters[0] instanceof TextElementNode ?
				gameData.getIntIdentifierValue(((TextElementNode) parameters[0]).getText().toLowerCase()) :
				parameters[0].evaluate(gameData);
		if (object > 0) {
			gameData.locations[object - gameData.fobj] = gameData.getIntIdentifierValue("here");
		}
		return 0;
	}

	// isverb("verb", proc, parameters...)
	@InternalFunction(name = "isverb")
	public int isverb(ExprNode... parameters) {
		if (parameters.length > 1) {
			if (iskey(parameters[0]) == 0) {
				return 0;
			}
		}

		if (!(parameters[1] instanceof IdentifierNode identifierNode)) {
			throw new GameRuntimeException("second parameter to isverb must be proc identifier");
		}

		List<ExprNode> exprNodeList = new ArrayList<>();
//		exprNodeList.add(objectNode);
		exprNodeList.addAll(Arrays.asList(Arrays.copyOfRange(parameters, 2, parameters.length)));

		gameData.callFunction(identifierNode.getName(), exprNodeList);

		throw new BreakException(ControlType.REPEAT);
	}

	@InternalFunction(name = "goto")
	public int goto_(ExprNode... parameters) {
		int place = parameters[0] instanceof TextElementNode ?
				gameData.getIntIdentifierValue(((TextElementNode) parameters[0]).getText().toLowerCase()) :
				parameters[0].evaluate(gameData);
		gameData.setIntIdentifierValue("there", gameData.getIntIdentifierValue("here"));
		gameData.setIntIdentifierValue("here", place);
		gameData.setFlag(gameData.getIdentifierRefno("status"), gameData.getIntIdentifierValue("moved"));
		return 0;
	}

	@InternalFunction(name = "move")
	public int move_(ExprNode... parameters) {
		int place = parameters[parameters.length - 1] instanceof TextElementNode ?
				gameData.getIntIdentifierValue(((TextElementNode) parameters[parameters.length - 1]).getText().toLowerCase()) :
				parameters[parameters.length - 1].evaluate(gameData);
		if (parameters.length > 1) {
			if (iskey(Arrays.copyOfRange(parameters, 0, parameters.length - 1)) == 0) {
				return 0;
			}
		}
		gameData.setIntIdentifierValue("there", gameData.getIntIdentifierValue("here"));
		gameData.setIntIdentifierValue("here", place);
		gameData.setFlag(gameData.getIdentifierRefno("status"), gameData.getIntIdentifierValue("moved"));

		for (int obj = gameData.fobj; obj < gameData.lobj; obj++) {
			if (gameData.locations[obj - gameData.fobj] == place) {
				gameData.setFlag(obj, gameData.getIntIdentifierValue("seen"));
			}
		}

		throw new BreakException(ControlType.REPEAT);
	}

	@InternalFunction(name = "smove")
	public int smove(ExprNode... parameters) {
		// FIXME Implement this
		return 0;
	}

	@InternalFunction(name = "say")
	public int say_(ExprNode... parameters) {
		try {
			if (parameters.length == 0) return 0;
			String text = "";
			if (parameters[0] instanceof IdentifierNode) {
				text = gameData.getTextIdentifierValue(((IdentifierNode) parameters[0]).getName(), 0);
			} else if (parameters[0] instanceof TextElementNode) {
				text = ((TextElementNode) parameters[0]).getText();
			}

			Integer qualifier = null;
			if (parameters.length > 1) {
				qualifier = parameters[1].evaluate(gameData);
			}

			System.out.println(gameData.expandText(text, qualifier));
		} catch (Exception e) {
			throw new GameRuntimeException(parameters[0].getSourceLocation() + ": exception in 'say'", e);
		}

		return 0;
	}

	@InternalFunction(name = "append")
	public int append(ExprNode... text) {
		// FIXME Implement this
		// Not sure how to implement this, since we typically generate the newline after the text.
		// Maybe need to generate it before. Not sure.
		return 0;
	}

	@InternalFunction(name = "quip")
	public int quip(ExprNode... text) {
		say_(text);
		throw new BreakException(ControlType.REPEAT);
	}

	@InternalFunction(name = "respond")
	public int respond(ExprNode... parameters) {
		if (anyof(parameters) != 0) {
			quip(Arrays.copyOfRange(parameters, parameters.length - 1, parameters.length));
		}
		return 0;
	}

	@InternalFunction(name = "describe")
	public int describe_(ExprNode... parameters) {
		if (parameters.length == 0) return 0;
		int var = parameters[0] instanceof IdentifierNode ?
				gameData.getIntIdentifierValue(((IdentifierNode) parameters[0]).getName()) :
				parameters[0].evaluate(gameData);
		if (var >= gameData.fvar && var < gameData.lvar) {
			var = gameData.variables[var - gameData.fvar];
		}
		System.out.println(gameData.getTextIdentifierValue(var, parameters.length > 1 ? 1 : 0));
		return 0;
	}

	@InternalFunction(name = "vocab")
	public int vocab(ExprNode... text) {
		// FIXME Implement this
		// TODO Need a way to get "verbs" from places and objects.
		return 0;
	}

	@InternalFunction(name = "tie")
	public int tie(ExprNode... parameters) {
		// FIXME Implement this
		return 0;
	}

	@InternalFunction(name = "stop")
	public int stop(ExprNode... parameters) {
		System.exit(0);
		return 0;
	}
}
