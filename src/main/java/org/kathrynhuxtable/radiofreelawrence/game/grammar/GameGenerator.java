package org.kathrynhuxtable.radiofreelawrence.game.grammar;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import org.kathrynhuxtable.radiofreelawrence.game.GameData;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.*;

@Data
@Component
@RequiredArgsConstructor
public class GameGenerator {

	private final GameData gameData;

	public void generate() throws Exception {
		createDefaultElements();

		GameVisitor visitor = new GameVisitor(gameData.gameNode, gameData.errorReporter);
		visitor.readFile("main.gdesc", false);

		assignRefnos();
		processStateValues();
		setupArrays();

		if (!gameData.errorReporter.getErrors().isEmpty()) {
			for (String error : gameData.errorReporter.getErrors()) {
				System.err.println(error);
			}
			System.exit(1);
		}

		ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
//		System.out.println(objectMapper.writeValueAsString(gameData.gameNode));
//		System.out.println("lobj = " + gameData.lobj);
//		System.out.println("states:");
//		System.out.println(objectMapper.writeValueAsString(gameData.states));

		System.out.println("Welcome to " + gameData.gameNode.getName());
		System.out.println("   Version " + gameData.gameNode.getVersion());
		System.out.println("Created by " + gameData.gameNode.getAuthor());
		System.out.println("        on " + gameData.gameNode.getDate());
	}

	private void createDefaultElements() {
		createState("badword", -2);
		createState("ambigword", -3);
		createState("badsyntax", -1);

		createVariable("arg1");
		createVariable("arg2");
		createVariable("status");
		createVariable("here");
		createVariable("there");

		createPlace("inhand", false, "inventory", "Inventory");
		createPlace("ylem", false, "ylem", "Ylem");
	}

	private void createState(String name, int value) {
		StateClauseNode node = new StateClauseNode(name, NumberLiteralNode.builder().number(value).build());
		gameData.gameNode.getStates().put(name, node);
		gameData.gameNode.getIdentifiers().put(name, node);
	}

	private void createVariable(String name) {
		VariableNode node = VariableNode.builder().variable(name).build();
		gameData.gameNode.getVariables().add(node);
		gameData.gameNode.getIdentifiers().put(name, node);
	}

	private void createPlace(String name, boolean inVocabulary, String briefDescription, String longDescription) {
		PlaceNode node = PlaceNode.builder()
				.names(Collections.singletonList(name))
				.inVocabulary(inVocabulary)
				.briefDescription(briefDescription)
				.longDescription(longDescription)
				.build();
		if (node.isInVocabulary()) {
			// Add first word to vocabulary.
			gameData.gameNode.getVerbs().put(node.getNames().get(0), node);
		}
		for (String var : node.getNames()) {
			gameData.gameNode.getIdentifiers().put(var, node);
		}
		gameData.gameNode.getPlaces().add(node);
	}

	private void assignRefnos() {
		/* objects, places, verbs, variables, text */
		gameData.refno = 1;
		gameData.fobj = gameData.refno;
		gameData.objects = new ObjectNode[gameData.gameNode.getObjects().size()];
		gameData.objectFlags = new long[gameData.gameNode.getObjects().size()];
		for (ObjectNode obj : gameData.gameNode.getObjects()) {
			gameData.objects[gameData.refno - gameData.fobj] = obj;
			obj.setRefno(gameData.refno++);
		}
		gameData.lobj = gameData.refno;

		gameData.floc = gameData.refno;
		gameData.places = new PlaceNode[gameData.gameNode.getPlaces().size()];
		gameData.placeFlags = new long[gameData.gameNode.getPlaces().size()];
		for (PlaceNode place : gameData.gameNode.getPlaces()) {
			gameData.places[gameData.refno - gameData.floc] = place;
			place.setRefno(gameData.refno++);
		}
		gameData.lloc = gameData.refno;

		gameData.fvar = gameData.refno;
		gameData.variables = new int[gameData.gameNode.getVariables().size()];
		gameData.variableFlags = new long[gameData.gameNode.getVariables().size()];
		for (VariableNode var : gameData.gameNode.getVariables()) {
			gameData.variables[gameData.refno - gameData.fvar] = 0;
			var.setRefno(gameData.refno++);
		}
		gameData.lvar = gameData.refno;

		gameData.ftext = gameData.refno;
		gameData.texts = new TextNode[gameData.gameNode.getTexts().size()];
		for (TextNode text : gameData.gameNode.getTexts()) {
			gameData.texts[gameData.refno - gameData.ftext] = text;
			text.setRefno(gameData.refno++);
		}
		gameData.ltext = gameData.refno;

		gameData.fverb = gameData.refno;
		int verbSize = (int) gameData.gameNode.getVerbs().values().stream()
				.filter(v -> v instanceof VerbNode)
				.count();
		gameData.verbs = new BaseNode[verbSize];
		for (BaseNode vocabulary : gameData.gameNode.getVerbs().values()) {
			if (vocabulary instanceof VerbNode) {
				gameData.verbs[gameData.refno - gameData.fverb] = vocabulary;
				((VerbNode) vocabulary).setRefno(gameData.refno++);
			}
		}
		gameData.lverb = gameData.refno;

		// Assign initial locations to ylem
		gameData.locations = new int[gameData.gameNode.getObjects().size()];
		for (ObjectNode obj : gameData.gameNode.getObjects()) {
			gameData.locations[obj.getRefno() - gameData.fobj] = gameData.floc + 1;
		}

	}

	private void processStateValues() {
		// Resolve the state values
		gameData.states = new HashMap<>();
		int nextStateValue = 0;
		for (Map.Entry<String, StateClauseNode> entry : gameData.gameNode.getStates().entrySet()) {
			if (entry.getValue().getValue() == null) {
				gameData.states.put(entry.getKey(), nextStateValue++);
			} else {
				nextStateValue = entry.getValue().getValue().evaluate(gameData);
				gameData.states.put(entry.getKey(), nextStateValue++);
			}
		}
	}

	private void setupArrays() {
		for (ArrayNode array : gameData.gameNode.getArrays()) {
			gameData.arrays.put(array.getName(), new int[array.getSize()]);
		}
	}
}
