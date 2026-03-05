package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.ClassVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext.VariableType;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VariableNode implements DeclaratorNode {
	private String variable;

	private SourceLocation sourceLocation;

	@Override
	public void generate(ClassVisitor cv, GameContext gameContext) {
		gameContext.variableStore.addVariable(variable, VariableType.NUMBER);

		cv.visitField(ACC_PUBLIC, variable, "I", null, null).visitEnd();
	}
}
