package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceNode implements BaseNode, HasRefno, VocabularyNode {
	String name;
	@Singular
	List<String> verbs;

	String briefDescription;
	String longDescription;
	BlockNode code;

	int refno;
}
