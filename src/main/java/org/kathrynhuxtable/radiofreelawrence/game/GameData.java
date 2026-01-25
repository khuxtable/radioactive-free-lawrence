package org.kathrynhuxtable.radiofreelawrence.game;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import org.springframework.stereotype.Component;

import org.kathrynhuxtable.radiofreelawrence.game.exception.BreakException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.ContinueException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.LoopControlException.ControlType;
import org.kathrynhuxtable.radiofreelawrence.game.exception.ReturnException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.ErrorReporter;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.*;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.ActionNode.ActionCode;

@Data
@Component
public class GameData {

	public enum IdentifierType {OBJECT, PLACE, VARIABLE, TEXT, VERB}

	// Static data

	public final Input input = new Input(this);
	public final InternalFunctions internalFunctions = new InternalFunctions(this);

	public final GameNode gameNode = new GameNode();
	public ErrorReporter errorReporter = new ErrorReporter();

	public int refno;
	public int fobj;
	public int lobj;
	public int floc;
	public int lloc;
	public int fvar;
	public int lvar;
	public int ftext;
	public int ltext;
	public int fverb;
	public int lverb;
	public ObjectNode[] objects;
	public PlaceNode[] places;
	public VariableNode[] vars;
	public TextNode[] texts;
	public VocabularyNode[] verbs;

	// Mutable data

	public Map<String, Integer> states = new HashMap<>();
	public int[] variables;
	public int[] locations;
	public Map<String, int[]> arrays = new HashMap<>();
	public long[] objectFlags;
	public long[] placeFlags;
	public long[] variableFlags;
	public LocalVariables localVariables = new LocalVariables();

	// Utility methods

	public IdentifierType getIdentifierType(String identifier) {
		return getRefnoType(getIdentifierRefno(identifier));
	}

	public IdentifierType getRefnoType(int refno) {
		if (refno < fobj) {
			return null;
		} else if (refno < lobj) {
			return IdentifierType.OBJECT;
		} else if (refno < lloc) {
			return IdentifierType.PLACE;
		} else if (refno < lvar) {
			return IdentifierType.VARIABLE;
		} else if (refno < ltext) {
			return IdentifierType.TEXT;
		} else if (refno < lverb) {
			return IdentifierType.VERB;
		} else {
			return null;
		}
	}

	public BaseNode getRefnoNode(int refno) {
		if (refno < fobj) {
			return null;
		} else if (refno < lobj) {
			return objects[refno - fobj];
		} else if (refno < lloc) {
			return places[refno - floc];
		} else if (refno < lvar) {
			return vars[refno - fvar];
		} else if (refno < ltext) {
			return texts[refno - ftext];
		} else if (refno < lverb) {
			return (BaseNode) verbs[refno - fverb];
		} else {
			return null;
		}
	}

	public int getIdentifierRefno(String identifier) {
		BaseNode local = localVariables.getLocalVariableValue(identifier.toLowerCase());
		if (local != null) {
			return 0;
		}

		BaseNode idNode = gameNode.getIdentifiers().get(identifier.toLowerCase());
		if (idNode instanceof HasRefno hasRefno) {
			return hasRefno.getRefno();
		} else {
			return 0;
		}
	}

	public String getTextIdentifierValue(String identifier, int type) {
		BaseNode local = localVariables.getLocalVariableValue(identifier.toLowerCase());
		if (local != null) {
			if (local instanceof TextElementNode textElementNode) {
				return getTextIdentifierValue(textElementNode.getRefno(), 0);
			} else if (local instanceof NumberLiteralNode numberLiteralNode) {
				return getTextIdentifierValue(numberLiteralNode.getNumber(), 0);
			} else {
				throw new GameRuntimeException(local.getSourceLocation() + ": invalid type for local variable " + identifier);
			}
		}

		// FIXME Need to handle the cyclic, random, etc. options.
		BaseNode idNode = gameNode.getIdentifiers().get(identifier.toLowerCase());
		if (idNode instanceof HasRefno hasRefno) {
			return getTextIdentifierValue(hasRefno.getRefno(), type);
		} else if (idNode instanceof StateClauseNode stateClauseNode) {
			return Integer.toString(states.get(stateClauseNode.getState()));
		} else if (idNode instanceof FlagNode flagNode) {
			return Integer.toString(flagNode.getFlags().indexOf(identifier));
		} else {
			return "???";
		}
	}

