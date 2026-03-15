package org.kathrynhuxtable.radiofreelawrence.game;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.Setter;

import org.kathrynhuxtable.gdesc.parser.GameInfo;
import org.kathrynhuxtable.gdesc.parser.InternalFunction;
import org.kathrynhuxtable.radiofreelawrence.game.exception.BreakException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.ControlType;

public class InternalFunctions {

	@Setter
	private Object game;

	private Map<String, String> internalFunctions;

	public String getInternalFunction(String name) {
		return getInternalFunctions().get(name);
	}

	private Map<String, String> getInternalFunctions() {
		if (internalFunctions == null) {
			internalFunctions = new HashMap<>();
			for (Method method : InternalFunctions.class.getDeclaredMethods()) {
				if (method.isAnnotationPresent(InternalFunction.class)) {
					InternalFunction annotation = method.getAnnotation(InternalFunction.class);
					internalFunctions.put(annotation.name(), method.getName());
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

	private Object getGameVariable(String name) {
		Object gameVariable = null;
		try {
			gameVariable = internalFunctions.get(name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return gameVariable;
	}

//	 Internal functions here.

//	@InternalFunction(name = "input")
//	public int input(ExprNode... parameters) {
//		gameContext.clearFlag(gameContext.getIdentifierRefno("status"), gameContext.getIntIdentifierValue("moved"));
//		gameContext.getInput().input();
//		return 0;
//	}

	@InternalFunction(name = "in")
	public int inrange(Object... parameters) {
		int value = (int) parameters[0];
		return (value >= (int) parameters[1] && value <= (int) parameters[2]) ? 1 : 0;
	}

	@InternalFunction(name = "chance")
	public int ischance(Object... parameters) {
		return Math.random() * 100 < (int) parameters[0] ? 1 : 0;
	}

	@InternalFunction(name = "getrandom")
	public int random(Object... parameters) {
		int min;
		int max;
		if (parameters.length == 0) {
			min = 0;
			max = 100;
		} else if (parameters.length == 1) {
			min = 0;
			max = (int) parameters[0];
		} else {
			min = (int) parameters[0];
			max = (int) parameters[1];
		}
		return (int) (Math.random() * (max - min + 1)) + min;
	}

	@InternalFunction(name = "have")
	public int ishave(Object... parameters) {
		Object refno = parameters[0];
		// The inhand location (inventory) is always refno floc.
		if (refno instanceof GameObject) {
//			if (gameContext.locations[refno - gameContext.fobj] == gameContext.floc) {
//				return 1;
//			}
		}
		return 0;
	}

//	@InternalFunction(name = "ishere")
//	public int ishere(ExprNode... parameters) {
//		int refno = parameters[0].evaluate(gameContext);
//		if (refno >= gameContext.fobj && refno < gameContext.lobj) {
//			if (gameContext.locations[refno - gameContext.fobj] == gameContext.getIntIdentifierValue("here")) {
//				return 1;
//			}
//		}
//		return 0;
//	}
//
//	@InternalFunction(name = "isnear")
//	public int isnear(ExprNode... parameters) {
//		return ishave(parameters) != 0 || ishere(parameters) != 0 ? 1 : 0;
//	}
//
//	@InternalFunction(name = "isflag")
//	public int isflag(ExprNode... parameters) {
//		long flag = parameters[1].evaluate(gameContext);
//		return gameContext.testFlag(parameters[0], flag) ? 1 : 0;
//	}
//
//	@InternalFunction(name = "setflag")
//	public int setflag(ExprNode... parameters) {
//		int refno = parameters[0].evaluate(gameContext);
//		long flag = parameters[1].evaluate(gameContext);
//		gameContext.setFlag(refno, flag);
//		return 0;
//	}
//
//	@InternalFunction(name = "clearflag")
//	public int clearflag(ExprNode... parameters) {
//		int refno = parameters[0].evaluate(gameContext);
//		long flag = parameters[1].evaluate(gameContext);
//		gameContext.clearFlag(refno, flag);
//		return 0;
//	}
//
//	@InternalFunction(name = "isat")
//	public int isat(ExprNode... parameters) {
//		int here = gameContext.getIntIdentifierValue("here");
//		for (ExprNode node : parameters) {
//			int place = node.evaluate(gameContext);
//			if (place == here) {
//				return 1;
//			}
//		}
//		return 0;
//	}
//
//	@InternalFunction(name = "atplace")
//	public int atplace(ExprNode... parameters) {
//		int obj = gameContext.getIntIdentifierValue("here");
//		int loc = gameContext.locations[obj - gameContext.fobj];
//		for (int i = 1; i < parameters.length; i++) {
//			int place = parameters[i].evaluate(gameContext);
//			if (place == loc) {
//				return 1;
//			}
//		}
//		return 0;
//	}
//
//	@InternalFunction(name = "varis")
//	public int varis(ExprNode... parameters) {
//		int refno = parameters[0].evaluate(gameContext);
//		if (refno < gameContext.fvar || refno >= gameContext.lvar) {
//			return 0;
//		}
//		int value = gameContext.variables[refno - gameContext.fvar];
//		for (int i = 1; i < parameters.length; i++) {
//			int other = parameters[i].evaluate(gameContext);
//			if (other == refno) {
//				return 1;
//			}
//		}
//		return 0;
//	}
//
//	@InternalFunction(name = "key")
//	public int iskey(ExprNode... parameters) {
//		int refno = gameContext.getIntIdentifierValue("arg1");
//		if (refno == 0) {
//			return 0;
//		}
//		for (ExprNode parameter : parameters) {
//			if (parameter instanceof TextElementNode textElementNode) {
//				VocabularyNode node = (VocabularyNode) gameContext.getRefnoNode(refno);
//				if (!textElementNode.getText().equals(node.getName())) {
//					return 0;
//				}
//			} else if (parameter instanceof IdentifierNode identifierNode) {
//				int idVal = gameContext.getIntIdentifierValue(identifierNode.getName());
//				if (idVal == 0) {
//					// Unrecognized value -- ignore
//					continue;
//				}
//				BaseNode node = gameContext.getRefnoNode(idVal);
//				if (node instanceof TextNode textNode) {
//					VocabularyNode vnode = (VocabularyNode) gameContext.getRefnoNode(refno);
//					if (!textNode.getTexts().get(0).equals(vnode.getName())) {
//						return 0;
//					}
//				} else if (idVal != refno) {
//					return 0;
//				}
//			} else {
//				int value = parameter.evaluate(gameContext);
//				if (value != refno) {
//					return 0;
//				}
//			}
//		}
//		return 1;
//	}
//
//	@InternalFunction(name = "anyof")
//	public int anyof(ExprNode... parameters) {
//		int verb = gameContext.getIntIdentifierValue("arg1");
//		for (ExprNode parameter : parameters) {
//			int value = parameter.evaluate(gameContext);
//			if (value == verb) {
//				return 1;
//			}
//		}
//		return 0;
//	}
//
//	@InternalFunction(name = "query")
//	public int getquery(ExprNode... parameters) {
//		// FIXME Implement this
//		return 0;
//	}
//
//	@InternalFunction(name = "usertyped")
//	public int usertyped(ExprNode... parameters) {
//		// FIXME Implement this
//		return 0;
//	}
//
//	@InternalFunction(name = "needcmd")
//	public int needcmd(ExprNode... parameters) {
//		// FIXME Implement this
//		return 0;
//	}
//
//	/*
//	 * Abort the do-all loop if one executing and flush the command line buffer.
//	 */
//	@InternalFunction(name = "flush")
//	public int flushinput(ExprNode... parameters) {
//		// FIXME Implement this
//		return 0;
//	}
//
//	@InternalFunction(name = "apport")
//	public int apport(ExprNode... parameters) {
//		int object = parameters[0] instanceof IdentifierNode identifierNode ?
//				gameContext.getIntIdentifierValue(identifierNode.getName().toLowerCase()) :
//				parameters[0].evaluate(gameContext);
//		int place = parameters[1] instanceof IdentifierNode identifierNode ?
//				gameContext.getIntIdentifierValue(identifierNode.getName().toLowerCase()) :
//				parameters[1].evaluate(gameContext);
//		gameContext.locations[object - gameContext.fobj] = place;
//		return 0;
//	}
//
//	@InternalFunction(name = "get")
//	public int iget(ExprNode... parameters) {
//		int object = parameters[0] instanceof TextElementNode ?
//				gameContext.getIntIdentifierValue(((TextElementNode) parameters[0]).getText().toLowerCase()) :
//				parameters[0].evaluate(gameContext);
//		if (object > 0) {
//			gameContext.locations[object - gameContext.fobj] = gameContext.floc;
//		}
//		return 0;
//	}
//
//	@InternalFunction(name = "drop")
//	public int idrop(ExprNode... parameters) {
//		int object = parameters[0] instanceof TextElementNode ?
//				gameContext.getIntIdentifierValue(((TextElementNode) parameters[0]).getText().toLowerCase()) :
//				parameters[0].evaluate(gameContext);
//		if (object > 0) {
//			gameContext.locations[object - gameContext.fobj] = gameContext.getIntIdentifierValue("here");
//		}
//		return 0;
//	}
//
//	// isverb("verb", proc, parameters...)
//	@InternalFunction(name = "isverb")
//	public int isverb(ExprNode... parameters) {
//		if (parameters.length > 1) {
//			if (iskey(parameters[0]) == 0) {
//				return 0;
//			}
//		}
//
//		if (!(parameters[1] instanceof IdentifierNode identifierNode)) {
//			throw new GameRuntimeException("second parameter to isverb must be proc identifier");
//		}
//
//		List<ExprNode> exprNodeList = new ArrayList<>();
////		exprNodeList.add(objectNode);
//		exprNodeList.addAll(Arrays.asList(Arrays.copyOfRange(parameters, 2, parameters.length)));
//
//		gameContext.callFunction(identifierNode.getName(), exprNodeList);
//
//		throw new BreakException(ControlType.REPEAT);
//	}
//
//	@InternalFunction(name = "goto")
//	public int goto_(ExprNode... parameters) {
//		int place = parameters[0] instanceof TextElementNode ?
//				gameContext.getIntIdentifierValue(((TextElementNode) parameters[0]).getText().toLowerCase()) :
//				parameters[0].evaluate(gameContext);
//		gameContext.setIntIdentifierValue("there", gameContext.getIntIdentifierValue("here"));
//		gameContext.setIntIdentifierValue("here", place);
//		gameContext.setFlag(gameContext.getIdentifierRefno("status"), gameContext.getIntIdentifierValue("moved"));
//		return 0;
//	}
//
//	@InternalFunction(name = "move")
//	public int move_(ExprNode... parameters) {
//		int place = parameters[0] instanceof TextElementNode ?
//				gameContext.getIntIdentifierValue(((TextElementNode) parameters[0]).getText().toLowerCase()) :
//				parameters[0].evaluate(gameContext);
//
//		if (parameters.length > 1) {
//			say_(Arrays.copyOfRange(parameters, 1, parameters.length));
//		}
//
//		gameContext.setIntIdentifierValue("there", gameContext.getIntIdentifierValue("here"));
//		gameContext.setIntIdentifierValue("here", place);
//		gameContext.setFlag(gameContext.getIdentifierRefno("status"), gameContext.getIntIdentifierValue("moved"));
//
//		for (int obj = gameContext.fobj; obj < gameContext.lobj; obj++) {
//			if (gameContext.locations[obj - gameContext.fobj] == place) {
//				gameContext.setFlag(obj, gameContext.getIntIdentifierValue("seen"));
//			}
//		}
//
//		throw new BreakException(ControlType.REPEAT);
//	}
//
//	@InternalFunction(name = "smove")
//	public int smove(ExprNode... parameters) {
//		// FIXME Implement this
//		return 0;
//	}

	@InternalFunction(name = "say")
	public int say_(Object... parameters) {
		try {
			if (parameters.length == 0) return 0;
			for (int i = 0; i < parameters.length; i++) {
				System.out.print(parameters[i]);
			}
			System.out.println();
//			String text = "";
//			if (parameters[0] instanceof IdentifierNode) {
//				text = gameContext.getTextIdentifierValue(((IdentifierNode) parameters[0]).getName(), 0);
//			} else if (parameters[0] instanceof TextElementNode) {
//				text = ((TextElementNode) parameters[0]).getText();
//			}
//
//			Integer qualifier = null;
//			if (parameters.length > 1) {
//				qualifier = parameters[1].evaluate(gameContext);
//			}
//			int newline = 1;
//			if (parameters.length > 2) {
//				newline = parameters[2].evaluate(gameContext);
//			}
//
//			String outputText = gameContext.expandText(text, qualifier);
//			if (newline != 0) {
//				System.out.println(outputText);
//			} else {
//				System.out.print(outputText);
//			}
		} catch (Exception e) {
			throw new GameRuntimeException("exception in 'say'", e);
		}

		return 0;
	}

//	@InternalFunction(name = "sayrandom")
//	public int sayRandom(ExprNode... parameters) {
//		try {
//			if (parameters.length == 0) return 0;
//
//			int dice = (int) (Math.random() * 100) + 1;
//			int prob = 0;
//			for (int i = 0; i < parameters.length - 1; i += 2) {
//				prob += parameters[i].evaluate(gameContext);
//				if (dice < prob) {
//					ExprNode parameter = parameters[i + 1];
//					String text = null;
//					if (parameter instanceof IdentifierNode) {
//						text = gameContext.getTextIdentifierValue(((IdentifierNode) parameter).getName(), 0);
//					} else if (parameter instanceof TextElementNode) {
//						text = ((TextElementNode) parameter).getText();
//					}
//					if (text != null) {
//						System.out.println(text);
//						break;
//					}
//				}
//			}
//		} catch (Exception e) {
//			throw new GameRuntimeException(parameters[0].getSourceLocation() + ": exception in 'sayrandom'", e);
//		}
//
//		throw new BreakException(ControlType.REPEAT);
//	}
//
//	@InternalFunction(name = "append")
//	public int append(ExprNode... text) {
//		// FIXME Implement this
//		// Not sure how to implement this, since we typically generate the newline after the text.
//		// Maybe need to generate it before. Not sure.
//		return 0;
//	}

	@InternalFunction(name = "quip")
	public int quip(Object... parameters) {
		say_(parameters);
		throw new BreakException(ControlType.REPEAT);
	}

//	@InternalFunction(name = "respond")
//	public int respond(ExprNode... parameters) {
//		if (anyof(parameters) != 0) {
//			quip(Arrays.copyOfRange(parameters, parameters.length - 1, parameters.length));
//		}
//		return 0;
//	}
//
//	@InternalFunction(name = "describe")
//	public int describe_(ExprNode... parameters) {
//		if (parameters.length == 0) return 0;
//		int var = parameters[0] instanceof IdentifierNode ?
//				gameContext.getIntIdentifierValue(((IdentifierNode) parameters[0]).getName()) :
//				parameters[0].evaluate(gameContext);
//		if (var >= gameContext.fvar && var < gameContext.lvar) {
//			var = gameContext.variables[var - gameContext.fvar];
//		}
//		System.out.println(gameContext.getTextIdentifierValue(var, parameters.length > 1 ? 1 : 0));
//		return 0;
//	}
//
//	@InternalFunction(name = "vocab")
//	public int vocab(ExprNode... text) {
//		int here = gameContext.getIntIdentifierValue("here");
//		for (String verb : gameContext.places[here - gameContext.floc].getCommands().keySet()) {
//			System.out.println(verb + " [here]");
//		}
//		for (String verb : gameContext.gameNode.getActions().keySet()) {
//			System.out.println(verb + " [action]");
//		}
//		int inhand = gameContext.getIntIdentifierValue("inhand");
//		for (int objRefno = gameContext.fobj; objRefno < gameContext.lobj; objRefno++) {
//			if (gameContext.locations[objRefno - gameContext.fobj] == inhand) {
//				ObjectNode objectNode = gameContext.objects[objRefno - gameContext.fobj];
//				for (String verb : objectNode.getCommands().keySet()) {
//					System.out.println(verb + " [" + objectNode.getName() + "]");
//				}
//			}
//		}
//		throw new BreakException(ControlType.REPEAT);
//	}
//
//	@InternalFunction(name = "tie")
//	public int tie(ExprNode... parameters) {
//		// FIXME Implement this
//		return 0;
//	}
//
//	@InternalFunction(name = "stop")
//	public int stop(ExprNode... parameters) {
//		System.exit(0);
//		return 0;
//	}
}
