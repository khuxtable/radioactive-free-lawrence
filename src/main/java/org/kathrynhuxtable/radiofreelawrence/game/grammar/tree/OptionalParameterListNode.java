package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext.VariableType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OptionalParameterListNode implements BaseNode {

	private List<String> parameterNames;
	private List<VariableType> parameterTypes;
	private SourceLocation sourceLocation;
}
