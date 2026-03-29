package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.Map;
import java.util.TreeMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.LocalVariablesSorter;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageNode implements StatementNode {

	private String name;
	private BlockNode block;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		gameContext.variableStore.newBlockScope();
		Label startLabel = new Label();
		mv.visitLabel(startLabel);
		block.generate(mv, gameContext);
		Label endLabel = new Label();
		mv.visitLabel(endLabel);
		gameContext.variableStore.closeBlockScope(mv, startLabel, endLabel);
	}
}
