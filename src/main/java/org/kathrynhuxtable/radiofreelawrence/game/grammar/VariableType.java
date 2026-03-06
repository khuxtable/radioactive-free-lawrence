package org.kathrynhuxtable.radiofreelawrence.game.grammar;

import org.objectweb.asm.Type;
import org.springframework.lang.Nullable;

import org.kathrynhuxtable.radiofreelawrence.game.Text;

public enum VariableType {
	FLAG, STATE, NUMBER, TEXT, TEXT_NODE, REFERENCE, METHOD, LABEL;

	@Nullable
	public String getDescriptor() {
		return switch (this) {
			case FLAG, STATE, NUMBER -> Type.INT_TYPE.getDescriptor();
			case TEXT -> Type.getDescriptor(String.class);
			case TEXT_NODE -> Type.getDescriptor(Text.class);
			case REFERENCE -> Type.getDescriptor(Object.class);
			case LABEL, METHOD -> null;
		};
	}
}
