parser grammar ACodeParser;

options { tokenVocab = ACodeLexer; }

input
	:	directive+
	;

directive
    : majorStatement
    | NL
    ;

// Major language statements

majorStatement
    : INCLUDE textLiteral NL                #includePragma
    | INCLUDEOPT textLiteral NL             #includeOptPragma
    | NAME textLiteral NL                   #namePragma
    | VERSION textLiteral NL                #versionPragma
    | DATE textLiteral NL                   #datePragma
    | AUTHOR textLiteral NL                 #authorPragma
    | STYLE number NL                       #stylePragma
    | UTF8 NL                               #utf8Pragma
    | FLAGS flagType NL (flagClause NL)+                        #flagDirective
    | STATE stateClause+                                        #stateDirective
    | TEXT identifier NL textBlock                              #textDirective
    | FRAGMENT identifier NL textBlock                          #fragmentDirective
    | PLACE PLUS? identifier (OPTCOMMA identifier)* NL (textBlock NL)? (textBlock NL)?          #placeDirective
    | OBJECT MINUS? identifier (OPTCOMMA EQUALS? identifier)* NL textBlock NL (textBlock NL)?   #objectDirective
    | NOISE identifier (OPTCOMMA identifier)* NL                #noiseDirective
    | VERB MINUS? identifier (OPTCOMMA identifier)* NL          #verbDirective
    | ACTION arg1=identifier arg2=identifier? NL codeBlock      #actionDirective
    | VARIABLE identifier (OPTCOMMA identifier)* NL             #variableDirective
    | ARRAY identifier number NL                                #arrayDirective
    | PROC name=identifier (OPTCOMMA identifier)* NL codeBlock  #procDirective
    | AT identifier NL codeBlock                                #atDirective
    | INITIAL NL codeBlock                                      #initialDirective
    | REPEAT NL codeBlock                                       #repeatDirective
    ;

flagType
    : VARIABLE
    | OBJECT
    | PLACE
    ;

flagClause : identifier (OPTCOMMA identifier)* ;

stateClause : (expression OPTCOMMA)? identifier NL ;

// Code block

codeBlock : codeStatement ;

codeStatement
    : expression + SEMICOLON
    |
    ;

// Expression

expression
    : atom=unaryExpression                                      #simpleExpr
    | left=expression oper=(PLUS|MINUS) right=unaryExpression   #addSubExpr
    ;

unaryExpression
    : oper=(PLUS|MINUS) expr=unaryExpression    #unaryOperExpr
    | primary                                   #primaryExpr
    ;

primary : identifier | number | character;

// Evaluate lexer elements

textLiteral : TEXT_STRING ;

textBlock : TEXT_LINES ;

codeBlockXXX : TEXT_LINES ;

identifier : IDENTIFIER ;

character : CHAR_LITERAL ;

number : NUM_LITERAL ;
