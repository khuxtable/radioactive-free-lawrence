package org.kathrynhuxtable.radiofreelawrence.game.grammar;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.TerminalNode;

import org.kathrynhuxtable.gdesc.parser.GameLexer;
import org.kathrynhuxtable.gdesc.parser.GameParser;
import org.kathrynhuxtable.gdesc.parser.GameParser.*;
import org.kathrynhuxtable.gdesc.parser.GameParserBaseVisitor;
import org.kathrynhuxtable.radiofreelawrence.game.GameData.IdentifierType;
import org.kathrynhuxtable.radiofreelawrence.game.exception.LoopControlException.ControlType;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.*;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.ActionNode.ActionCode;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.AssignmentNode.AssignmentOperator;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.BinaryNode.Operator;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.FlagNode.FlagType;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.TextNode.TextMethod;
import org.kathrynhuxtable.radiofreelawrence.game.grammar.tree.UnaryNode.UnaryOperator;

@RequiredArgsConstructor
public class GameVisitor extends GameParserBaseVisitor<BaseNode> {
	private final GameNode root;
	private final ErrorReporter errorReporter;

	public void readFile(String filePath, boolean optional) throws IOException {
		try (InputStream inputStream = GameVisitor.class.getResourceAsStream("/" + filePath)) {
			if (inputStream != null) {
				CharStream charStream = CharStreams.fromChannel(
						Channels.newChannel(inputStream),
						StandardCharsets.UTF_8,
						4096,
						CodingErrorAction.REPLACE,
						filePath,
						-1);

				GameLexer lexer = new GameLexer(charStream);
				lexer.removeErrorListeners();
				lexer.addErrorListener(DescriptiveErrorListener.INSTANCE);

				CommonTokenStream tokens = new CommonTokenStream(lexer);

				GameParser parser = new GameParser(tokens);
				parser.removeErrorListeners();
				parser.addErrorListener(DescriptiveErrorListener.INSTANCE);
				parser.setBuildParseTree(true);

				GameContext gameContext = parser.game();

				visit(gameContext);
			} else if (!optional) {
				throw new IOException("Unable to open required include file " + filePath);
			}
		}
	}

	@Override
	public GameNode visitGame(GameParser.GameContext ctx) {
		visitChildren(ctx);
		return root;
	}

	@Override
	public BaseNode visitIncludePragma(IncludePragmaContext ctx) {
		TextElementNode textLiteralNode = getTextLiteralNode(ctx.STRING_LITERAL());
		String resource = textLiteralNode.getText();
		boolean optional = ctx.BOOL_LITERAL() != null && "true".equalsIgnoreCase(ctx.BOOL_LITERAL().getText());
		try {
			// Parse the include file, adding its values to our result.
			GameVisitor nested = new GameVisitor(root, errorReporter);
			nested.readFile(resource, optional);
		} catch (IOException e) {
			errorReporter.reportError(ctx,
					"Unable to open include file \"" + resource + "\": " + e.getMessage());
		}
		return root;
	}

	@Override
	public BaseNode visitNamePragma(NamePragmaContext ctx) {
		root.setName(TextUtils.cleanStringLiteral(ctx.STRING_LITERAL().getText()));
		return root;
	}

	@Override
	public BaseNode visitVersionPragma(VersionPragmaContext ctx) {
		root.setVersion(TextUtils.cleanStringLiteral(ctx.STRING_LITERAL().getText()));
		return root;
	}

	@Override
	public BaseNode visitAuthorPragma(AuthorPragmaContext ctx) {
		root.setAuthor(TextUtils.cleanStringLiteral(ctx.STRING_LITERAL().getText()));
		return root;
	}

	@Override
	public BaseNode visitDatePragma(DatePragmaContext ctx) {
		root.setDate(TextUtils.cleanStringLiteral(ctx.STRING_LITERAL().getText()));
		return root;
	}

