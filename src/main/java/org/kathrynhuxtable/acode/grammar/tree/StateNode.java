package org.kathrynhuxtable.acode.grammar.tree;

import java.util.*;

import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class StateNode implements AcdNode {

	Map<String, StateClauseNode> states = new LinkedHashMap<>();
}
