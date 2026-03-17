package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.Iterator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import org.kathrynhuxtable.radiofreelawrence.game.GameAction;
import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EnhancedForStatementNode implements StatementNode {
	private IdentifierNode identifier;
	private ExprNode expression;
	private StatementNode statement;
	private String label;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		gameContext.variableStore.newBlockScope();
		Label beginLabel = new Label();
		Label loopLabel = new Label();
		Label endLabel = new Label();

		mv.visitLabel(beginLabel);
		VariableContext variableContext = gameContext.variableStore.addVariable(
				identifier.getName(),
				VariableType.REFERENCE);

		expression.generate(mv, gameContext);

		int iterIndex = 1;
		variableContext.setIndex(((LocalVariablesSorter) mv).newLocal(Type.getType(GameAction.class)));
		int varIndex = 2;
		variableContext.setIndex(((LocalVariablesSorter) mv).newLocal(Type.getType(Object.class)));

		mv.visitMethodInsn(
				INVOKEINTERFACE,
				Type.getInternalName(GameAction.class),
				"iterator",
				"()" + Type.getDescriptor(Iterator.class),
				true);
		mv.visitVarInsn(ASTORE, iterIndex);

		mv.visitLabel(loopLabel);
//		mv.visitFrame(Opcodes.F_APPEND, 1, new Object[]{Type.getInternalName(Iterator.class)}, 0, null);
		mv.visitVarInsn(ALOAD, iterIndex);
		mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Iterator.class), "hasNext", "()Z", true);
		mv.visitJumpInsn(IFEQ, endLabel);
		mv.visitVarInsn(ALOAD, iterIndex);
		mv.visitMethodInsn(INVOKEINTERFACE, Type.getInternalName(Iterator.class), "next", "()" + Type.getDescriptor(Object.class), true);
		mv.visitVarInsn(ASTORE, varIndex);
		statement.generate(mv, gameContext);

		mv.visitJumpInsn(GOTO, loopLabel);
		mv.visitLabel(endLabel);
		gameContext.variableStore.closeBlockScope(mv, beginLabel, endLabel);
	}
}
