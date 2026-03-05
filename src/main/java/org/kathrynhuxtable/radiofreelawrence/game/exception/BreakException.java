package org.kathrynhuxtable.radiofreelawrence.game.exception;

import lombok.Getter;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.ControlType;

@Getter
public class BreakException extends LoopControlException {

	public BreakException(String label) {
		super(label);
	}

	public BreakException(ControlType controlType) {
		super(controlType);
	}
}