	@Override
	public FlagNode visitFlagDirective(FlagDirectiveContext ctx) {
		FlagType type = FlagType.valueOf(ctx.getChild(1).getText().toUpperCase());
		FlagNode node = FlagNode.builder()
				.type(type)
				.flags(ctx.flagClause().stream()
						.map(c -> c.getText().toLowerCase())
						.collect(Collectors.toList()))
				.build();
		node.getFlags().forEach(flag -> {
			if (root.getIdentifiers().containsKey(flag)) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + flag + "\"");
			}
			root.getIdentifiers().put(flag, node);
		});

		root.getFlags().add(node);
		return node;
	}

	@Override
	public VerbNode visitVerbDirective(GameParser.VerbDirectiveContext ctx) {
		VerbNode node = VerbNode.builder()
				.verbs(ctx.IDENTIFIER().stream()
						.map(v -> v.getText().toLowerCase())
						.collect(Collectors.toList()))
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
	public VerbNode visitNoiseDirective(GameParser.NoiseDirectiveContext ctx) {
		VerbNode node = VerbNode.builder()
				.verbs(ctx.IDENTIFIER().stream()
						.map(n -> n.getText().toLowerCase())
						.collect(Collectors.toList()))
				.noise(false)
				.build();
		root.getNoise().addAll(node.getVerbs());
		return node;
	}

	@Override
	public TextNode visitTextDirective(GameParser.TextDirectiveContext ctx) {
		List<String> texts = ctx.textElement().stream()
				.map(c -> ((TextElementNode) visit(c)).getText())
				.collect(Collectors.toList());
		TextNode node = TextNode.builder()
				.name(ctx.IDENTIFIER() == null ? null : ctx.IDENTIFIER().getText().toLowerCase())
				.texts(texts)
				.method(ctx.method() == null ? null : TextMethod.valueOf(ctx.method().getText().toUpperCase()))
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
	public TextNode visitFragmentDirective(GameParser.FragmentDirectiveContext ctx) {
		List<String> texts = ctx.textElement().stream()
				.map(c -> ((TextElementNode) visit(c)).getText())
				.collect(Collectors.toList());
		TextNode node = TextNode.builder()
				.name(ctx.IDENTIFIER() == null ? null : ctx.IDENTIFIER().getText().toLowerCase())
				.texts(texts)
				.method(ctx.method() == null ? null : TextMethod.valueOf(ctx.method().getText().toUpperCase()))
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
	public ActionNode visitActionDirective(ActionDirectiveContext ctx) {
		ActionNode node = root.getActions().computeIfAbsent(ctx.arg1.getText().toLowerCase(), k -> new ActionNode());
		node.setArg1(ctx.arg1.getText().toLowerCase());
		node.getActionCodes().add(ActionCode.builder()
				.arg2(ctx.arg2 == null ? null : ctx.arg2.getText().toLowerCase())
				.code((BlockNode) visit(ctx.block()))
				.build());
		return node;
	}

	@Override
	public ProcNode visitProcDirective(ProcDirectiveContext ctx) {
		ProcNode node = ProcNode.builder()
				.name(ctx.name.getText().toLowerCase())
				.args(ctx.IDENTIFIER().subList(1, ctx.IDENTIFIER().size()).stream()
						.map(i -> i.getText().toLowerCase())
						.collect(Collectors.toList()))
				.code((BlockNode) visit(ctx.block()))
				.build();
		if (node.getName() != null) {
			if (root.getIdentifiers().containsKey(node.getName())) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + node.getName() + "\"");
			}
			root.getIdentifiers().put(node.getName(), node);
		}
		root.getProcs().put(ctx.name.getText().toLowerCase(), node);
		return node;
	}

	@Override
	public InitialNode visitInitialDirective(InitialDirectiveContext ctx) {
		InitialNode node = InitialNode.builder()
				.code((BlockNode) visit(ctx.block()))
				.build();
		root.getInits().add(node);
		return node;
	}

	@Override
	public RepeatNode visitRepeatDirective(RepeatDirectiveContext ctx) {
		RepeatNode node = RepeatNode.builder()
				.code((BlockNode) visit(ctx.block()))
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
					clause.setValue(NumberLiteralNode.builder()
							.number(0)
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
		return new StateClauseNode(ctx.IDENTIFIER().getText().toLowerCase(), ctx.expression() == null ? null : (ExprNode) visit(ctx.expression()));
	}

	@Override
	public BaseNode visitVariableDirective(VariableDirectiveContext ctx) {
		List<VariableNode> varList = root.getVariables();
		for (TerminalNode idCtx : ctx.IDENTIFIER()) {
			String varName = idCtx.getText().toLowerCase();
			VariableNode node = VariableNode.builder().variable(varName).build();
			if (root.getIdentifiers().containsKey(varName)) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + varName + "\"");
			}
			root.getIdentifiers().put(varName, node);
			varList.add(node);
		}
		return root;
	}

	@Override
	public ArrayNode visitArrayDirective(GameParser.ArrayDirectiveContext ctx) {
		ArrayNode node = ArrayNode.builder()
				.name(ctx.IDENTIFIER().getText().toLowerCase())
				.size(Integer.parseInt(ctx.NUM_LITERAL().getText()))
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
	public PlaceNode visitPlaceDirective(GameParser.PlaceDirectiveContext ctx) {
		String briefDescription = ((TextElementNode) visit(ctx.textElement(0))).getText();
		String longDescription = ctx.textElement(1) == null ? null : ((TextElementNode) visit(ctx.textElement(1))).getText();
		PlaceNode node = PlaceNode.builder()
				.names(ctx.IDENTIFIER().stream()
						.map(i -> i.getText().toLowerCase())
						.collect(Collectors.toList()))
				.inVocabulary("+".equals(ctx.getChild(1).toString()))
				.briefDescription(briefDescription)
				.longDescription(longDescription)
				.code((BlockNode) visit(ctx.optBlock()))
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
	public ObjectNode visitObjectDirective(GameParser.ObjectDirectiveContext ctx) {
		String inventoryDescription = ((TextElementNode) visit(ctx.textElement(0))).getText();
		String briefDescription = ctx.textElement(1) == null ? null : ((TextElementNode) visit(ctx.textElement(1))).getText();
		String longDescription = ctx.textElement(2) == null ? null : ((TextElementNode) visit(ctx.textElement(2))).getText();
		ObjectNode node = ObjectNode.builder()
				.names(ctx.IDENTIFIER().stream()
						.map(i -> i.getText().toLowerCase())
						.collect(Collectors.toList()))
				.inVocabulary(!"-".equals(ctx.getChild(1).toString()))
				.inventoryDescription(inventoryDescription)
				.briefDescription(briefDescription)
				.longDescription(longDescription)
				.code((BlockNode) visit(ctx.optBlock()))
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
	public BlockNode visitBlock(BlockContext ctx) {
		return BlockNode.builder()
				.statements(ctx.statement().stream()
						.map(s -> (StatementNode) visit(s))
						.collect(Collectors.toList()))
				.build();
	}

	@Override
	public StatementNode visitLocalVariableDeclarationStatement(LocalVariableDeclarationStatementContext ctx) {
		return LocalVariableDeclarationStatementNode.builder()
				.declarators(((LocalVariableDeclarationNode) visit(ctx.localVariableDeclaration())).getDeclarators())
				.build();
	}

	@Override
	public StatementNode visitEmptyStatement(EmptyStatementContext ctx) {
		return new EmptyStatementNode();
	}

	@Override
	public BaseNode visitLabeledStatement(LabeledStatementContext ctx) {
		StatementNode node = (StatementNode) visit(ctx.statement());
		node.setLabel(getIdentifierNode(ctx.IDENTIFIER()).getName());
		return node;
	}

	@Override
	public BaseNode visitLabeledStatementNoShortIf(LabeledStatementNoShortIfContext ctx) {
		StatementNode node = (StatementNode) visit(ctx.statementNoShortIf());
		node.setLabel(getIdentifierNode(ctx.IDENTIFIER()).getName());
		return node;
	}

	@Override
	public BaseNode visitExpressionStatement(ExpressionStatementContext ctx) {
		return ExpressionStatementNode.builder()
				.expression((ExprNode) visit(ctx.statementExpression()))
				.build();
	}

	@Override
	public BaseNode visitIfThenStatement(IfThenStatementContext ctx) {
		return IfStatementNode.builder()
				.expression((ExprNode) visit(ctx.expression()))
				.thenStatement((StatementNode) visit(ctx.statement()))
				.build();
	}

	@Override
	public BaseNode visitIfThenElseStatement(IfThenElseStatementContext ctx) {
		return IfStatementNode.builder()
				.expression((ExprNode) visit(ctx.expression()))
				.thenStatement((StatementNode) visit(ctx.statementNoShortIf()))
				.elseStatement((StatementNode) visit(ctx.statement()))
				.build();
	}

	@Override
	public BaseNode visitIfThenElseStatementNoShortIf(IfThenElseStatementNoShortIfContext ctx) {
		return IfStatementNode.builder()
				.expression((ExprNode) visit(ctx.expression()))
				.thenStatement((StatementNode) visit(ctx.statementNoShortIf().get(0)))
				.elseStatement((StatementNode) visit(ctx.statementNoShortIf().get(1)))
				.build();
	}

	@Override
	public BaseNode visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
		return LocalVariableDeclarationNode.builder()
				.declarators(ctx.variableDeclarator().stream()
						.map(d -> (VariableDeclaratorNode) visit(d))
						.collect(Collectors.toList()))
				.build();
	}

	@Override
	public BaseNode visitWhileStatement(WhileStatementContext ctx) {
		return WhileStatementNode.builder()
				.expression((ExprNode) visit(ctx.expression()))
				.statement((StatementNode) visit(ctx.statement()))
				.build();
	}

	@Override
	public BaseNode visitWhileStatementNoShortIf(WhileStatementNoShortIfContext ctx) {
		return WhileStatementNode.builder()
				.postTest(false)
				.expression((ExprNode) visit(ctx.expression()))
				.statement((StatementNode) visit(ctx.statementNoShortIf()))
				.build();
	}

	@Override
	public BaseNode visitRepeatStatement(RepeatStatementContext ctx) {
		return WhileStatementNode.builder()
				.postTest(true)
				.expression((ExprNode) visit(ctx.expression()))
				.statement((StatementNode) visit(ctx.statement()))
				.build();
	}

	@Override
	public BaseNode visitBasicForStatement(BasicForStatementContext ctx) {
		List<StatementNode> init;
		if (ctx.forInit() != null && ctx.forInit().localVariableDeclaration() != null) {
			init = Collections.singletonList(LocalVariableDeclarationStatementNode.builder()
					.declarators(
							((LocalVariableDeclarationNode) visit(ctx.forInit().localVariableDeclaration())).getDeclarators())
					.build());
		} else if (ctx.forInit() != null) {
			init = ((StatementExpressionListNode) visit(ctx.forInit())).getStatements();
		} else {
			init = Collections.emptyList();
		}
		return BasicForStatementNode.builder()
				.init(init)
				.test((ExprNode) visit(ctx.expression()))
				.update(((StatementExpressionListNode) visit(ctx.forUpdate().statementExpressionList())).getStatements())
				.statement((StatementNode) visit(ctx.statement()))
				.build();
	}

	@Override
	public BaseNode visitBasicForStatementNoShortIf(BasicForStatementNoShortIfContext ctx) {
		List<StatementNode> init;
		if (ctx.forInit().localVariableDeclaration() != null) {
			init = Collections.singletonList((StatementNode) visit(ctx.forInit().localVariableDeclaration()));
		} else {
			init = ((StatementExpressionListNode) visit(ctx.forInit())).getStatements();
		}
		return BasicForStatementNode.builder()
				.init(init)
				.test((ExprNode) visit(ctx.expression()))
				.update(((StatementExpressionListNode) visit(ctx.forUpdate().statementExpressionList())).getStatements())
				.statement((StatementNode) visit(ctx.statementNoShortIf()))
				.build();
	}

	@Override
	public BaseNode visitEnhancedForStatement(EnhancedForStatementContext ctx) {
		return EnhancedForStatementNode.builder()
				.identifier(getIdentifierNode(ctx.IDENTIFIER()))
				.expression((ExprNode) visit(ctx.expression()))
				.statement((StatementNode) visit(ctx.statement()))
				.build();
	}

	@Override
	public BaseNode visitEnhancedForStatementNoShortIf(EnhancedForStatementNoShortIfContext ctx) {
		return EnhancedForStatementNode.builder()
				.identifier(getIdentifierNode(ctx.IDENTIFIER()))
				.expression((ExprNode) visit(ctx.expression()))
				.statement((StatementNode) visit(ctx.statementNoShortIf()))
				.build();
	}

	@Override
	public BaseNode visitBreakStatement(BreakStatementContext ctx) {
		ControlType controlType = ctx.PROC() != null ? ControlType.PROC : ctx.REPEAT() != null ? ControlType.REPEAT : ControlType.CODE;
		String identifier = ctx.IDENTIFIER() == null ? null : getIdentifierNode(ctx.IDENTIFIER()).getName();
		return BreakStatementNode.builder()
				.identifier(identifier)
				.controlType(controlType)
				.build();
	}

	@Override
	public BaseNode visitContinueStatement(ContinueStatementContext ctx) {
		ControlType controlType = ctx.PROC() != null ? ControlType.PROC : ctx.REPEAT() != null ? ControlType.REPEAT : ControlType.CODE;
		String identifier = ctx.IDENTIFIER() == null ? null : getIdentifierNode(ctx.IDENTIFIER()).getName();
		return ContinueStatementNode.builder()
				.identifier(identifier)
				.controlType(controlType)
				.build();
	}

	@Override
	public BaseNode visitReturnStatement(ReturnStatementContext ctx) {
		return ReturnStatementNode.builder()
				.expression(
						ctx.expression() == null ?
								NumberLiteralNode.builder().number(0).build() :
								(ExprNode) visit(ctx.expression()))
				.build();
	}

	@Override
	public BaseNode visitStatementExpressionList(StatementExpressionListContext ctx) {
		return StatementExpressionListNode.builder()
				.statements(ctx.statementExpression().stream()
						.map(s -> ExpressionStatementNode.builder()
								.expression((ExprNode) visit(s))
								.build())
						.collect(Collectors.toList())
				)
				.build();
	}

	@Override
	public BaseNode visitVariableDeclarator(VariableDeclaratorContext ctx) {
		return VariableDeclaratorNode.builder()
				.identifier(getIdentifierNode(ctx.IDENTIFIER()))
				.expression(ctx.expression() == null ? null : (ExprNode) visit(ctx.expression()))
				.build();
	}

	@Override
	public BaseNode visitParenthesizedExpression(ParenthesizedExpressionContext ctx) {
		return visit(ctx.expression());
	}

	@Override
	public BaseNode visitArrayAccess(ArrayAccessContext ctx) {
		return ArrayAccessNode.builder()
				.arrayName(ctx.IDENTIFIER().getText().toLowerCase())
				.index((ExprNode) visit(ctx.expression()))
				.build();
	}

	@Override
	public BaseNode visitFunctionInvocation(FunctionInvocationContext ctx) {
		if (ctx.IDENTIFIER() != null) {
			return FunctionInvocationNode.builder()
					.identifier(getIdentifierNode(ctx.IDENTIFIER()))
					.parameters(ctx.expression().stream()
							.map(p -> (ExprNode) visit(p))
							.collect(Collectors.toList()))
					.build();
		} else {
			return FunctionInvocationNode.builder()
					.internalFunction(ctx.internalFunction().getText().toLowerCase())
					.parameters(ctx.expression().stream()
							.map(p -> (ExprNode) visit(p))
							.collect(Collectors.toList()))
					.build();
		}
	}

	@Override
	public BaseNode visitAssignment(AssignmentContext ctx) {
		return AssignmentNode.builder()
				.assignmentOperator(AssignmentOperator.findOperator(ctx.assignmentOperator().getText()))
				.left((ExprNode) visit(ctx.lvalue()))
				.right((ExprNode) visit(ctx.expression()))
				.build();
	}

	@Override
	public BaseNode visitQueryExpression(QueryExpressionContext ctx) {
		return QueryNode.builder()
				.expression((ExprNode) visit(ctx.conditionalOrExpression()))
				.trueExpression((ExprNode) visit(ctx.expression()))
				.falseExpression((ExprNode) visit(ctx.conditionalExpression()))
				.build();
	}

	@Override
	public BaseNode visitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(BinaryNode.Operator.OR)
					.left((ExprNode) visit(ctx.conditionalOrExpression()))
					.right((ExprNode) visit(ctx.conditionalAndExpression()))
					.build();
		}
	}

	@Override
	public BaseNode visitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(BinaryNode.Operator.AND)
					.left((ExprNode) visit(ctx.conditionalAndExpression()))
					.right((ExprNode) visit(ctx.inclusiveOrExpression()))
					.build();
		}
	}

	@Override
	public BaseNode visitInclusiveOrExpression(InclusiveOrExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(BinaryNode.Operator.BITOR)
					.left((ExprNode) visit(ctx.inclusiveOrExpression()))
					.right((ExprNode) visit(ctx.exclusiveOrExpression()))
					.build();
		}
	}

	@Override
	public BaseNode visitExclusiveOrExpression(ExclusiveOrExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(BinaryNode.Operator.XOR)
					.left((ExprNode) visit(ctx.exclusiveOrExpression()))
					.right((ExprNode) visit(ctx.andExpression()))
					.build();
		}
	}

	@Override
	public BaseNode visitAndExpression(AndExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(BinaryNode.Operator.BITOR)
					.left((ExprNode) visit(ctx.andExpression()))
					.right((ExprNode) visit(ctx.equalityExpression()))
					.build();
		}
	}

	@Override
	public BaseNode visitEqualityExpression(EqualityExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(Operator.findOperator(ctx.getChild(1).getText()))
					.left((ExprNode) visit(ctx.equalityExpression()))
					.right((ExprNode) visit(ctx.relationalExpression()))
					.build();
		}
	}

	@Override
	public BaseNode visitRelationalExpression(RelationalExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(Operator.findOperator(ctx.getChild(1).getText()))
					.left((ExprNode) visit(ctx.relationalExpression()))
					.right((ExprNode) visit(ctx.shiftExpression()))
					.build();
		}
	}

	@Override
	public BaseNode visitShiftExpression(ShiftExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(Operator.findOperator(ctx.getChild(1).getText()))
					.left((ExprNode) visit(ctx.shiftExpression()))
					.right((ExprNode) visit(ctx.additiveExpression()))
					.build();
		}
	}

	@Override
	public BaseNode visitAdditiveExpression(AdditiveExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(Operator.findOperator(ctx.getChild(1).getText()))
					.left((ExprNode) visit(ctx.additiveExpression()))
					.right((ExprNode) visit(ctx.multiplicativeExpression()))
					.build();
		}
	}

	@Override
	public BaseNode visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(Operator.findOperator(ctx.getChild(1).getText()))
					.left((ExprNode) visit(ctx.multiplicativeExpression()))
					.right((ExprNode) visit(ctx.unaryExpression()))
					.build();
		}
	}

	@Override
	public BaseNode visitUnaryExpression(UnaryExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else if ("+".equals(ctx.getChild(0).getText())) {
			return visit(ctx.getChild(1));
		} else {
			return UnaryNode.builder()
					.operator(UnaryOperator.MINUS)
					.expression((ExprNode) visit(ctx.unaryExpression()))
					.build();
		}
	}

	@Override
	public BaseNode visitPreIncrementOrDecrementExpression(PreIncrementOrDecrementExpressionContext ctx) {
		return UnaryNode.builder()
				.operator("++".equals(ctx.getChild(0).getText()) ? UnaryOperator.PREINC : UnaryOperator.PREDEC)
				.expression((ExprNode) visit(ctx.unaryExpression()))
				.build();
	}

	@Override
	public BaseNode visitUnaryExpressionNotPlusMinus(UnaryExpressionNotPlusMinusContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return UnaryNode.builder()
					.operator("~".equals(ctx.getChild(0).getText()) ? UnaryOperator.BITNOT : UnaryOperator.NOT)
					.expression((ExprNode) visit(ctx.unaryExpression()))
					.build();
		}
	}

	@Override
	public BaseNode visitPostIncrementOrDecrementExpression(PostIncrementOrDecrementExpressionContext ctx) {
		if (ctx.getChild(1) == null) {
			errorReporter.reportError(ctx, "Weird stuff");
		}
		return UnaryNode.builder()
				.operator("++".equals(ctx.getChild(1).getText()) ? UnaryOperator.POSTINC : UnaryOperator.POSTDEC)
				.expression((ExprNode) visit(ctx.primary()))
				.build();
	}

	@Override
	public BaseNode visitInstanceofExpression(InstanceofExpressionContext ctx) {
		IdentifierType identifierType;
		try {
			identifierType = IdentifierType.valueOf(ctx.getText().toUpperCase());
		} catch (IllegalArgumentException e) {
			identifierType = null;
		}
		return new InstanceofNode(getIdentifierNode(ctx.IDENTIFIER()), identifierType);
	}

	@Override
	public BaseNode visitRefExpression(RefExpressionContext ctx) {
		return new RefNode(getIdentifierNode(ctx.IDENTIFIER()));
	}

	@Override
	public BaseNode visitTextElement(TextElementContext ctx) {
		if (ctx.STRING_LITERAL() != null) {
			return getTextLiteralNode(ctx.STRING_LITERAL());
		} else {
			return getTextBlockNode(ctx.TEXT_BLOCK());
		}
	}

	@Override
	public BaseNode visitLiteral(LiteralContext ctx) {
		if (ctx.STRING_LITERAL() != null) {
			return getTextLiteralNode(ctx.STRING_LITERAL());
		} else if (ctx.CHAR_LITERAL() != null) {
			return NumberLiteralNode.builder()
					.number(TextUtils.cleanCharLiteral(ctx.CHAR_LITERAL().getText()))
					.build();
		} else if (ctx.BOOL_LITERAL() != null) {
			return NumberLiteralNode.builder()
					.number("true".equals(ctx.BOOL_LITERAL().getText()) ? 1 : 0)
					.build();
		} else if (ctx.NULL_LITERAL() != null) {
			return NumberLiteralNode.builder()
					.number(0)
					.build();
		} else {
			return NumberLiteralNode.builder()
					.number(Integer.parseInt(ctx.getText()))
					.build();
		}
	}

	public IdentifierNode getIdentifierNode(TerminalNode identifier) {
		return new IdentifierNode(identifier.getText().toLowerCase());
	}

	@Override
	public BaseNode visitIdentifierReference(IdentifierReferenceContext ctx) {
		return new IdentifierNode(ctx.getText().toLowerCase());
	}

	private static @NonNull TextElementNode getTextLiteralNode(TerminalNode ctx) {
		return new TextElementNode(TextUtils.cleanStringLiteral(ctx.getText()));
	}

	private static @NonNull TextElementNode getTextBlockNode(TerminalNode ctx) {
		return new TextElementNode(TextUtils.cleanTextBlock(ctx.getText()));
	}
}
