package org.kathrynhuxtable.acode.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ActionNode implements AcdNode {
	String arg1;
	String arg2;
	String code;
}
