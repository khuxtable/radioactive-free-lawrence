package org.kathrynhuxtable.acode.grammar.tree;

import java.util.List;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class PlaceNode implements AcdNode {
	@Singular
	List<String> names;
	boolean inVocabulary;
	
	String briefDescription;
	String longDescription;

	int refno;
}
