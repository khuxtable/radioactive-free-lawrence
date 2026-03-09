package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.MyClassVisitor;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VariableNode implements DeclaratorNode {
	private String variable;

	private SourceLocation sourceLocation;

	@Override
	public void generate(MyClassVisitor cv, GameContext gameContext) {
		gameContext.variableStore.addVariable(variable, VariableType.NUMBER);

		cv.createField(ACC_PUBLIC, variable, "I");
	}
}
