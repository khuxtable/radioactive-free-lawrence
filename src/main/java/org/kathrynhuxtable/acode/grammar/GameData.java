package org.kathrynhuxtable.acode.grammar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;

import org.kathrynhuxtable.acode.grammar.tree.*;

@Data
public class GameData {

	// Data that is changeable.

	private List<SingleVariableNode> variables =  new ArrayList<>();
	private List<FlagNode> flags =  new ArrayList<>();
	private List<ArrayNode> arrays =  new ArrayList<>();
	private Map<String, Integer> states = new HashMap<>();
}
