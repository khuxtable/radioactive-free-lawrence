package org.kathrynhuxtable.acode.grammar.tree;

import java.util.List;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerbNode implements AcdNode {
	@Singular
	List<String> verbs;
	boolean noise;

	int refno;
}
