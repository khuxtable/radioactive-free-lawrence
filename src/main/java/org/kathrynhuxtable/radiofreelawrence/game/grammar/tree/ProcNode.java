package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.LocalVariablesSorter;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.exception.GameRuntimeException;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableType;

import static org.objectweb.asm.Opcodes.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ProcNode implements DeclaratorNode {
	private String name;
	private List<String> args;
	private List<VariableType> parameterTypes;
	private BlockNode code;
	private SourceLocation sourceLocation;

	@Override
	public void generate(ClassVisitor cv, GameContext gameContext) {
		try {
			gameContext.variableStore.newFunctionScope();
			StringBuilder descriptor = new StringBuilder("(");
			for (int i = 0; i < args.size(); i++) {
				descriptor.append(switch (parameterTypes.get(i)) {
					case NUMBER -> Type.INT_TYPE.getDescriptor();
					case TEXT -> Type.getDescriptor(String.class);
					case REFERENCE -> Type.getDescriptor(Object.class);
					default -> Type.getDescriptor(Object.class);
				});
			}
			descriptor.append(")");
			descriptor.append(Type.INT_TYPE.getDescriptor());
			MethodVisitor mv2 = cv.visitMethod(ACC_PUBLIC, name, descriptor.toString(), null, null);
			mv2.visitCode();
			LocalVariablesSorter mv = new LocalVariablesSorter(ACC_PUBLIC, descriptor.toString(), mv2);
			Label startLabel = new Label();
			mv.visitLabel(startLabel);
			int index = 1;
			for (int i = 0; i < args.size(); i++) {
				VariableContext variableContext = gameContext.variableStore.addVariable(args.get(i), parameterTypes.get(i));
				variableContext.setIndex(index++);
			}
			code.generate(mv, gameContext);
			Label endLabel = new Label();
			mv.visitLabel(endLabel);
			mv.visitInsn(ICONST_0);
			mv.visitInsn(IRETURN);
			gameContext.variableStore.closeFunctionScope(mv, startLabel, endLabel);
			mv.visitMaxs(0, 0);
			mv.visitEnd();
		} catch (Exception e) {
			throw new GameRuntimeException(sourceLocation + ": " + e.getMessage(), e);
		}
	}
}
