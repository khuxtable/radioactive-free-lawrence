package org.kathrynhuxtable.radiofreelawrence.game.grammar;

import org.objectweb.asm.Type;
import org.springframework.lang.Nullable;

import org.kathrynhuxtable.radiofreelawrence.game.Text;

public enum VariableType {
	FLAG        (false, false),
	STATE       (false, false),
	NUMBER      (true,  false),
	TEXT        (true,  true),
	TEXT_NODE   (false, true),
	REFERENCE   (true,  true),
	METHOD      (false, true),
	LABEL       (false, false),
	OBJECT      (false, true),
	PLACE       (false, true);

	public final boolean assignable;
	public final boolean reference;

	VariableType(boolean assignable, boolean reference) {
		this.assignable = assignable;
		this.reference = reference;
	}

	@Nullable
	public String getDescriptor() {
		return switch (this) {
			case FLAG, STATE, NUMBER -> Type.INT_TYPE.getDescriptor();
			case TEXT -> Type.getDescriptor(String.class);
			case TEXT_NODE -> Type.getDescriptor(Text.class);
			case REFERENCE, OBJECT, PLACE -> Type.getDescriptor(Object.class);
			case LABEL, METHOD -> null;
		};
	}
}
