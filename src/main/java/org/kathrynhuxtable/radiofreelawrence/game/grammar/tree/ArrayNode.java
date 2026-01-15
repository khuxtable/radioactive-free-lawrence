package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.*;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArrayNode implements BaseNode {
	private String name;
	private int size;
	private SourceLocation sourceLocation;
}
