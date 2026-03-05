package org.kathrynhuxtable.radiofreelawrence.game.exception;

import java.util.Objects;

import lombok.Getter;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.ControlType;

@Getter
public class LoopControlException extends RuntimeException {

	private final String label;
	private final ControlType controlType;

	public LoopControlException(String label) {
		this.label = label;
		this.controlType = ControlType.CODE;
	}

	public LoopControlException(ControlType controlType) {
		assert controlType != null;

		this.label = null;
		this.controlType = controlType;
	}

	public boolean ignore(String label) {
		return controlType != ControlType.CODE || !Objects.equals(label, this.label);
	}
}
