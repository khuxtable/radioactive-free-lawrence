package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.MyClassVisitor;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FlagNode implements DeclaratorNode {

	public enum FlagType { VARIABLE, OBJECT, PLACE }

	private List<String> flags;
	private FlagType type;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MyClassVisitor cv, GameContext gameContext) {
		for (String flag : this.flags) {
			gameContext.variableStore.addVariable(flag, VariableType.NUMBER);

			cv.createField(ACC_PUBLIC | ACC_FINAL, flag, "I");
		}
	}
}
