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
import org.antlr.v4.runtime.Token;
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

	// directive+ EOF
	@Override
	public GameNode visitGame(GameParser.GameContext ctx) {
		visitChildren(ctx);
		return root;
	}

	// INCLUDE STRING_LITERAL (COMMA BOOL_LITERAL)? SEMI
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

	// INFO gameDescriptor (COMMA gameDescriptor)* SEMI
	@Override
	public BaseNode visitInfoPragma(InfoPragmaContext ctx) {
		for (GameDescriptorContext gdc : ctx.gameDescriptor()) {
			String key = gdc.IDENTIFIER().getText();
			String value = TextUtils.cleanStringLiteral(gdc.STRING_LITERAL().getText());
			if ("name".equals(key)) {
				root.setName(value);
			}
			if ("version".equals(key)) {
				root.setVersion(value);
			}
			if ("author".equals(key)) {
				root.setAuthor(value);
			}
			if ("date".equals(key)) {
				root.setDate(value);
			}
		}
		return root;
	}

	// FLAGS flagType flagClause (COMMA flagClause)* SEMI
	@Override
	public FlagNode visitFlagDirective(FlagDirectiveContext ctx) {
		FlagType type = FlagType.valueOf(ctx.getChild(1).getText().toUpperCase());
		FlagNode node = FlagNode.builder()
				.type(type)
				.flags(ctx.flagClause().stream()
						.map(c -> c.getText().toLowerCase())
						.collect(Collectors.toList()))
				.sourceLocation(new SourceLocation(ctx))
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

	// VERB verb (verb)* SEMI
	@Override
	public VerbNode visitVerbDirective(GameParser.VerbDirectiveContext ctx) {
		VerbNode node = VerbNode.builder()
				.name(TextUtils.cleanStringLiteral(ctx.verb(0).getText()).toLowerCase())
				.verbs(ctx.verb().stream()
						.map(v -> TextUtils.cleanStringLiteral(v.STRING_LITERAL().getText()).toLowerCase())
						.collect(Collectors.toList()))
				.noise(false)
				.sourceLocation(new SourceLocation(ctx))
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

	// NOISE verb (verb)* SEMI
	@Override
	public VerbNode visitNoiseDirective(GameParser.NoiseDirectiveContext ctx) {
		VerbNode node = VerbNode.builder()
				.verbs(ctx.verb().stream()
						.map(n -> TextUtils.cleanStringLiteral(n.STRING_LITERAL().getText()).toLowerCase())
						.collect(Collectors.toList()))
				.noise(false)
				.sourceLocation(new SourceLocation(ctx))
				.build();
		root.getNoise().addAll(node.getVerbs());
		return node;
	}

	// TEXT method? IDENTIFIER textElement+ SEMI
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
				.sourceLocation(new SourceLocation(ctx))
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

	// FRAGMENT method? IDENTIFIER textElement+ SEMI
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
				.sourceLocation(new SourceLocation(ctx))
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

	// ACTION arg1=verb (arg2=verb)? block
	@Override
	public ActionNode visitActionDirective(ActionDirectiveContext ctx) {
		String name = TextUtils.cleanStringLiteral(ctx.arg1.getText()).toLowerCase();
		ActionNode node = root.getActions().computeIfAbsent(name, k -> new ActionNode());
		node.setSourceLocation(new SourceLocation(ctx));
		node.setArg1(name);
		node.getActionCodes().add(ActionCode.builder()
				.arg2(ctx.arg2 == null ? null : TextUtils.cleanStringLiteral(ctx.arg2.getText()).toLowerCase())
				.code((BlockNode) visit(ctx.block()))
				.sourceLocation(new SourceLocation(ctx.block()))
				.build());
		return node;
	}

	// PROC name=IDENTIFIER (IDENTIFIER)* block
	@Override
	public ProcNode visitProcDirective(ProcDirectiveContext ctx) {
		ProcNode node = ProcNode.builder()
				.name(ctx.name.getText().toLowerCase())
				.args(ctx.IDENTIFIER().subList(1, ctx.IDENTIFIER().size()).stream()
						.map(i -> i.getText().toLowerCase())
						.collect(Collectors.toList()))
				.code((BlockNode) visit(ctx.block()))
				.sourceLocation(new SourceLocation(ctx.block()))
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

	// INITIAL block
	@Override
	public InitialNode visitInitialDirective(InitialDirectiveContext ctx) {
		InitialNode node = InitialNode.builder()
				.code((BlockNode) visit(ctx.block()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
		root.getInits().add(node);
		return node;
	}

	// REPEAT block
	@Override
	public RepeatNode visitRepeatDirective(RepeatDirectiveContext ctx) {
		RepeatNode node = RepeatNode.builder()
				.code((BlockNode) visit(ctx.block()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
		root.getRepeats().add(node);
		return node;
	}

	// STATE stateClause (COMMA stateClause)* SEMI
	@Override
	public StateNode visitStateDirective(StateDirectiveContext ctx) {
		StateNode node = new StateNode();
		node.setSourceLocation(new SourceLocation(ctx));

		boolean first = true;
		for (StateClauseContext childCtx : ctx.stateClause()) {
			StateClauseNode clause = (StateClauseNode) visit(childCtx);
			if (first) {
				first = false;
				if (clause.getValue() == null) {
					clause.setValue(NumberLiteralNode.builder()
							.number(0)
							.sourceLocation(new SourceLocation(childCtx))
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

	// IDENTIFIER (EQUAL expression)?
	@Override
	public StateClauseNode visitStateClause(StateClauseContext ctx) {
		return new StateClauseNode(
				ctx.IDENTIFIER().getText().toLowerCase(),
				ctx.expression() == null ? null : (ExprNode) visit(ctx.expression()),
				new SourceLocation(ctx));
	}

	// IDENTIFIER | IDENTIFIER LBRACK NUM_LITERAL RBRACK
	@Override
	public BaseNode visitGlobalDeclarator(GlobalDeclaratorContext ctx) {
		if (ctx.NUM_LITERAL() == null) {
			List<VariableNode> varList = root.getVariables();
			TerminalNode idCtx = ctx.IDENTIFIER();
			String varName = idCtx.getText().toLowerCase();
			VariableNode node = VariableNode.builder()
					.variable(varName)
					.sourceLocation(new SourceLocation(ctx))
					.build();
			if (root.getIdentifiers().containsKey(varName)) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + varName + "\"");
			}
			root.getIdentifiers().put(varName, node);
			varList.add(node);
			return node;
		} else {
			ArrayNode node = ArrayNode.builder()
					.name(ctx.IDENTIFIER().getText().toLowerCase())
					.size(Integer.parseInt(ctx.NUM_LITERAL().getText()))
					.sourceLocation(new SourceLocation(ctx))
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
	}

	// PLACE IDENTIFIER (EQUAL verb)* textElement? textElement? optionalBlock
	@Override
	public PlaceNode visitPlaceDirective(GameParser.PlaceDirectiveContext ctx) {
		String briefDescription = ((TextElementNode) visit(ctx.textElement(0))).getText();
		String longDescription = ctx.textElement(1) == null ? null : ((TextElementNode) visit(ctx.textElement(1))).getText();
		PlaceNode node = PlaceNode.builder()
				.name(ctx.IDENTIFIER().getText().toLowerCase())
				.verbs(ctx.verb().stream()
						.map(i -> TextUtils.cleanStringLiteral(i.STRING_LITERAL().getText()).toLowerCase())
						.collect(Collectors.toList()))
				.briefDescription(briefDescription)
				.longDescription(longDescription)
				.commands(ctx.verbCommand().stream()
						.map(vc -> (VerbCommandNode) visitVerbCommand(vc))
						.collect(Collectors.toMap(VerbCommandNode::getVerb, VerbCommandNode::getBlock)))
				.sourceLocation(new SourceLocation(ctx))
				.build();
		if (root.getIdentifiers().containsKey(node.getName())) {
			errorReporter.reportError(ctx, "Duplicate identifier \"" + node.getName() + "\"");
		}
		root.getIdentifiers().put(node.getName(), node);
		if (root.getVerbs().containsKey(node.getName())) {
			errorReporter.reportError(ctx, "Duplicate verb \"" + node.getName() + "\"");
		}
		// Add first word to vocabulary.
		root.getVerbs().put(node.getName(), node);
		for (String var : node.getVerbs()) {
			if (root.getIdentifiers().containsKey(var)) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + var + "\"");
			}
			root.getIdentifiers().put(var, node);
		}
		root.getPlaces().add(node);
		return node;
	}

	// OBJECT SUB? IDENTIFIER (EQUAL verb)* textElement textElement? textElement? optionalBlock
	@Override
	public ObjectNode visitObjectDirective(GameParser.ObjectDirectiveContext ctx) {
		String inventoryDescription = ((TextElementNode) visit(ctx.textElement(0))).getText();
		String briefDescription = ctx.textElement(1) == null ? null : ((TextElementNode) visit(ctx.textElement(1))).getText();
		String longDescription = ctx.textElement(2) == null ? null : ((TextElementNode) visit(ctx.textElement(2))).getText();
		ObjectNode node = ObjectNode.builder()
				.name(ctx.IDENTIFIER().getText().toLowerCase())
				.verbs(ctx.verb().stream()
						.map(i -> TextUtils.cleanStringLiteral(i.STRING_LITERAL().getText()).toLowerCase())
						.collect(Collectors.toList()))
				.inVocabulary(!"-".equals(ctx.getChild(1).toString()))
				.inventoryDescription(inventoryDescription)
				.briefDescription(briefDescription)
				.longDescription(longDescription)
				.commands(ctx.verbCommand().stream()
						.map(vc -> (VerbCommandNode) visitVerbCommand(vc))
						.collect(Collectors.toMap(VerbCommandNode::getVerb, VerbCommandNode::getBlock)))
				.sourceLocation(new SourceLocation(ctx))
				.build();
		if (root.getIdentifiers().containsKey(node.getName())) {
			errorReporter.reportError(ctx, "Duplicate identifier \"" + node.getName() + "\"");
		}
		root.getIdentifiers().put(node.getName(), node);
		if (node.isInVocabulary()) {
			if (root.getVerbs().containsKey(node.getName())) {
				errorReporter.reportError(ctx, "Duplicate verb \"" + node.getName() + "\"");
			}
			// Add first word to vocabulary.
			root.getVerbs().put(node.getName(), node);
		}
		for (String var : node.getVerbs()) {
			if (root.getIdentifiers().containsKey(var)) {
				errorReporter.reportError(ctx, "Duplicate identifier \"" + var + "\"");
			}
			root.getIdentifiers().put(var, node);
		}
		root.getObjects().add(node);
		return node;
	}

	// verb COLON block
	@Override
	public BaseNode visitVerbCommand(VerbCommandContext ctx) {
		return VerbCommandNode.builder()
				.verb(TextUtils.cleanStringLiteral(ctx.verb().getText()).toLowerCase())
				.block(visitBlock(ctx.block()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// LBRACE statement* RBRACE
	@Override
	public BlockNode visitBlock(BlockContext ctx) {
		return BlockNode.builder()
				.statements(ctx.statement().stream()
						.map(s -> (StatementNode) visit(s))
						.collect(Collectors.toList()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// localVariableDeclaration SEMI
	@Override
	public StatementNode visitLocalVariableDeclarationStatement(LocalVariableDeclarationStatementContext ctx) {
		return LocalVariableDeclarationStatementNode.builder()
				.declarators(((LocalVariableDeclarationNode) visit(ctx.localVariableDeclaration())).getDeclarators())
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// SEMI
	@Override
	public StatementNode visitEmptyStatement(EmptyStatementContext ctx) {
		EmptyStatementNode node = new EmptyStatementNode();
		node.setSourceLocation(new SourceLocation(ctx));
		return node;
	}

	@Override
	public BaseNode visitExpressionStatement(ExpressionStatementContext ctx) {
		return ExpressionStatementNode.builder()
				.expression((ExprNode) visit(ctx.statementExpression()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// IF LPAREN expression RPAREN block (ELSE IF LPAREN expression RPAREN block)* (ELSE block)?
	@Override
	public BaseNode visitIfStatement(IfStatementContext ctx) {
		return IfStatementNode.builder()
				.expressions(ctx.expression().stream()
						.map(e -> (ExprNode) visit(e))
						.toList())
				.thenStatements(ctx.block().stream()
						.map(b -> (StatementNode) visit(b))
						.toList())
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// VAR variableDeclarator (COMMA variableDeclarator)*
	@Override
	public BaseNode visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
		return LocalVariableDeclarationNode.builder()
				.declarators(ctx.variableDeclarator().stream()
						.map(d -> (VariableDeclaratorNode) visit(d))
						.collect(Collectors.toList()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// optionalLabel WHILE LPAREN expression RPAREN block
	@Override
	public BaseNode visitWhileStatement(WhileStatementContext ctx) {
		String label = null;
		if (ctx.optionalLabel() != null && ctx.optionalLabel().IDENTIFIER() != null) {
			label = getIdentifierNode(ctx.optionalLabel().IDENTIFIER(), new SourceLocation(ctx.optionalLabel())).getName();
		}
		return WhileStatementNode.builder()
				.label(label)
				.expression((ExprNode) visit(ctx.expression()))
				.statement((StatementNode) visit(ctx.block()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// optionalLabel REPEAT block UNTIL LPAREN expression RPAREN SEMI
	@Override
	public BaseNode visitRepeatStatement(RepeatStatementContext ctx) {
		String label = null;
		if (ctx.optionalLabel() != null && ctx.optionalLabel().IDENTIFIER() != null) {
			label = getIdentifierNode(ctx.optionalLabel().IDENTIFIER(), new SourceLocation(ctx.optionalLabel())).getName();
		}
		return WhileStatementNode.builder()
				.label(label)
				.postTest(true)
				.expression((ExprNode) visit(ctx.expression()))
				.statement((StatementNode) visit(ctx.block()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// optionalLabel FOR LPAREN forInit? SEMI expression? SEMI forUpdate? RPAREN block
	@Override
	public BaseNode visitBasicForStatement(BasicForStatementContext ctx) {
		String label = null;
		if (ctx.optionalLabel() != null && ctx.optionalLabel().IDENTIFIER() != null) {
			label = getIdentifierNode(ctx.optionalLabel().IDENTIFIER(), new SourceLocation(ctx.optionalLabel())).getName();
		}
		List<StatementNode> init;
		if (ctx.forInit() != null && ctx.forInit().localVariableDeclaration() != null) {
			init = Collections.singletonList(LocalVariableDeclarationStatementNode.builder()
					.declarators(
							((LocalVariableDeclarationNode) visit(ctx.forInit().localVariableDeclaration())).getDeclarators())
					.sourceLocation(new SourceLocation(ctx))
					.build());
		} else if (ctx.forInit() != null) {
			init = ((StatementExpressionListNode) visit(ctx.forInit())).getStatements();
		} else {
			init = Collections.emptyList();
		}
		return BasicForStatementNode.builder()
				.label(label)
				.init(init)
				.test((ExprNode) visit(ctx.expression()))
				.update(((StatementExpressionListNode) visit(ctx.forUpdate().statementExpressionList())).getStatements())
				.statement((StatementNode) visit(ctx.block()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// optionalLabel FOR LPAREN VAR IDENTIFIER COLON expression RPAREN block
	@Override
	public BaseNode visitEnhancedForStatement(EnhancedForStatementContext ctx) {
		String label = null;
		if (ctx.optionalLabel() != null && ctx.optionalLabel().IDENTIFIER() != null) {
			label = getIdentifierNode(ctx.optionalLabel().IDENTIFIER(), new SourceLocation(ctx.optionalLabel())).getName();
		}
		return EnhancedForStatementNode.builder()
				.label(label)
				.identifier(getIdentifierNode(ctx.IDENTIFIER(), getSourceLocation(ctx.IDENTIFIER())))
				.expression((ExprNode) visit(ctx.expression()))
				.statement((StatementNode) visit(ctx.block()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// BREAK (PROC | REPEAT | IDENTIFIER)? SEMI
	@Override
	public BaseNode visitBreakStatement(BreakStatementContext ctx) {
		ControlType controlType = ctx.PROC() != null ? ControlType.PROC : ctx.REPEAT() != null ? ControlType.REPEAT : ControlType.CODE;
		String identifier = ctx.IDENTIFIER() == null ? null : getIdentifierNode(ctx.IDENTIFIER(), getSourceLocation(ctx.IDENTIFIER())).getName();
		return BreakStatementNode.builder()
				.identifier(identifier)
				.controlType(controlType)
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// CONTINUE (PROC | REPEAT | IDENTIFIER)? SEMI
	@Override
	public BaseNode visitContinueStatement(ContinueStatementContext ctx) {
		ControlType controlType = ctx.PROC() != null ? ControlType.PROC : ctx.REPEAT() != null ? ControlType.REPEAT : ControlType.CODE;
		String identifier = ctx.IDENTIFIER() == null ? null : getIdentifierNode(ctx.IDENTIFIER(), getSourceLocation(ctx.IDENTIFIER())).getName();
		return ContinueStatementNode.builder()
				.identifier(identifier)
				.controlType(controlType)
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// RETURN expression? SEMI
	@Override
	public BaseNode visitReturnStatement(ReturnStatementContext ctx) {
		return ReturnStatementNode.builder()
				.expression(
						ctx.expression() == null ?
								NumberLiteralNode.builder()
										.number(0)
										.sourceLocation(getSourceLocation(ctx.SEMI()))
										.build() :
								(ExprNode) visit(ctx.expression()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// statementExpression (COMMA statementExpression)*
	@Override
	public BaseNode visitStatementExpressionList(StatementExpressionListContext ctx) {
		return StatementExpressionListNode.builder()
				.statements(ctx.statementExpression().stream()
						.map(s -> ExpressionStatementNode.builder()
								.expression((ExprNode) visit(s))
								.sourceLocation(new SourceLocation(s))
								.build())
						.collect(Collectors.toList())
				)
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	//IDENTIFIER (EQUAL expression)?
	@Override
	public BaseNode visitVariableDeclarator(VariableDeclaratorContext ctx) {
		return VariableDeclaratorNode.builder()
				.identifier(getIdentifierNode(ctx.IDENTIFIER(), getSourceLocation(ctx.IDENTIFIER())))
				.expression(ctx.expression() == null ? null : (ExprNode) visit(ctx.expression()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// LPAREN expression RPAREN
	@Override
	public BaseNode visitParenthesizedExpression(ParenthesizedExpressionContext ctx) {
		return visit(ctx.expression());
	}

	// IDENTIFIER LBRACK expression RBRACK
	@Override
	public BaseNode visitArrayAccess(ArrayAccessContext ctx) {
		return ArrayAccessNode.builder()
				.arrayName(ctx.IDENTIFIER().getText().toLowerCase())
				.index((ExprNode) visit(ctx.expression()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// IDENTIFIER LPAREN optionalExpressionList RPAREN
	// | STRING_LITERAL LPAREN optionalExpressionList RPAREN
	// | internalFunction LPAREN optionalExpressionList RPAREN
	@Override
	public BaseNode visitFunctionInvocation(FunctionInvocationContext ctx) {
		if (ctx.IDENTIFIER() != null) {
			return FunctionInvocationNode.builder()
					.identifier(getIdentifierNode(ctx.IDENTIFIER(), getSourceLocation(ctx.IDENTIFIER())))
					.parameters(((ExprListNode) visit(ctx.optionalExpressionList())).getExprNodes())
					.sourceLocation(new SourceLocation(ctx))
					.build();
		} else if (ctx.STRING_LITERAL() != null) {
			return FunctionInvocationNode.builder()
					.verbFunction(TextUtils.cleanStringLiteral(ctx.STRING_LITERAL().getText()).toLowerCase())
					.parameters(((ExprListNode) visit(ctx.optionalExpressionList())).getExprNodes())
					.sourceLocation(new SourceLocation(ctx))
					.build();
		} else {
			return FunctionInvocationNode.builder()
					.internalFunction(ctx.internalFunction().getText().toLowerCase())
					.parameters(((ExprListNode) visit(ctx.optionalExpressionList())).getExprNodes())
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// | expression (COMMA expression)*
	@Override
	public BaseNode visitOptionalExpressionList(OptionalExpressionListContext ctx) {
		List<ExprNode> exprNodeList;
		if (ctx.expression() == null) {
			exprNodeList = Collections.emptyList();
		} else {
			exprNodeList = ctx.expression().stream()
					.map(p -> (ExprNode) visit(p))
					.toList();
		}
		return ExprListNode.builder()
				.exprNodes(exprNodeList)
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// lvalue assignmentOperator expression
	@Override
	public BaseNode visitAssignment(AssignmentContext ctx) {
		return AssignmentNode.builder()
				.assignmentOperator(AssignmentOperator.findOperator(ctx.assignmentOperator().getText()))
				.left((ExprNode) visit(ctx.lvalue()))
				.right((ExprNode) visit(ctx.expression()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// conditionalOrExpression QUESTION expression COLON conditionalExpression
	@Override
	public BaseNode visitQueryExpression(QueryExpressionContext ctx) {
		return QueryNode.builder()
				.expression((ExprNode) visit(ctx.conditionalOrExpression()))
				.trueExpression((ExprNode) visit(ctx.expression()))
				.falseExpression((ExprNode) visit(ctx.conditionalExpression()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// conditionalAndExpression
	// | conditionalOrExpression OR conditionalAndExpression
	@Override
	public BaseNode visitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(BinaryNode.Operator.OR)
					.left((ExprNode) visit(ctx.conditionalOrExpression()))
					.right((ExprNode) visit(ctx.conditionalAndExpression()))
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// inclusiveOrExpression
	// | conditionalAndExpression AND inclusiveOrExpression
	@Override
	public BaseNode visitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(BinaryNode.Operator.AND)
					.left((ExprNode) visit(ctx.conditionalAndExpression()))
					.right((ExprNode) visit(ctx.inclusiveOrExpression()))
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// exclusiveOrExpression
	// | inclusiveOrExpression BITOR exclusiveOrExpression
	@Override
	public BaseNode visitInclusiveOrExpression(InclusiveOrExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(BinaryNode.Operator.BITOR)
					.left((ExprNode) visit(ctx.inclusiveOrExpression()))
					.right((ExprNode) visit(ctx.exclusiveOrExpression()))
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// andExpression
	// | exclusiveOrExpression CARET andExpression
	@Override
	public BaseNode visitExclusiveOrExpression(ExclusiveOrExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(BinaryNode.Operator.XOR)
					.left((ExprNode) visit(ctx.exclusiveOrExpression()))
					.right((ExprNode) visit(ctx.andExpression()))
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// relationalExpression
	// | andExpression BITAND relationalExpression
	@Override
	public BaseNode visitAndExpression(AndExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(BinaryNode.Operator.BITOR)
					.left((ExprNode) visit(ctx.andExpression()))
					.right((ExprNode) visit(ctx.relationalExpression()))
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// shiftExpression
	// | shiftExpression relationalOperator shiftExpression
	@Override
	public BaseNode visitRelationalExpression(RelationalExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(Operator.findOperator(ctx.getChild(1).getText()))
					.left((ExprNode) visit(ctx.shiftExpression(0)))
					.right((ExprNode) visit(ctx.shiftExpression(1)))
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// additiveExpression
	// | shiftExpression LSHIFT additiveExpression
	// | shiftExpression RSHIFT additiveExpression
	// | shiftExpression URSHIFT additiveExpression
	@Override
	public BaseNode visitShiftExpression(ShiftExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(Operator.findOperator(ctx.getChild(1).getText()))
					.left((ExprNode) visit(ctx.shiftExpression()))
					.right((ExprNode) visit(ctx.additiveExpression()))
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// multiplicativeExpression
	// | additiveExpression ADD multiplicativeExpression
	// | additiveExpression SUB multiplicativeExpression
	@Override
	public BaseNode visitAdditiveExpression(AdditiveExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(Operator.findOperator(ctx.getChild(1).getText()))
					.left((ExprNode) visit(ctx.additiveExpression()))
					.right((ExprNode) visit(ctx.multiplicativeExpression()))
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// unaryExpression
	// | multiplicativeExpression MUL unaryExpression
	// | multiplicativeExpression DIV unaryExpression
	// | multiplicativeExpression MOD unaryExpression
	@Override
	public BaseNode visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return BinaryNode.builder()
					.operator(Operator.findOperator(ctx.getChild(1).getText()))
					.left((ExprNode) visit(ctx.multiplicativeExpression()))
					.right((ExprNode) visit(ctx.unaryExpression()))
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// preIncrementOrDecrementExpression
	// | unaryExpressionNotPlusMinus
	// | ADD unaryExpression
	// | SUB unaryExpression
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
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// INC unaryExpression
	// | DEC unaryExpression
	@Override
	public BaseNode visitPreIncrementOrDecrementExpression(PreIncrementOrDecrementExpressionContext ctx) {
		return UnaryNode.builder()
				.operator("++".equals(ctx.getChild(0).getText()) ? UnaryOperator.PREINC : UnaryOperator.PREDEC)
				.expression((ExprNode) visit(ctx.unaryExpression()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// primary
	// | postIncrementOrDecrementExpression
	// | TILDE unaryExpression
	// | BANG unaryExpression
	@Override
	public BaseNode visitUnaryExpressionNotPlusMinus(UnaryExpressionNotPlusMinusContext ctx) {
		if (ctx.getChildCount() == 1) {
			return visit(ctx.getChild(0));
		} else {
			return UnaryNode.builder()
					.operator("~".equals(ctx.getChild(0).getText()) ? UnaryOperator.BITNOT : UnaryOperator.NOT)
					.expression((ExprNode) visit(ctx.unaryExpression()))
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// primary INC
	// | primary DEC
	@Override
	public BaseNode visitPostIncrementOrDecrementExpression(PostIncrementOrDecrementExpressionContext ctx) {
		if (ctx.getChild(1) == null) {
			errorReporter.reportError(ctx, "Weird stuff");
		}
		return UnaryNode.builder()
				.operator("++".equals(ctx.getChild(1).getText()) ? UnaryOperator.POSTINC : UnaryOperator.POSTDEC)
				.expression((ExprNode) visit(ctx.primary()))
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// IDENTIFIER INSTANCEOF refType
	@Override
	public BaseNode visitInstanceofExpression(InstanceofExpressionContext ctx) {
		IdentifierType identifierType;
		try {
			identifierType = IdentifierType.valueOf(ctx.getChild(2).getText().toUpperCase());
		} catch (IllegalArgumentException e) {
			identifierType = null;
		}
		return InstanceofNode.builder()
				.identifier(getIdentifierNode(ctx.IDENTIFIER(), getSourceLocation(ctx.IDENTIFIER())))
				.identifierType(identifierType)
				.sourceLocation(new SourceLocation(ctx))
				.build();
	}

	// MUL IDENTIFIER
	@Override
	public BaseNode visitDerefExpression(DerefExpressionContext ctx) {
		return new DerefNode(
				getIdentifierNode(ctx.IDENTIFIER(), getSourceLocation(ctx.IDENTIFIER())),
				new SourceLocation(ctx));
	}

	// BITAND IDENTIFIER
	@Override
	public BaseNode visitRefExpression(RefExpressionContext ctx) {
		return new RefNode(
				getIdentifierNode(ctx.IDENTIFIER(), getSourceLocation(ctx.IDENTIFIER())),
				new SourceLocation(ctx));
	}

	// TEXT_BLOCK | STRING_LITERAL
	@Override
	public BaseNode visitTextElement(TextElementContext ctx) {
		if (ctx.STRING_LITERAL() != null) {
			return getTextLiteralNode(ctx.STRING_LITERAL());
		} else {
			return getTextBlockNode(ctx.TEXT_BLOCK());
		}
	}

	// NUM_LITERAL
	// | BOOL_LITERAL
	// | CHAR_LITERAL
	// | STRING_LITERAL
	// | NULL_LITERAL
	@Override
	public BaseNode visitLiteral(LiteralContext ctx) {
		if (ctx.STRING_LITERAL() != null) {
			return getTextLiteralNode(ctx.STRING_LITERAL());
		} else if (ctx.CHAR_LITERAL() != null) {
			return NumberLiteralNode.builder()
					.number(TextUtils.cleanCharLiteral(ctx.CHAR_LITERAL().getText()))
					.sourceLocation(new SourceLocation(ctx))
					.build();
		} else if (ctx.BOOL_LITERAL() != null) {
			return NumberLiteralNode.builder()
					.number("true".equals(ctx.BOOL_LITERAL().getText()) ? 1 : 0)
					.sourceLocation(new SourceLocation(ctx))
					.build();
		} else if (ctx.NULL_LITERAL() != null) {
			return NumberLiteralNode.builder()
					.number(0)
					.sourceLocation(new SourceLocation(ctx))
					.build();
		} else {
			return NumberLiteralNode.builder()
					.number(Integer.parseInt(ctx.getText()))
					.sourceLocation(new SourceLocation(ctx))
					.build();
		}
	}

	// IDENTIFIER
	@Override
	public BaseNode visitIdentifierReference(IdentifierReferenceContext ctx) {
		return new IdentifierNode(ctx.getText().toLowerCase(), new SourceLocation(ctx));
	}

	public IdentifierNode getIdentifierNode(TerminalNode identifier, SourceLocation sourceLocation) {
		return new IdentifierNode(identifier.getText().toLowerCase(), sourceLocation);
	}

	private @NonNull TextElementNode getTextLiteralNode(TerminalNode ctx) {
		TextElementNode node = TextElementNode.builder()
				.text(TextUtils.cleanStringLiteral(ctx.getText()))
				.sourceLocation(getSourceLocation(ctx))
				.build();
		root.getTextElements().add(node);
		return node;
	}

	private @NonNull TextElementNode getTextBlockNode(TerminalNode ctx) {
		TextElementNode node = TextElementNode.builder()
				.text(TextUtils.cleanTextBlock(ctx.getText()))
				.sourceLocation(getSourceLocation(ctx))
				.build();
		root.getTextElements().add(node);
		return node;
	}

	private SourceLocation getSourceLocation(TerminalNode node) {
		Token token = node.getSymbol();
		String sourceName = token.getInputStream() == null ? null : token.getInputStream().getSourceName();
		return new SourceLocation(sourceName, token.getLine(), token.getCharPositionInLine());
	}
}
