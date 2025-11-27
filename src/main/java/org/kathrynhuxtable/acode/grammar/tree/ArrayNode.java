package org.kathrynhuxtable.acode.grammar.tree;

import java.util.List;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ArrayNode implements AcdNode {
	String name;
	int size;
}
