package org.kathrynhuxtable.radiofreelawrence.game;

public class Text {

	public TextMethod method;
	public String[] texts;

	public Text(TextMethod method, String[] texts) {
		this.method = method;
		this.texts = texts;
	}

	public String getText() {
		return texts[0];
	}
}
