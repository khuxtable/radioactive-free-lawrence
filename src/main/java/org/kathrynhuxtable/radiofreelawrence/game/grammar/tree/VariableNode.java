package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import org.kathrynhuxtable.radiofreelawrence.game.AsmUtils;
import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.Text;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VariableNode implements DeclaratorNode {
	private String variable;
	private VariableType variableType;

	private SourceLocation sourceLocation;

	@Override
	public void generate(ClassVisitor cv, GameContext gameContext) {
		gameContext.variableStore.addVariable(variable, variableType);

		AsmUtils.createField(cv, ACC_PUBLIC, variable, getType());
	}

	public String getType() {
		return switch (variableType) {
			case FLAG, STATE, NUMBER -> "I";
			case TEXT -> Type.getDescriptor(String.class);
			case TEXT_NODE -> Type.getDescriptor(Text.class);
			case REFERENCE, OBJECT, PLACE -> Type.getDescriptor(Object.class);
			case METHOD, LABEL -> Type.getDescriptor(Object.class);
		};
	}
}