	public String getTextIdentifierValue(int refno, int type) {
		// FIXME Need to handle the cyclic, random, etc. options.
		if (refno < fobj) {
			throw new GameRuntimeException("refno " + refno + " is not valid");
		} else if (refno < lobj) {
			ObjectNode objectNode = objects[refno - fobj];
			if (type < 0) {
				return objectNode.getName();
			}
			boolean inHand = locations[objectNode.getRefno() - fobj] == floc;
			int seenFlag = getIntIdentifierValue("seen");
			boolean seen = testflag(objectNode.getRefno(), seenFlag);
			if (inHand) {
				return objectNode.getInventoryDescription();
			} else {
				return (!seen || type > 0) && objectNode.getLongDescription() != null ? objectNode.getLongDescription() : objectNode.getBriefDescription();
			}
		} else if (refno < lloc) {
			PlaceNode placeNode = places[refno - floc];
			if (type < 0) {
				return placeNode.getName();
			}
			int beenHereFlag = getIntIdentifierValue("beenHere");
			boolean beenHere = testflag(refno, beenHereFlag);
			setFlag(refno, beenHereFlag);
			return (!beenHere || type > 0) && placeNode.getLongDescription() != null ? placeNode.getLongDescription() : placeNode.getBriefDescription();
		} else if (refno < lvar) {
			int var = variables[refno - fvar];
			return Integer.toString(var);
		} else if (refno < ltext) {
			TextNode text = texts[refno - ftext];
			return text.getTexts().get(0);
		} else if (refno < lverb) {
			VocabularyNode verb = verbs[refno - fverb];
			return verb.getName();
		} else {
			throw new GameRuntimeException("refno " + refno + " is not valid");
		}
	}

