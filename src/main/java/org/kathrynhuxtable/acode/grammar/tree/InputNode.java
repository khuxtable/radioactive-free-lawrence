package org.kathrynhuxtable.acode.grammar.tree;

import java.util.*;

import lombok.Data;

@Data
public class InputNode implements AcdNode {
	private String name;
	private String version;
	private String date;
	private String author;
	private int style;
	private boolean utf8;

	Map<String, AcdNode> verbs =  new HashMap<>();
	Map<String, AcdNode> identifiers =  new HashMap<>();
	List<String> noise = new ArrayList<>();
	List<SingleVariableNode> variables =  new ArrayList<>();
	List<FlagNode> flags =  new ArrayList<>();
	List<ArrayNode> arrays =  new ArrayList<>();
	List<ObjectNode> objects =  new ArrayList<>();
	List<PlaceNode> places =  new ArrayList<>();
	List<TextNode> texts =  new ArrayList<>();
	List<AtNode> ats =  new ArrayList<>();
	List<ActionNode> actions =  new ArrayList<>();
	List<ProcNode> procs =  new ArrayList<>();
	List<InitialNode> inits =  new ArrayList<>();
	List<RepeatNode> repeats =  new ArrayList<>();
	Map<String, StateClauseNode> states = new LinkedHashMap<>();
}
