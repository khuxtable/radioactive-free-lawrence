package org.kathrynhuxtable.acode.grammar;

import java.io.IOException;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.RuleContext;

import org.kathrynhuxtable.acode.grammar.ACodeParser.*;
import org.kathrynhuxtable.acode.grammar.tree.*;
import org.kathrynhuxtable.acode.grammar.tree.ExprNode.ExprOperator;
import org.kathrynhuxtable.acode.grammar.tree.FlagNode.FlagType;

@RequiredArgsConstructor
public class ACodeAstVisitor extends ACodeParserBaseVisitor<AcdNode> {
	private final InputNode root;
	private final ErrorReporter errorReporter;

	@Override
	public InputNode visitInput(ACodeParser.InputContext ctx) {
		visitChildren(ctx);
		return root;
	}

	@Override
	public AcdNode visitIncludePragma(IncludePragmaContext ctx) {
		TextLiteralNode textLiteralNode = (TextLiteralNode) visit(ctx.textLiteral());
		String resource = textLiteralNode.getText();
		try {
			// Parse the include file, adding its values to our result.
			ACodeGrammarGenerator.parseFile((InputNode) root, errorReporter, resource, false);
		} catch (IOException e) {
			errorReporter.reportError(ctx,
					"Unable to open include file \"" + resource + "\": " + e.getMessage());
		}
		return root;
	}

	@Override
	public AcdNode visitIncludeOptPragma(IncludeOptPragmaContext ctx) {
		TextLiteralNode textLiteralNode = (TextLiteralNode) visit(ctx.textLiteral());
		String resource = textLiteralNode.getText();
		try {
			// Parse the include file, adding its values to our result.
			ACodeGrammarGenerator.parseFile((InputNode) root, errorReporter, resource, true);
		} catch (IOException e) {
			errorReporter.reportError(ctx,
					"Unable to open include file \"" + resource + "\": " + e.getMessage());
		}
		return root;
	}

	@Override
	public AcdNode visitNamePragma(NamePragmaContext ctx) {
		TextLiteralNode textLiteralNode = (TextLiteralNode) visit(ctx.textLiteral());
		root.setName(textLiteralNode.getText());
		return root;
	}

	@Override
	public AcdNode visitVersionPragma(VersionPragmaContext ctx) {
		TextLiteralNode textLiteralNode = (TextLiteralNode) visit(ctx.textLiteral());
		root.setVersion(textLiteralNode.getText());
		return root;
	}

	@Override
	public AcdNode visitAuthorPragma(AuthorPragmaContext ctx) {
		TextLiteralNode textLiteralNode = (TextLiteralNode) visit(ctx.textLiteral());
		root.setAuthor(textLiteralNode.getText());
		return root;
	}

	@Override
	public AcdNode visitDatePragma(DatePragmaContext ctx) {
		TextLiteralNode textLiteralNode = (TextLiteralNode) visit(ctx.textLiteral());
		root.setDate(textLiteralNode.getText());
		return root;
	}

	@Override
	public AcdNode visitUtf8Pragma(Utf8PragmaContext ctx) {
		root.setUtf8(true);
		return root;
	}

	@Override
	public AcdNode visitStylePragma(StylePragmaContext ctx) {
		root.setStyle(Integer.parseInt(ctx.number().getText()));
		return root;
	}

	@Override
	public FlagNode visitFlagDirective(FlagDirectiveContext ctx) {
		FlagType type = FlagType.valueOf(ctx.getChild(1).getText().toUpperCase());
		FlagNode node = FlagNode.builder()
				.type(type)
				.flags(ctx.flagClause().stream().map(RuleContext::getText).collect(Collectors.toList()))
				.build();
		root.getFlags().add(node);
		return node;
	}

