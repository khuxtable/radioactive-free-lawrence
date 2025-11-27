package org.kathrynhuxtable.acode.grammar;

import java.util.List;

public class Element {
	
	public String name;
	public String label;
	public int value;
	public boolean fdef;

	public List<Direction> direction;
	public List<Element> list;
	public List<String> desc;

	public String power;
	public Element function;
	public List<Arg> args;
	public List<Element> objects;

	public List<Verb> verb;

}
