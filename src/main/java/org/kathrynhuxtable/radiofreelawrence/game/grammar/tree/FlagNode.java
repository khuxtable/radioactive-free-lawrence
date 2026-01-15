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
public class FlagNode implements BaseNode {

	public enum FlagType { VARIABLE, OBJECT, PLACE }

	private List<String> flags;
	private FlagType type;
	private SourceLocation sourceLocation;
}
