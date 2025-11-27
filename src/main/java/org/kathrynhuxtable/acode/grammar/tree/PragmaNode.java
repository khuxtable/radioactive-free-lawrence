package org.kathrynhuxtable.acode.grammar.tree;

import lombok.Data;

@Data
public class PragmaNode implements AcdNode {
	public final String pragma;
	public final String value;
	public PragmaNode(String pragma, String value) {
		this.pragma = pragma;
		this.value = value;
	}
}
