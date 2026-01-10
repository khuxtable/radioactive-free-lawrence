package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.*;

import lombok.Data;

@Data
public class GameNode implements BaseNode {
	private String name;
	private String version;
	private String date;
	private String author;

	Map<String, VocabularyNode> verbs =  new LinkedHashMap<>();
	Map<String, BaseNode> identifiers =  new LinkedHashMap<>();
	List<String> noise = new ArrayList<>();
	List<VariableNode> variables =  new ArrayList<>();
	List<FlagNode> flags =  new ArrayList<>();
	List<ArrayNode> arrays =  new ArrayList<>();
	List<ObjectNode> objects =  new ArrayList<>();
	List<PlaceNode> places =  new ArrayList<>();
	List<TextNode> texts =  new ArrayList<>();
	Map<String, ActionNode> actions =  new LinkedHashMap<>();
	Map<String, ProcNode> procs =  new LinkedHashMap<>();
	List<InitialNode> inits =  new ArrayList<>();
	List<RepeatNode> repeats =  new ArrayList<>();
	Map<String, StateClauseNode> states = new LinkedHashMap<>();
}
