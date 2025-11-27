package org.kathrynhuxtable.acode.grammar.tree;

import java.util.List;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TextNode implements AcdNode {
	String name;
	String text;
	boolean fragment;

	int refno;
}
