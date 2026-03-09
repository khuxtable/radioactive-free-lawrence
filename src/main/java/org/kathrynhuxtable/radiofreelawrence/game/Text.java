package org.kathrynhuxtable.radiofreelawrence.game;

public class Text {

	public TextMethod method;
	public String[] texts;
	private int count = 0;

	public Text(TextMethod method, String[] texts) {
		this.method = method;
		this.texts = texts;
	}

	public String getText() {
		return switch (method) {
			case INCREMENT -> texts[Math.min(count++, texts.length - 1)];
			case CYCLE -> texts[count++ % texts.length];
			case RANDOM -> texts[(int) (Math.random() * texts.length)];
			case ASSIGNED -> texts[0];
		};
	}
}
