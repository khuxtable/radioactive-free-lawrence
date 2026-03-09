package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.MyClassVisitor;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

@Data
@AllArgsConstructor
public class StateClauseNode implements DeclaratorNode {

	private String state;
	private ExprNode value;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MyClassVisitor cv, GameContext gameContext) {
		gameContext.variableStore.addVariable(state, VariableType.NUMBER);

		cv.createField(ACC_PUBLIC | ACC_FINAL, state, "I");
	}

}
