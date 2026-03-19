package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlagStatement implements ExprNode {

	private boolean set;
	private FlagExpressionNode flagExpression;

	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
//		if (assignmentOperator == AssignmentOperator.EQUAL) {
//			right.generate(mv, gameContext);
//		} else {
//			left.getExpr().generate(mv, gameContext);
//			right.generate(mv, gameContext);
//
//			switch (assignmentOperator) {
//			case DIV_ASSIGN -> mv.visitInsn(IDIV);
//			case MOD_ASSIGN -> mv.visitInsn(IREM);
//			case ADD_ASSIGN -> mv.visitInsn(IADD);
//			case SUB_ASSIGN -> mv.visitInsn(ISUB);
//			case LSHIFT_ASSIGN -> mv.visitInsn(ISHL);
//			case RSHIFT_ASSIGN -> mv.visitInsn(ISHR);
//			case URSHIFT_ASSIGN -> mv.visitInsn(IUSHR);
//			case AND_ASSIGN -> mv.visitInsn(IAND);
//			case XOR_ASSIGN -> mv.visitInsn(IXOR);
//			case OR_ASSIGN -> mv.visitInsn(IOR);
//			}
//		}
//
//		mv.visitInsn(DUP);
//		left.generate(mv, gameContext);
	}

	@Override
	public VariableType getVariableType(GameContext gameContext) {
		return VariableType.NUMBER;
	}
}
