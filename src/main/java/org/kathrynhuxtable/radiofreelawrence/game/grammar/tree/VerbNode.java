package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerbNode implements BaseNode, HasRefno, VocabularyNode {
	String name;
	@Singular
	List<String> verbs;
	boolean noise;

	int refno;
}
