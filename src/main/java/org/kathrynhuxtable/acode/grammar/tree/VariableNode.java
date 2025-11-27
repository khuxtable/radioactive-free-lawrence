package org.kathrynhuxtable.acode.grammar.tree;

import java.util.List;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VariableNode implements AcdNode {
	List<String> variables;

	int refno;
}
