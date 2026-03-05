package org.kathrynhuxtable.radiofreelawrence.game.grammar.tree;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Type;

import org.kathrynhuxtable.radiofreelawrence.game.GameContext;
import org.kathrynhuxtable.radiofreelawrence.game.Text;
import org.kathrynhuxtable.radiofreelawrence.game.TextMethod;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.SourceLocation;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.VariableContext.VariableType;

import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TextNode implements DeclaratorNode {

	private String name;
	private List<TextElementNode> textNodes;
	private TextMethod method;
	private boolean fragment;

	private SourceLocation sourceLocation;

	@Override
	public void generate(ClassVisitor cv, GameContext gameContext) {
		gameContext.variableStore.addVariable(name, VariableType.TEXT_NODE);
		cv.visitField(ACC_PUBLIC | ACC_FINAL, name, Type.getDescriptor(Text.class), null, null).visitEnd();
	}
}
