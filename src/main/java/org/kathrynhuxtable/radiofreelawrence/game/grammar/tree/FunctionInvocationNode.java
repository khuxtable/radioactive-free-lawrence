package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import org.kathrynhuxtable.radiofreelawrence.game.GameAction;
import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.InternalFunctions;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionInvocationNode implements ExprNode {
	private IdentifierNode objectReference;
	private IdentifierNode identifier;
	private String internalFunction;
	private String verbFunction;
	private List<ExprNode> parameters;
	private SourceLocation sourceLocation;

	@Override
	public void generate(MethodVisitor mv, GameContext gameContext) {
		try {
			if (objectReference != null) {
				objectReference.generate(mv, gameContext);
				mv.visitLdcInsn(identifier.getName());
				for (ExprNode parameter : parameters) {
					parameter.generate(mv, gameContext);
				}
				mv.visitMethodInsn(
						INVOKEINTERFACE,
						Type.getInternalName(GameAction.class),
						"doMessage",
						"(Ljava/lang/String;I)I",
						true);
			} else if (internalFunction != null) {
				mv.visitVarInsn(ALOAD, 0);
				if (gameContext.variableStore.getCurrentClass() != null) {
					mv.visitFieldInsn(
							GETFIELD,
							gameContext.variableStore.getCurrentClass(),
							"this$0",
							GameContext.GAME_CLASS_DESCRIPTOR);
				}
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
						if (variableContext != null && variableContext.getVariableType() == VariableType.NUMBER) {
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
			} else if (identifier != null) {
				VariableContext variableContext = gameContext.variableStore.getVariable(identifier.getName());
				if (variableContext == null) {
					throw new GameRuntimeException("Unknown method name: " + identifier.getName());
				}

				VariableType variableType = variableContext.getVariableType();
				if (variableType == VariableType.METHOD) {
					String className = variableContext.getParentClass();
					mv.visitVarInsn(ALOAD, 0);
					if (gameContext.variableStore.getCurrentClass() != null && className == null) {
						// Need to reference instance variable in outer class
						mv.visitFieldInsn(
								GETFIELD,
								gameContext.variableStore.getCurrentClass(),
								"this$0", // outer class "this"
								GameContext.GAME_CLASS_DESCRIPTOR);
					}
					StringBuilder descriptor = new StringBuilder("(");
					for (ExprNode parameter : parameters) {
						parameter.generate(mv, gameContext);
						descriptor.append(parameter.getVariableType(gameContext).getDescriptor());
					}
					descriptor.append(")I");

					if (className == null) {
						className = GameContext.GAME_CLASS_NAME;
					}
					mv.visitMethodInsn(INVOKEVIRTUAL,
							className,
							identifier.getName(),
							descriptor.toString(),
							false);
				} else if (variableType == VariableType.TEXT) {
					mv.visitVarInsn(ALOAD, 0);
					if (gameContext.variableStore.getCurrentClass() != null) {
						// Need to reference instance variable in outer class
						mv.visitFieldInsn(
								GETFIELD,
								gameContext.variableStore.getCurrentClass(),
								"this$0", // outer class "this"
								GameContext.GAME_CLASS_DESCRIPTOR);
					}

					StringBuilder descriptor = new StringBuilder("(");
					identifier.generate(mv, gameContext);
					descriptor.append("Ljava/lang/String;");
					for (ExprNode parameter : parameters) {
						parameter.generate(mv, gameContext);
						descriptor.append(parameter.getVariableType(gameContext).getDescriptor());
					}
					descriptor.append(")V");
					mv.visitMethodInsn(
							INVOKEVIRTUAL,
							GameContext.GAME_CLASS_NAME,
							"doAction",
							descriptor.toString(),
							false);
					mv.visitInsn(ICONST_1);
				} else if (variableType == VariableType.OBJECT || variableType == VariableType.PLACE || variableType == VariableType.REFERENCE) {
					identifier.generate(mv, gameContext);
					StringBuilder descriptor = new StringBuilder("(");
					for (ExprNode parameter : parameters) {
						parameter.generate(mv, gameContext);
						descriptor.append(parameter.getVariableType(gameContext).getDescriptor());
					}
					descriptor.append(")V");
					mv.visitMethodInsn(
							INVOKEINTERFACE,
							Type.getInternalName(GameAction.class),
							"doAction",
							descriptor.toString(),
							true);
					mv.visitInsn(ICONST_1);
				} else {
					throw new GameRuntimeException("Invalid method type: " + identifier.getName());
				}
			} else if (verbFunction != null) {
				mv.visitVarInsn(ALOAD, 0);
				if (gameContext.variableStore.getCurrentClass() != null) {
					// Need to reference instance variable in outer class
					mv.visitFieldInsn(
							GETFIELD,
							gameContext.variableStore.getCurrentClass(),
							"this$0", // outer class "this"
							GameContext.GAME_CLASS_DESCRIPTOR);
				}
				StringBuilder descriptor = new StringBuilder("(Ljava/lang/String;");
				mv.visitLdcInsn(verbFunction);
				for (ExprNode parameter : parameters) {
					parameter.generate(mv, gameContext);
					descriptor.append(parameter.getVariableType(gameContext).getDescriptor());
				}
				descriptor.append(")V");
				mv.visitMethodInsn(
						INVOKEVIRTUAL,
						GameContext.GAME_CLASS_NAME,
						"doAction",
						descriptor.toString(),
						false);
				mv.visitInsn(ICONST_1);
			} else {
				throw new GameRuntimeException("Unknown function invocation");
			}
		} catch (Exception e) {
			throw new GameRuntimeException(sourceLocation + ": " + e.getMessage(), e);
		}
	}

	@Override
	public VariableType getVariableType(GameContext gameContext) {
		return VariableType.NUMBER;
	}
}
