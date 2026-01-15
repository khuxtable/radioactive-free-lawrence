package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.*;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ObjectNode implements BaseNode, HasRefno, VocabularyNode {
	private String name;
	@Singular
	private List<String> verbs;
	private boolean inVocabulary;

	private String inventoryDescription;
	private String briefDescription;
	private String longDescription;
	private BlockNode code;

	private int refno;
	private SourceLocation sourceLocation;
}
