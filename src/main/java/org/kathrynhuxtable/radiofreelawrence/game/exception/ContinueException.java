package org.kathrynhuxtable.radiofreelawrence.game.exception;

import lombok.Getter;

import org.kathrynhuxtable.radiofreelawrence.game.grammar.ControlType;

@Getter
public class ContinueException extends LoopControlException {

	public ContinueException(String label) {
		super(label);
	}

	public ContinueException(ControlType controlType) {
		super(controlType);
	}
}