	@Override
	public VerbNode visitVerbDirective(ACodeParser.VerbDirectiveContext ctx) {
		VerbNode node = VerbNode.builder()
				.verbs(ctx.identifier().stream().map(RuleContext::getText).collect(Collectors.toList()))
				.noise(false)
				.build();
		for (String verb : node.getVerbs()) {
			if (root.getVerbs().containsKey(verb)) {
				errorReporter.reportError(ctx, "Duplicate verb \"" + verb + "\"");
			}
			if (root.getIdentifiers().containsKey(verb)) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + verb + "\"");
			}
			root.getIdentifiers().put(verb, node);
			root.getVerbs().put(verb, node);
		}
		return node;
	}

	@Override
	public VerbNode visitNoiseDirective(ACodeParser.NoiseDirectiveContext ctx) {
		VerbNode node = VerbNode.builder()
				.verbs(ctx.identifier().stream().map(RuleContext::getText).collect(Collectors.toList()))
				.noise(false)
				.build();
		root.getNoise().addAll(node.getVerbs());
		return node;
	}

	@Override
	public TextNode visitTextDirective(ACodeParser.TextDirectiveContext ctx) {
		String text = ((TextBlockNode) visit(ctx.textBlock())).getText();
		TextNode node = TextNode.builder()
				.name(ctx.identifier() == null ? null : ctx.identifier().getText())
				.text(text)
				.fragment(false)
				.build();
		if (node.getName() != null) {
			if (root.getIdentifiers().containsKey(node.getName())) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + node.getName() + "\"");
			}
			root.getIdentifiers().put(node.getName(), node);
		}
		root.getTexts().add(node);
		return node;
	}

	@Override
	public TextNode visitFragmentDirective(ACodeParser.FragmentDirectiveContext ctx) {
		String text = ((TextBlockNode) visit(ctx.textBlock())).getText();
		TextNode node = TextNode.builder()
				.name(ctx.identifier() == null ? null : ctx.identifier().getText())
				.text(text)
				.fragment(true)
				.build();
		if (node.getName() != null) {
			if (root.getIdentifiers().containsKey(node.getName())) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + node.getName() + "\"");
			}
			root.getIdentifiers().put(node.getName(), node);
		}
		root.getTexts().add(node);
		return node;
	}

	@Override
	public AtNode visitAtDirective(AtDirectiveContext ctx) {
		CodeBlockNode codeBlockNode = (CodeBlockNode) visit(ctx.codeBlock());
		AtNode node = AtNode.builder()
				.name(ctx.identifier() == null ? null : ctx.identifier().getText())
				.code(codeBlockNode.getCode())
				.build();
		root.getAts().add(node);
		return node;
	}

	@Override
	public ActionNode visitActionDirective(ActionDirectiveContext ctx) {
		CodeBlockNode codeBlockNode = (CodeBlockNode) visit(ctx.codeBlock());
		ActionNode node = ActionNode.builder()
				.arg1(ctx.arg1.getText())
				.arg2(ctx.arg2 == null ? null : ctx.arg2.getText())
				.code(codeBlockNode.getCode())
				.build();
		root.getActions().add(node);
		return node;
	}

	@Override
	public ProcNode visitProcDirective(ProcDirectiveContext ctx) {
		CodeBlockNode codeBlockNode = (CodeBlockNode) visit(ctx.codeBlock());
		ProcNode node = ProcNode.builder()
				.name(ctx.name.getText())
				.args(ctx.identifier().stream().map(RuleContext::getText).collect(Collectors.toList()))
				.code(codeBlockNode.getCode())
				.build();
		if (node.getName() != null) {
			if (root.getIdentifiers().containsKey(node.getName())) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + node.getName() + "\"");
			}
			root.getIdentifiers().put(node.getName(), node);
		}
		root.getProcs().add(node);
		return node;
	}

	@Override
	public InitialNode visitInitialDirective(InitialDirectiveContext ctx) {
		InitialNode node = InitialNode.builder()
				.code(ctx.codeBlock().getText())
				.build();
		root.getInits().add(node);
		return node;
	}

	@Override
	public RepeatNode visitRepeatDirective(RepeatDirectiveContext ctx) {
		RepeatNode node = RepeatNode.builder()
				.code(ctx.codeBlock().getText())
				.build();
		root.getRepeats().add(node);
		return node;
	}

	@Override
	public StateNode visitStateDirective(StateDirectiveContext ctx) {
		StateNode node = new StateNode();
		boolean first = true;
		for (StateClauseContext childCtx : ctx.stateClause()) {
			StateClauseNode clause = (StateClauseNode) visit(childCtx);
			if (first) {
				first = false;
				if (clause.getValue() == null) {
					clause.setValue(ExprNode.builder()
							.operator(ExprOperator.LEAF)
							.value("0")
							.build());
				}
			}
			node.getStates().put(clause.getState(), clause);
		}
		for (Map.Entry<String, StateClauseNode> entry : node.getStates().entrySet()) {
			if (root.getIdentifiers().containsKey(entry.getKey())) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + entry.getKey() + "\"");
			}
			root.getIdentifiers().put(entry.getKey(), entry.getValue());
			root.getStates().put(entry.getKey(), entry.getValue());
		}
		return node;
	}

	@Override
	public StateClauseNode visitStateClause(StateClauseContext ctx) {
		return new StateClauseNode(ctx.identifier().getText(), ctx.expression() == null ? null : (ExprNode) visit(ctx.expression()));
	}

	@Override
	public VariableNode visitVariableDirective(VariableDirectiveContext ctx) {
		VariableNode node = VariableNode.builder()
				.variables(ctx.identifier().stream().map(RuleContext::getText).collect(Collectors.toList()))
				.build();
		root.getVariables().addAll(node.getVariables().stream()
				.map(v -> SingleVariableNode.builder()
						.variable(v)
						.build())
				.collect(Collectors.toList()));
		for (String var : node.getVariables()) {
			if (root.getIdentifiers().containsKey(var)) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + var + "\"");
			}
			root.getIdentifiers().put(var, node);
		}
		return node;
	}

	@Override
	public ArrayNode visitArrayDirective(ACodeParser.ArrayDirectiveContext ctx) {
		ArrayNode node = ArrayNode.builder()
				.name(ctx.identifier().getText())
				.size(Integer.parseInt(ctx.number().getText()))
				.build();
		if (node.getName() != null) {
			if (root.getIdentifiers().containsKey(node.getName())) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + node.getName() + "\"");
			}
			root.getIdentifiers().put(node.getName(), node);
		}
		root.getArrays().add(node);
		return node;
	}

	@Override
	public PlaceNode visitPlaceDirective(ACodeParser.PlaceDirectiveContext ctx) {
		String briefDescription = ((TextBlockNode) visit(ctx.textBlock(0))).getText();
		String longDescription = ctx.textBlock(1) == null ? null : ((TextBlockNode) visit(ctx.textBlock(1))).getText();
		PlaceNode node = PlaceNode.builder()
				.names(ctx.identifier().stream().map(RuleContext::getText).collect(Collectors.toList()))
				.inVocabulary("+".equals(ctx.getChild(1).toString()))
				.briefDescription(briefDescription)
				.longDescription(longDescription)
				.build();
		if (node.isInVocabulary()) {
			if (root.getVerbs().containsKey(node.getNames().get(0))) {
				errorReporter.reportError(ctx, "Duplicate verb \"" + node.getNames().get(0) + "\"");
			}
			// Add first word to vocabulary.
			root.getVerbs().put(node.getNames().get(0), node);
		}
		for (String var : node.getNames()) {
			if (root.getIdentifiers().containsKey(var)) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + var + "\"");
			}
			root.getIdentifiers().put(var, node);
		}
		root.getPlaces().add(node);
		return node;
	}

	@Override
	public ObjectNode visitObjectDirective(ACodeParser.ObjectDirectiveContext ctx) {
		String briefDescription = ((TextBlockNode) visit(ctx.textBlock(0))).getText();
		String longDescription = ctx.textBlock(1) == null ? null : ((TextBlockNode) visit(ctx.textBlock(1))).getText();
		ObjectNode node = ObjectNode.builder()
				.names(ctx.identifier().stream().map(RuleContext::getText).collect(Collectors.toList()))
				.inVocabulary(!"-".equals(ctx.getChild(1).toString()))
				.briefDescription(briefDescription)
				.longDescription(longDescription)
				.build();
		if (node.isInVocabulary()) {
			if (root.getVerbs().containsKey(node.getNames().get(0))) {
				errorReporter.reportError(ctx, "Duplicate verb \"" + node.getNames().get(0) + "\"");
			}
			// Add first word to vocabulary.
			root.getVerbs().put(node.getNames().get(0), node);
		}
		for (String var : node.getNames()) {
			if (root.getIdentifiers().containsKey(var)) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + var + "\"");
			}
			root.getIdentifiers().put(var, node);
		}
		root.getObjects().add(node);
		return node;
	}

	@Override
	public ExprNode visitAddSubExpr(ACodeParser.AddSubExprContext ctx) {
		return ExprNode.builder()
				.operator("-".equals(ctx.oper.getText()) ? ExprOperator.MINUS : ExprOperator.PLUS)
				.left((ExprNode) visit(ctx.left))
				.right((ExprNode) visit(ctx.right))
				.build();
	}

	@Override
	public AcdNode visitSimpleExpr(SimpleExprContext ctx) {
		return visit(ctx.atom);
	}

	@Override
	public ExprNode visitUnaryOperExpr(UnaryOperExprContext ctx) {
		return ExprNode.builder()
				.operator("-".equals(ctx.oper.getText()) ? ExprOperator.UNARY_MINUS : ExprOperator.UNARY_PLUS)
				.left((ExprNode) visit(ctx.expr))
				.build();
	}

	@Override
	public AcdNode visitPrimaryExpr(PrimaryExprContext ctx) {
		return visitPrimary(ctx.primary());
	}

	@Override
	public ExprNode visitPrimary(PrimaryContext ctx) {
		return ExprNode.builder()
				.operator(ExprNode.ExprOperator.LEAF)
				.value(ctx.getText())
				.build();
	}

	@Override
	public TextLiteralNode visitTextLiteral(ACodeParser.TextLiteralContext ctx) {
		String text = ctx.getText();
		if (text.startsWith("\"") && text.endsWith("\"")) {
			text = text.substring(1, text.length() - 1).replaceAll("\\\"", "\"");
			// TODO Need to unescape other chars, maybe?
		}
		return new TextLiteralNode(text);
	}

	@Override
	public TextBlockNode visitTextBlock(TextBlockContext ctx) {
		String text = ctx.getText();
		text = text.replaceAll("\\{\\{\\s+(.+)\\s+}}", "$1");
		return TextBlockNode.builder()
				.text(text)
				.build();
	}

	@Override
	public CodeBlockNode visitCodeBlock(CodeBlockContext ctx) {
		String text = ctx.getText();
		String code = text.replaceAll("\\{\\{\\s+(.+)\\s+}}", "$1");
		return CodeBlockNode.builder()
				.code(code)
				.build();
	}
}