	public String expandText(String text, Integer qualifier) {
		StringBuilder result = new StringBuilder();
		char[] charArray = text.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (c == '\\') {
				result.append(charArray[++i]);
			} else if (c == '$') {
				result.append(qualifier);
			} else if (c == '#') {
				if (qualifier != null) {
					result.append(getTextIdentifierValue(qualifier, -1));
				} else {
					result.append('#');
				}
			} else if (c == '{') {
				StringBuilder ident = new StringBuilder();
				while (++i < charArray.length) {
					c = charArray[i];
					if (c == '\\') {
						ident.append(charArray[++i]);
					} else if (c == '}') {
						break;
					} else {
						ident.append(c);
					}
				}
				result.append(expandText(getTextIdentifierValue(ident.toString().toLowerCase(), 0), qualifier));
			} else if (c == '[') {
				List<String> switches = new ArrayList<>();
				i = parseSwitches(charArray, i, switches);
				if (qualifier == null) {
					result.append(expandText(switches.get(0), null));
				} else if (qualifier >= switches.size()) {
					result.append(expandText(switches.get(switches.size() - 1), qualifier));
				} else {
					result.append(expandText(switches.get(qualifier), qualifier));
				}
			} else {
				result.append(c);
			}
		}
		return result.toString();
	}

	private static int parseSwitches(char[] charArray, int i, List<String> switches) {
		StringBuilder switchText = new StringBuilder(100);
		char c;
		while (++i < charArray.length) {
			c = charArray[i];
			if (c == '\\') {
				switchText.append(charArray[++i]);
			} else if (c == '/') {
				if ("=".contentEquals(switchText)) {
					switches.add(switches.get(switches.size() - 1));
				} else {
					switches.add(switchText.toString());
				}
				switchText = new StringBuilder(100);
			} else if (c == ']') {
				if ("=".contentEquals(switchText)) {
					switches.add(switches.get(switches.size() - 1));
				} else {
					switches.add(switchText.toString());
				}
				return i;
			} else {
				switchText.append(c);
			}
		}
		throw new GameRuntimeException("Missing ']' in switch text in \"" + new String(charArray) + "\"");
	}

	public int getIntIdentifierValue(String identifier) {
		BaseNode local = localVariables.getLocalVariableValue(identifier.toLowerCase());
		if (local != null) {
			if (local instanceof TextElementNode textElementNode) {
				return textElementNode.getRefno();
			} else if (local instanceof NumberLiteralNode numberLiteralNode) {
				return numberLiteralNode.getNumber();
			} else {
				throw new GameRuntimeException(local.getSourceLocation() + ": Invalid type for local variable " + identifier);
			}
		}

		BaseNode idNode = gameNode.getIdentifiers().get(identifier.toLowerCase());
		if (idNode instanceof HasRefno hasRefno) {
			return getIntIdentifierValue(hasRefno.getRefno());
		} else if (idNode instanceof StateClauseNode) {
			return states.get(identifier);
		} else if (idNode instanceof FlagNode flagNode) {
			return flagNode.getFlags().indexOf(identifier);
		} else {
			return 0;
		}
	}

	public int getIntIdentifierValue(int refno) {
		if (refno < fobj) {
			throw new GameRuntimeException("refno " + refno + " is not valid");
		} else if (refno < lobj) {
			return refno;
		} else if (refno < lloc) {
			return refno;
		} else if (refno < lvar) {
			return variables[refno - fvar];
		} else if (refno < ltext) {
			return refno;
		} else if (refno < lverb) {
			return refno;
		} else {
			throw new GameRuntimeException("refno " + refno + " is not valid");
		}
	}

	public void setIntIdentifierValue(String identifier, int value) {
		if (localVariables.setLocalVariableValue(identifier.toLowerCase(), value)) {
			return;
		}

		BaseNode idNode = gameNode.getIdentifiers().get(identifier.toLowerCase());
		if (idNode instanceof VariableNode) {
			variables[((VariableNode) idNode).getRefno() - fvar] = value;
		} else {
			throw new GameRuntimeException("Cannot set identifier " + identifier);
		}
	}

	public int getArrayValue(String identifier, int index) {
		return arrays.get(identifier.toLowerCase())[index];
	}

	public void setArrayValue(String identifier, int index, int value) {
		arrays.get(identifier.toLowerCase())[index] = value;
	}

	public void setLeftHandSide(ExprNode leftHandSide, int value) {
		if (leftHandSide instanceof IdentifierNode identifierNode) {
			setIntIdentifierValue(identifierNode.getName(), value);
		} else if (leftHandSide instanceof ArrayAccessNode arrayAccessNode) {
			setArrayValue(arrayAccessNode.getArrayName(), arrayAccessNode.getIndex().evaluate(this), value);
		} else if (leftHandSide instanceof DerefNode derefNode) {
			int refno = getIntIdentifierValue(derefNode.getIdentifier().getName());
			if (refno < fvar || refno >= lvar) {
				throw new GameRuntimeException("Cannot set left hand side: " + refno + " is not a variable refno");
			}
			variables[refno - fvar] = value;
		} else {
			throw new GameRuntimeException("unexpected left hand side in array access");
		}
	}

	public boolean testFlag(ExprNode flagVar, long state) {
		int refno;
		if (flagVar instanceof IdentifierNode) {
			refno = getIdentifierRefno(((IdentifierNode) flagVar).getName().toLowerCase());
		} else {
			refno = flagVar.evaluate(this);
		}
		return testflag(refno, state);
	}

	private boolean testflag(int refno, long state) {
		long value;
		if (refno >= fvar && refno < lvar) {
			value = variableFlags[refno - fvar];
		} else if (refno >= floc && refno < lloc) {
			value = placeFlags[refno - floc];
		} else if (refno >= fobj && refno < lobj) {
			value = objectFlags[refno - fobj];
		} else {
			return false;
		}

		return (value & (1L << state)) != 0;
	}

	public void setFlag(int refno, long state) {
		if (refno >= fvar && refno < lvar) {
			variableFlags[refno - fvar] |= 1L << state;
		} else if (refno >= floc && refno < lloc) {
			placeFlags[refno - floc] |= 1L << state;
		} else if (refno >= fobj && refno < lobj) {
			objectFlags[refno - fobj] |= 1L << state;
		}
	}

	public void clearFlag(int refno, long state) {
		if (refno >= fvar && refno < lvar) {
			variableFlags[refno - fvar] &= ~(1L << state);
		} else if (refno >= floc && refno < lloc) {
			placeFlags[refno - floc] &= ~(1L << state);
		} else if (refno >= fobj && refno < lobj) {
			objectFlags[refno - fobj] &= ~(1L << state);
		}
	}

	public void callInits() {
		List<InitialNode> inits = gameNode.getInits();
		for (InitialNode init : inits) {
			try {
				localVariables.newFunctionScope();
				init.getCode().execute(this);
			} catch (BreakException | ContinueException e) {
				// Implicit continue
			} catch (ReturnException e) {
				break;
			} finally {
				localVariables.closeFunctionScope();
			}
		}
	}

	public void callRepeats() {
		List<RepeatNode> repeats = gameNode.getRepeats();
		for (; ; ) {
			for (RepeatNode repeat : repeats) {
				try {
					localVariables.newFunctionScope();
					repeat.getCode().execute(this);
				} catch (BreakException e) {
					if (e.getControlType() == ControlType.REPEAT) {
						break;
					}
				} catch (ContinueException | ReturnException e) {
					// Restart from the beginning
					break;
				} finally {
					localVariables.closeFunctionScope();
				}
			}
		}
	}

	public int callFunction(String name, List<ExprNode> parameters) {
		Method method = internalFunctions.getInternalFunction(name.toLowerCase());
		if (method != null) {
			try {
				Integer result = (Integer) method.invoke(internalFunctions, (Object) parameters.toArray(new ExprNode[0]));
				if (result != null) {
					return result;
				} else {
					return 0;
				}
			} catch (IllegalAccessException | InvocationTargetException e) {
				if (e.getCause() instanceof ReturnException) {
					throw ((ReturnException) e.getCause());
				} else if (e.getCause() instanceof BreakException) {
					throw (BreakException) e.getCause();
				} else if (e.getCause() instanceof ContinueException) {
					throw (ContinueException) e.getCause();
				}
				throw new GameRuntimeException("Error calling internal function " + method.getName(), e);
			}
		} else {
			BaseNode idNode;
			BaseNode localNode = localVariables.getLocalVariableValue(name.toLowerCase());
			Integer local = null;
			if (localNode != null) {
				if (localNode instanceof TextElementNode textElementNode) {
					local = textElementNode.getRefno();
				} else if (localNode instanceof NumberLiteralNode numberLiteralNode) {
					local = numberLiteralNode.getNumber();
				} else {
					throw new GameRuntimeException(localNode.getSourceLocation() + ": Invalid internal function in local variable " + name.toLowerCase());
				}
			}
			if (local != null && local >= fobj && local < lobj) {
				idNode = objects[local - fobj];
			} else {
				idNode = gameNode.getIdentifiers().get(name.toLowerCase());
				if (idNode instanceof VariableNode variableNode) {
					int refno = variables[variableNode.getRefno() - fvar];
					if (refno < floc) {
						// No valid function reference
						return 0;
					} else if (refno <= lloc) {
						idNode = places[refno - floc];
					} else if (refno <= lverb) {
						idNode = (BaseNode) verbs[refno - fverb];
					} else {
						// No valid function reference
						return 0;
					}
				}
			}
			if (idNode instanceof ProcNode procNode) {
				return callProc(procNode, parameters);
			} else if (idNode instanceof PlaceNode placeNode) {
				callPlace(placeNode);
				return 0;
			} else if (idNode instanceof ObjectNode objectNode) {
				callObject(objectNode);
				return 0;
			} else if (idNode instanceof VerbNode verbNode) {
				callAction(verbNode);
				return 0;
			} else {
				throw new GameRuntimeException("variable " + name + " does not contain a valid function reference");
			}
		}
	}

	private void callPlace(PlaceNode placeNode) {
		int refno = getIntIdentifierValue("arg1");
		if (refno == 0) {
			return;
		}
		BaseNode wordNode = getRefnoNode(refno);
		if (wordNode == null) {
			return;
		}
		String verb = null;
		if (wordNode instanceof VerbNode verbNode) {
			verb = verbNode.getName();
		}
		if (verb != null && placeNode != null && placeNode.getCommands().get(verb) != null) {
			localVariables.newFunctionScope();

			localVariables.addVariable(
					"this",
					NumberLiteralNode.builder()
							.number(placeNode.getRefno())
							.sourceLocation(placeNode.getSourceLocation())
							.build());

			try {
				placeNode.getCommands().get(verb).execute(this);
			} catch (BreakException | ContinueException e) {
				if (e.getControlType() == ControlType.REPEAT) {
					throw e;
				}
				// Implicit continue
			} catch (ReturnException e) {
				// Fall through
			} finally {
				localVariables.closeFunctionScope();
			}
		}
	}

	private void callObject(ObjectNode objectNode) {
		int refno = getIntIdentifierValue("arg1");
		if (refno == 0) {
			return;
		}
		BaseNode wordNode = getRefnoNode(refno);
		if (wordNode == null) {
			return;
		}
		String verb = null;
		if (wordNode instanceof VerbNode verbNode) {
			verb = verbNode.getName();
		}
		if (objectNode != null && objectNode.getCommands().get(verb) != null) {
			localVariables.newFunctionScope();

			localVariables.addVariable(
					"this",
					NumberLiteralNode.builder()
							.number(objectNode.getRefno())
							.sourceLocation(objectNode.getSourceLocation())
							.build());

			try {
				objectNode.getCommands().get(verb).execute(this);
			} catch (BreakException | ContinueException e) {
				if (e.getControlType() == ControlType.REPEAT) {
					throw e;
				}
				// Implicit continue
			} catch (ReturnException e) {
				// Fall through
			} finally {
				localVariables.closeFunctionScope();
			}
		}
	}

	public void callAction(VerbNode idNode) {
		ActionNode node = gameNode.getActions().get(idNode.getVerbs().get(0));
		if (node != null) {
			for (ActionCode actionCode : node.getActionCodes()) {
				if (actionCode.getArg2() != null) {
					int arg2refno = getIntIdentifierValue(actionCode.getArg2());
					int arg2 = getIntIdentifierValue("arg2");
					if (arg2refno != arg2) {
						continue;
					}
				}
				try {
					actionCode.getCode().execute(this);
				} catch (BreakException | ContinueException e) {
					if (e.getControlType() == ControlType.REPEAT) {
						throw e;
					}
					// Implicit continue
				} catch (ReturnException e) {
					break;
				}
			}
		}
	}

	private int callProc(ProcNode procNode, List<ExprNode> parameters) {
		List<ExprNode> evalParameters = new ArrayList<>();
		for (ExprNode parameter : parameters) {
			if (parameter instanceof TextElementNode textElementNode) {
				evalParameters.add(textElementNode);
			} else {
				evalParameters.add(NumberLiteralNode.builder()
						.number(parameter.evaluate(this))
						.sourceLocation(parameter.getSourceLocation())
						.build());
			}
		}

		localVariables.newFunctionScope();

		List<String> args = procNode.getArgs();
		for (int i = 0; i < args.size(); i++) {
			if (i < evalParameters.size()) {
				localVariables.addVariable(
						args.get(i),
						evalParameters.get(i));
			}
		}

		try {
			procNode.getCode().execute(this);
			return 0;
		} catch (BreakException | ContinueException e) {
			if (e.getControlType() == ControlType.REPEAT) {
				throw e;
			}
			return 0;
		} catch (ReturnException e) {
			return e.getValue();
		} finally {
			localVariables.closeFunctionScope();
		}
	}
}
