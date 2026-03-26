package org.kathrynhuxtable.radiofreelawrence.game;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.kathrynhuxtable.gdesc.parser.GameInfo;
import org.kathrynhuxtable.gdesc.parser.InternalFunction;
import org.kathrynhuxtable.radiofreelawrence.game.exception.BreakException;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.ControlType;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.VocabularyNode;

public class InternalFunctions {

	private final GameContext gameContext;

	private Object game;
	private Set<String> noise;
	private Map<String, GamePlace> places;
	private Map<String, List<GameObject>> objects;
	private Map<String, Integer> variableFlags;

	private final Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);

	public InternalFunctions(GameContext gameContext) {
		this.gameContext = gameContext;
	}

	public void setGame(Object game) {
		this.game = game;
	}

	private int getIntVar(String name) {
		Class<?> gameClass = game.getClass();

		try {
			Field field = gameClass.getDeclaredField(name);
			return (int) field.get(game);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private void setIntVar(String name, int value) {
		Class<?> gameClass = game.getClass();

		try {
			Field field = gameClass.getDeclaredField(name);
			field.set(game, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private <T> T getObjectVar(String name) {
		Class<?> gameClass = game.getClass();

		try {
			Field field = gameClass.getDeclaredField(name);
			return (T) field.get(game);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private <T> void setObjectVar(String name, T value) {
		Class<?> gameClass = game.getClass();

		try {
			Field field = gameClass.getDeclaredField(name);
			field.set(game, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private <T> void putObjectVar(String name, T value) {
		Class<?> gameClass = game.getClass();

		try {
			Field field = gameClass.getDeclaredField(name);
			field.set(game, value);
		} catch (NoSuchFieldException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private Set<String> getNoise() {
		Class<?> gameClass = game.getClass();

		if (this.noise == null) {
			try {
				Field placesField = gameClass.getDeclaredField("noise");
				this.noise = (Set<String>) placesField.get(game);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return this.noise;
	}

	private Map<String, GamePlace> getPlaces() {
		Class<?> gameClass = game.getClass();

		if (this.places == null) {
			try {
				Field placesField = gameClass.getDeclaredField("places");
				this.places = (Map<String, GamePlace>) placesField.get(game);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return this.places;
	}

	private Map<String, List<GameObject>> getObjects() {
		Class<?> gameClass = game.getClass();

		if (this.places == null) {
			try {
				Field objectsField = gameClass.getDeclaredField("objects");
				this.objects = (Map<String, List<GameObject>>) objectsField.get(game);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return this.objects;
	}

	private Map<String, Integer> getVariableFlags() {
		Class<?> gameClass = game.getClass();
		if (this.variableFlags == null) {
			try {
				Field objectsField = gameClass.getDeclaredField("variableFlags");
				this.variableFlags = (Map<String, Integer>) objectsField.get(game);
			} catch (NoSuchFieldException | IllegalAccessException e) {
				throw new RuntimeException(e);
			}
		}
		return variableFlags;
	}

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

	private <T> T getGameVariable(String name) {
		Object gameVariable = null;
		try {
			gameVariable = internalFunctions.get(name);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		return (T) gameVariable;
	}

//	 Internal functions here.

	@InternalFunction(name = "input")
	public int input(Object... parameters) {
		setFlag("status", getIntVar("moved"));

		System.out.print("? ");
		String text = scanner.nextLine();

		parseInput(text);

		return 0;
	}

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

	@InternalFunction(name = "strcmp")
	public int strcmp(Object... parameters) {
		Object obj1 = parameters[0];
		Object obj2 = parameters[1];
		return obj1.toString().compareTo(obj2.toString());
	}

	@InternalFunction(name = "have")
	public int ishave(Object... parameters) {
		GamePlace inhand = getPlaces().get("inhand");
		Object obj = parameters[0];
		if (obj instanceof GameObject gameObject) {
			if (gameObject.getLocation() == inhand) {
				return 1;
			}
		}
		return 0;
	}

	@InternalFunction(name = "ishere")
	public int ishere(Object... parameters) {
		GamePlace here = getObjectVar("here");
		Object obj = parameters[0];
		if (obj instanceof GameObject gameObject) {
			if (gameObject.getLocation() == here) {
				return 1;
			}
		} else if (obj instanceof String name) {
			for (GameObject gameObject : getObjects().get(name)) {
				if (gameObject.getLocation() == here && gameObject.getName().equals(name)) {
					return 1;
				}
			}
		}
		return 0;
	}

	@InternalFunction(name = "isnear")
	public int isnear(Object... parameters) {
		return ishave(parameters) != 0 || ishere(parameters) != 0 ? 1 : 0;
	}

	@InternalFunction(name = "isat")
	public int isat(Object... parameters) {
		GamePlace here = getPlaces().get("here");
		for (Object node : parameters) {
			if (node instanceof GamePlace gamePlace && gamePlace == here) {
				return 1;
			}
		}
		return 0;
	}

	@InternalFunction(name = "atplace")
	public int atplace(Object... parameters) {
		GamePlace loc = getPlaces().get("here");
		for (int i = 1; i < parameters.length; i++) {
			GamePlace place = (GamePlace) parameters[i];
			if (place == loc) {
				return 1;
			}
		}
		return 0;
	}

	@InternalFunction(name = "varis")
	public int varis(Object... parameters) {
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
		return 0;
	}

	@InternalFunction(name = "key")
	public int iskey(Object... parameters) {
		String arg1 = getObjectVar("arg1");
		if (arg1 == null) {
			return 0;
		}
		for (Object parameter : parameters) {
			if (!arg1.equals(parameter)) {
				return 0;
			}
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
		}
		return 1;
	}

	@InternalFunction(name = "anyof")
	public int anyof(Object... parameters) {
//		Object verb = gameContext.getIntIdentifierValue("arg1");
//		for (Object parameter : parameters) {
//			int value = parameter.evaluate(gameContext);
//			if (value == verb) {
//				return 1;
//			}
//		}
		return 0;
	}

	@InternalFunction(name = "query")
	public int getquery(Object... parameters) {
		// FIXME Implement this
		return 0;
	}

	@InternalFunction(name = "usertyped")
	public int usertyped(Object... parameters) {
		// FIXME Implement this
		return 0;
	}

	@InternalFunction(name = "needcmd")
	public int needcmd(Object... parameters) {
		// FIXME Implement this
		return 0;
	}

	/*
	 * Abort the do-all loop if one executing and flush the command line buffer.
	 */
	@InternalFunction(name = "flush")
	public int flushinput(Object... parameters) {
		// FIXME Implement this
		return 0;
	}

	@InternalFunction(name = "apport")
	public int apport(Object... parameters) {
		GameObject object;
		if (parameters.length < 3 || (int) parameters[2] == 0) {
			object = (GameObject) parameters[0];
		} else {
			String name = (String) parameters[0];
			try {
				String className = GameContext.GAME_CLASS_NAME.replaceAll("/", ".") + "$" + name;
				Class<?> myClass = Class.forName(className);
				Constructor<?> constructor = myClass.getDeclaredConstructor(game.getClass());
				object = (GameObject) constructor.newInstance(game);
				Map<String, List<GameObject>> objects = getObjects();
				if (!objects.containsKey(name)) {
					objects.put(name, new ArrayList<>());
				}
				objects.get(name).add(object);
			} catch (ClassNotFoundException | NoSuchMethodException | InstantiationException | IllegalAccessException |
			         InvocationTargetException e) {
				throw new RuntimeException(e);
			}
		}
		GamePlace place = (GamePlace) parameters[1];
		object.setLocation(place);
		return 0;
	}

	@InternalFunction(name = "get")
	public int iget(Object... parameters) {
		Object object = parameters[0];
		if (object instanceof GameObject gameObject) {
			gameObject.setLocation(getPlaces().get("inhand"));
		} else if (object instanceof String name) {
			GamePlace here = getObjectVar("here");
			for (GameObject gameObject : getObjects().get(name)) {
				if (gameObject.getLocation() == here) {
					gameObject.setLocation(getPlaces().get("inhand"));
					break;
				}
			}
		}
		return 0;
	}

	@InternalFunction(name = "drop")
	public int idrop(Object... parameters) {
		GameObject object = (GameObject) parameters[0];
		if (object != null) {
			GamePlace place = (GamePlace) getObjectVar("here");
			object.setLocation(place);
		}
		return 0;
	}

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

	/// /		exprNodeList.add(objectNode);
//		exprNodeList.addAll(Arrays.asList(Arrays.copyOfRange(parameters, 2, parameters.length)));
//
//		gameContext.callFunction(identifierNode.getName(), exprNodeList);
//
//		throw new BreakException(ControlType.REPEAT);
//	}
	@InternalFunction(name = "goto")
	public int goto_(Object... parameters) {
		GamePlace place = (GamePlace) parameters[0];
		putObjectVar("there", getObjectVar("here"));
		putObjectVar("here", place);
		setFlag("status", getIntVar("moved"));
		return 0;
	}

	@InternalFunction(name = "move")
	public int move_(Object... parameters) {
		GamePlace place = (GamePlace) parameters[0];

		if (parameters.length > 1) {
			say_(Arrays.copyOfRange(parameters, 1, parameters.length));
		}

		putObjectVar("there", getObjectVar("here"));
		putObjectVar("here", place);
		setFlag("status", getIntVar("moved"));

		getObjects().values().stream()
				.flatMap(Collection::stream)
				.filter(obj -> obj.getLocation() == place)
				.peek(obj -> setFlag(obj, getIntVar("seen")));

		throw new BreakException(ControlType.REPEAT);
	}

	@InternalFunction(name = "smove")
	public int smove(Object... parameters) {
		// FIXME Implement this
		return 0;
	}

	@InternalFunction(name = "say")
	public int say_(Object... parameters) {
		try {
			if (parameters.length == 0) return 0;
			String text = "";
			if (parameters[0] instanceof Text textElement) {
				text = textElement.getText();
			} else if (parameters[0] instanceof String stringElement) {
				text = stringElement;
			} else if (parameters[0] instanceof GamePlace place) {
				text = place.getBriefDescription();
			} else if (parameters[0] instanceof GameObject object) {
				text = object.getBriefDescription();
			}

			Object qualifier = null;
			if (parameters.length > 1) {
				qualifier = parameters[1];
			}
			int newline = 1;
			if (parameters.length > 2) {
				newline = (int) parameters[2];
			}

			String outputText = expandText(text, qualifier);
			if (newline != 0) {
				System.out.println(outputText);
			} else {
				System.out.print(outputText);
			}
		} catch (Exception e) {
			throw new GameRuntimeException("exception in 'say'", e);
		}

		return 0;
	}

	@InternalFunction(name = "sayrandom")
	public int sayRandom(Object... parameters) {
		try {
			if (parameters.length == 0) return 0;

			int dice = (int) (Math.random() * 100) + 1;
			int prob = 0;
			for (int i = 0; i < parameters.length - 1; i += 2) {
				prob += (int) parameters[i];
				if (dice < prob) {
					String text = (String) parameters[i + 1];
					if (text != null) {
						System.out.println(text);
						break;
					}
				}
			}
		} catch (Exception e) {
			throw new GameRuntimeException(/*parameters[0].getSourceLocation() + */": exception in 'sayrandom'", e);
		}

		throw new BreakException(ControlType.REPEAT);
	}

	@InternalFunction(name = "append")
	public int append(Object... text) {
		// FIXME Implement this
		// Not sure how to implement this, since we typically generate the newline after the text.
		// Maybe need to generate it before. Not sure.
		return 0;
	}

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

	@InternalFunction(name = "describe")
	public int describe_(Object... parameters) {
		if (parameters.length == 0) return 0;
		Object var = parameters[0];
		if (var instanceof GamePlace place) {
			System.out.println(place.getLongDescription());
		} else if (var instanceof GameObject object) {
			System.out.println(object.getLongDescription());
		} else {
			System.out.println("I don't have a description for " + var);
		}
		return 0;
	}

	@InternalFunction(name = "vocab")
	public int vocab(Object... text) {
		say_("vocab not yet implemented");
//		GamePlace here = getPlaces().get("here");
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
		throw new BreakException(ControlType.REPEAT);
	}

	@InternalFunction(name = "tie")
	public int tie(Object... parameters) {
		// FIXME Implement this
		return 0;
	}

	@InternalFunction(name = "stop")
	public int stop(Object... parameters) {
		System.exit(0);
		return 0;
	}

	public String expandText(String text, Object qualifier) {
		StringBuilder result = new StringBuilder();
		char[] charArray = text.toCharArray();
		for (int i = 0; i < charArray.length; i++) {
			char c = charArray[i];
			if (c == '\\') {
				result.append(charArray[++i]);
			} else if (c == '$') {
				result.append((int) qualifier);
			} else if (c == '#') {
				if (qualifier != null) {
					result.append(qualifier);
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
				String identName = ident.toString().toLowerCase();
				Object identObj = getObjectVar(identName);
				result.append(expandText(identObj instanceof Text ? ((Text) identObj).getText() : (String) identObj, qualifier));
//				result.append(expandText(getTextIdentifierValue(identName, 0), qualifier));
			} else if (c == '[') {
				List<String> switches = new ArrayList<>();
				i = parseSwitches(charArray, i, switches);
				if (qualifier == null) {
					result.append(expandText(switches.get(0), null));
				} else if ((int) qualifier >= switches.size()) {
					result.append(expandText(switches.get(switches.size() - 1), qualifier));
				} else {
					result.append(expandText(switches.get((int) qualifier), qualifier));
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

	private void parseInput(String input) {
		String arg1 = null;
		String arg2 = null;
		int status = 0;

		String[] words = input.split("\\s+");
		int index = 1;
		for (String word : words) {
			if (!getNoise().contains(word)) {
				String arg;
				arg = word;
				if (gameContext.gameNode.getVerbs().containsKey(word)) {
					VocabularyNode wordNode = (VocabularyNode) gameContext.gameNode.getVerbs().get(word);
					arg = wordNode.getName();
					if (status >= 0) {
						status = index;
					}
				} else {
					List<String> possibleKeys = gameContext.gameNode.getVerbs().keySet().stream()
							.filter(v -> v.startsWith(word))
							.toList();
					if (possibleKeys.isEmpty()) {
						status = getIntVar("badword") | getIntVar("badsyntax");
					} else if (possibleKeys.size() > 1) {
						status = getIntVar("ambigword") | getIntVar("badsyntax");
					} else {
						VocabularyNode wordNode = (VocabularyNode) gameContext.gameNode.getVerbs().get(possibleKeys.get(0));
						arg = wordNode.getName();
						if (status >= 0) {
							status = index;
						}
					}
				}
				if (index == 1) {
					arg1 = arg;
				} else {
					arg2 = arg;
				}
				index++;
			}
			if (index > 2) {
				break;
			}
		}

		setObjectVar("arg1", arg1);
		setObjectVar("arg2", arg2);
		setIntVar("status", status);
	}

	private boolean testFlag(String flag, long state) {
		return (getVariableFlags().get(flag) | state) != 0;
	}

	public void setFlag(String flag, int state) {
		getVariableFlags().put(flag, getVariableFlags().get(flag) | state);
	}

	public void setFlag(GameFlag flagObj, int state) {
		flagObj.setFlags(flagObj.getFlags() | state);
	}

	public void clearFlag(String flag, int state) {
		getVariableFlags().put(flag, getVariableFlags().get(flag) & ~(1 << state));
	}

	public static Iterator<Object> iterator(Map<String, List<GameObject>> objects, GamePlace location) {
		return objects.values().stream()
				.flatMap(Collection::stream)
				.filter(o -> o.getLocation() == location)
				.map(go -> (Object) go)
				.iterator();
	}

	public static Iterator<String> iterator(GameObject object) {
		return object.getActions().iterator();
	}
}
