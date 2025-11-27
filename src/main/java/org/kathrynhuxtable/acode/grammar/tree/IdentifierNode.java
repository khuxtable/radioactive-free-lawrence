package org.kathrynhuxtable.acode.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class IdentifierNode implements AcdNode {
	private String name;
}
