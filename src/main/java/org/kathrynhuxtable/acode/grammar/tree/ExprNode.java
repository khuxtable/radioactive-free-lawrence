package org.kathrynhuxtable.acode.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExprNode implements AcdNode {

	public enum ExprOperator { LEAF, PLUS, MINUS, TIMES, DIVIDE, UNARY_PLUS, UNARY_MINUS }

	private ExprOperator operator;
	private ExprNode left;
	private ExprNode right;
	private String value;
}
