package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;
import java.util.Map;

import lombok.*;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceNode implements BaseNode, HasRefno, VocabularyNode {
	private String name;
	@Singular
	private List<String> verbs;

	private String briefDescription;
	private String longDescription;
	private Map<String, BlockNode> commands;

	private int refno;
	private SourceLocation sourceLocation;
}
