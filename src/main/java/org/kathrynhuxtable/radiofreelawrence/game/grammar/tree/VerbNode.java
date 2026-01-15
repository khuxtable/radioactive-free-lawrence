package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.*;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VerbNode implements BaseNode, HasRefno, VocabularyNode {
	private String name;
	@Singular
	private List<String> verbs;
	private boolean noise;

	private int refno;
	private SourceLocation sourceLocation;
}
