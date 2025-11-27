package org.kathrynhuxtable.acode.grammar.tree;

import lombok.Data;

@Data
public class TextLiteralNode implements AcdNode {
	public final String text;
    public TextLiteralNode(String text) {
        this.text = text;
    }
}
