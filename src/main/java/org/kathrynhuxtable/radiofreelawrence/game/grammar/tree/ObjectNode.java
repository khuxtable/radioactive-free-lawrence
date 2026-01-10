package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ObjectNode implements BaseNode, HasRefno, VocabularyNode {
	String name;
	@Singular
	List<String> verbs;
	boolean inVocabulary;

	String inventoryDescription;
	String briefDescription;
	String longDescription;
	BlockNode code;

	int refno;
}
