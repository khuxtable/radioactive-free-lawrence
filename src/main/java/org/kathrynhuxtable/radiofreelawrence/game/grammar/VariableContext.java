package org.kathrynhuxtable.radiofreelawrence.game.grammar;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Label;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariableContext {

	private VariableType variableType;
	private VariableScope variableScope;

	String name;
	String parentClass;
	int index;

	VariableContext reference;
	Label label;
}
