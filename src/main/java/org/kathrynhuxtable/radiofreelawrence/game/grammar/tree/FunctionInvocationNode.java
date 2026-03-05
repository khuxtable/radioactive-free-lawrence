package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.InternalFunctions;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionInvocationNode implements ExprNode {
	private IdentifierNode identifier;
	private String internalFunction;
	private String verbFunction;
	private List<ExprNode> parameters;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		if (identifier != null) {
			VariableContext variableContext = gameContext.variableStore.getVariable(identifier.getName());
			if (variableContext == null) {
				throw new GameRuntimeException("Unknown method name: " + identifier.getName());
			} else if (variableContext.getVariableType() != VariableType.METHOD) {
				throw new GameRuntimeException("Invalid method name: " + identifier.getName());
			}

			mv.visitVarInsn(ALOAD, 0);

			StringBuilder descriptor = new StringBuilder("(");
			for (ExprNode parameter : parameters) {
				parameter.generate(mv, gameContext);
				descriptor.append("I");
			}
			descriptor.append(")I");

			mv.visitMethodInsn(INVOKEVIRTUAL,
					GameContext.GAME_CLASS_NAME,
					variableContext.getName(),
					descriptor.toString(),
					false);
		} else if (internalFunction != null) {
			mv.visitVarInsn(ALOAD, 0);
			mv.visitFieldInsn(
					GETFIELD,
					GameContext.GAME_CLASS_NAME,
					"internalFunctions",
					Type.getDescriptor(InternalFunctions.class));

			mv.visitIntInsn(SIPUSH, parameters.size());
			mv.visitTypeInsn(ANEWARRAY, "java/lang/Object");
			for (int index = 0; index < parameters.size(); index++) {
				mv.visitInsn(DUP);
				mv.visitIntInsn(SIPUSH, index);
				parameters.get(index).generate(mv, gameContext);
				if (!(parameters.get(index) instanceof TextElementNode) && !(parameters.get(index) instanceof IdentifierNode)) {
					mv.visitMethodInsn(
							INVOKESTATIC,
							"java/lang/Integer",
							"valueOf",
							"(I)Ljava/lang/Integer;",
					false);
				} else if (parameters.get(index) instanceof IdentifierNode identifierNode) {
					VariableContext variableContext = gameContext.variableStore.getVariable(identifierNode.getName());
					if (variableContext != null &&  variableContext.getVariableType() == VariableType.NUMBER) {
						mv.visitMethodInsn(
								INVOKESTATIC,
								"java/lang/Integer",
								"valueOf",
								"(I)Ljava/lang/Integer;",
								false);
					}
				}
				mv.visitInsn(AASTORE);
			}

			mv.visitMethodInsn(
					INVOKEVIRTUAL,
					Type.getInternalName(InternalFunctions.class),
					gameContext.getInternalFunctions().getInternalFunction(internalFunction),
					"([Ljava/lang/Object;)I",
					false);
		} else if (verbFunction != null) {
			throw new GameRuntimeException("Verb function not supported: " + verbFunction);
		} else {
			throw new GameRuntimeException("Unknown function invocation");
		}
	}
}
