package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.objectweb.asm.ClassVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext.VariableType;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

@Data
@AllArgsConstructor
public class StateClauseNode implements DeclaratorNode {

	private String state;
	private ExprNode value;
	private SourceLocation sourceLocation;

	@Override
	public void generate(ClassVisitor cv, GameContext gameContext) {
		gameContext.variableStore.addVariable(state, VariableType.NUMBER);

		cv.visitField(ACC_PUBLIC | ACC_FINAL, state, "I", null, null).visitEnd();
	}

}
