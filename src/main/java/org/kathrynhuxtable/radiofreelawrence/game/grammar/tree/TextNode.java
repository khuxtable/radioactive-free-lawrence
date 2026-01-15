package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TextNode implements BaseNode, HasRefno {

	public enum TextMethod { INCREMENT, CYCLE, RANDOM, ASSIGNED }

	private String name;
	private List<String> texts;
	private TextMethod method;
	private boolean fragment;

	private int refno;
	private SourceLocation sourceLocation;
}
