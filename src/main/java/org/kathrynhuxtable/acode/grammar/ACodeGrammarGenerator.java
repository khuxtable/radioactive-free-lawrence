package org.kathrynhuxtable.acode.grammar;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import org.kathrynhuxtable.acode.grammar.ACodeParser.InputContext;
import org.kathrynhuxtable.acode.grammar.CommandWord.CommandStatus;
import org.kathrynhuxtable.acode.grammar.tree.*;

public class ACodeGrammarGenerator {

	public static void main(String[] args) throws Exception {
		ACodeGrammarGenerator generator = new ACodeGrammarGenerator();

		generator.generate();

		System.exit(0);
	}

	InputNode inputNode;
	int refno;
	int fobj;
	int lobj;
	int floc;
	int lloc;
	int fverb;
	int lverb;
	int fvar;
	int lvar;
	int ftext;
	int ltext;
	Map<String, Integer> states = new HashMap<>();

	public void generate() throws Exception {
		ErrorReporter errorReporter = new ErrorReporter();
		inputNode = parseFile(new InputNode(), errorReporter, "main.acd", false);

		createDefaultElements();
		assignRefnos();
		processStateValues();

		if (errorReporter.hasErrors()) {
			for (String error : errorReporter.getErrors()) {
				System.err.println(error);
			}
			System.exit(1);
		}

		ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
//		System.out.println(objectMapper.writeValueAsString(inputNode));
//
//		System.out.println("lobj = " + lobj);
//		System.out.println("states:");
//		System.out.println(objectMapper.writeValueAsString(states));

		System.out.println("Welcome to " + inputNode.getName());
		System.out.println("   Version " + inputNode.getVersion());
		System.out.println("Created by " + inputNode.getAuthor());
		System.out.println("        on " + inputNode.getDate());

		Scanner scanner = new Scanner(System.in, "UTF-8");
		while (true) {
			System.out.print("> ");
			String text = scanner.next();
			if ("quit".equals(text)) {
				break;
			}
			List<CommandWord> words = parseInput(text);
			System.out.println(objectMapper.writeValueAsString(words));
		}
	}

	private void createDefaultElements() {
	}

	private void assignRefnos() {
		/* objects, places, verbs, variables, text */
		refno = 1;
		fobj = refno;
		for (ObjectNode obj : inputNode.getObjects()) {
			obj.setRefno(refno++);
		}
		lobj = refno - 1;
		floc = refno;
		for (PlaceNode place : inputNode.getPlaces()) {
			place.setRefno(refno++);
		}
		lloc = refno - 1;
		fverb = refno;
		for (AcdNode vocabulary : inputNode.getVerbs().values()) {
			if (vocabulary instanceof VerbNode) {
				((VerbNode) vocabulary).setRefno(refno++);
			}
		}
		lverb = refno - 1;
		fvar = refno;
		for (SingleVariableNode var : inputNode.getVariables()) {
			var.setRefno(refno++);
		}
		lvar = refno - 1;
		ftext = refno;
		for (TextNode text : inputNode.getTexts()) {
			text.setRefno(refno++);
		}
		ltext = refno - 1;
	}

	private void processStateValues() {
		// Resolve the state values
		states = new HashMap<>();
		int nextStateValue = 0;
		for (Map.Entry<String, StateClauseNode> entry : inputNode.getStates().entrySet()) {
			if (entry.getValue().getValue() == null) {
				states.put(entry.getKey(), nextStateValue++);
			} else {
				nextStateValue = evaluateExpression(entry.getValue().getValue());
				states.put(entry.getKey(), nextStateValue++);
			}
		}
	}

	public static InputNode parseFile(InputNode inputNode, ErrorReporter errorReporter, String filePath, boolean optional) throws IOException {
		try (InputStream inputStream = ACodeGrammarGenerator.class.getResourceAsStream("/" + filePath)) {
			if (inputStream != null) {
				CharStream in = CharStreams.fromStream(inputStream);
				ACodeLexer lexer = new ACodeLexer(in);
				CommonTokenStream tokens = new CommonTokenStream(lexer);
				ACodeParser parser = new ACodeParser(tokens);
				parser.setBuildParseTree(true);
				InputContext inputContext = parser.input();
				new ACodeAstVisitor(inputNode, errorReporter).visit(inputContext);
				return inputNode;
			} else if (optional) {
				return inputNode;
			} else {
				throw new IOException("Cannot find resource " + filePath);
			}
		}
	}

	public int evaluateExpression(ExprNode expression) {
		switch (expression.getOperator()) {
		case LEAF:
			if (expression.getValue().matches("[0-9]+")) {
				return Integer.parseInt(expression.getValue());
			} else {
				AcdNode n = inputNode.getIdentifiers().get(expression.getValue());
				if (n instanceof ObjectNode) {
					return ((ObjectNode) inputNode.getIdentifiers().get(expression.getValue())).getRefno();
				} else if (n instanceof PlaceNode) {
					return ((PlaceNode) inputNode.getIdentifiers().get(expression.getValue())).getRefno();
				} else if (n instanceof VerbNode) {
					return ((VerbNode) inputNode.getIdentifiers().get(expression.getValue())).getRefno();
				} else if (n instanceof SingleVariableNode) {
					return ((SingleVariableNode) inputNode.getIdentifiers().get(expression.getValue())).getRefno();
				} else if (n instanceof TextNode) {
					return ((TextNode) inputNode.getIdentifiers().get(expression.getValue())).getRefno();
				} else if (n instanceof StateClauseNode) {
					return states.get(((StateClauseNode) n).getState());
				} else {
					return -1;
				}
			}
		case PLUS:
			return evaluateExpression(expression.getLeft()) + evaluateExpression(expression.getRight());
		case MINUS:
			return evaluateExpression(expression.getLeft()) - evaluateExpression(expression.getRight());
		case TIMES:
			return evaluateExpression(expression.getLeft()) * evaluateExpression(expression.getRight());
		case DIVIDE:
			return evaluateExpression(expression.getLeft()) / evaluateExpression(expression.getRight());
		case UNARY_PLUS:
			return evaluateExpression(expression.getLeft());
		case UNARY_MINUS:
			return -evaluateExpression(expression.getLeft());
		default:
			return 0;
		}
	}

	public List<CommandWord> parseInput(String input) {
		List<CommandWord> nodes = new ArrayList<>();

		String[] words = input.split("\\s+");
		for (String word : words) {
			if (!inputNode.getNoise().contains(word)) {
				List<String> possibleKeys = inputNode.getVerbs().keySet().stream()
						.filter(v -> v.startsWith(word))
						.collect(Collectors.toList());
				if (possibleKeys.isEmpty()) {
					nodes.add(new CommandWord(word, new ArrayList<>(), CommandStatus.NOT_FOUND));
				} else if (possibleKeys.size() > 1) {
					List<AcdNode> values = inputNode.getVerbs().entrySet().stream()
							.filter(e -> possibleKeys.contains(e.getKey()))
							.map(Entry::getValue)
							.collect(Collectors.toList());
					nodes.add(new CommandWord(word, values, CommandStatus.AMBIGUOUS));
				} else {
					List<AcdNode> value = Collections.singletonList(inputNode.getVerbs().get(possibleKeys.get(0)));
					nodes.add(new CommandWord(word, value, CommandStatus.GOOD));
				}
			}
		}

		return nodes;
	}
}
