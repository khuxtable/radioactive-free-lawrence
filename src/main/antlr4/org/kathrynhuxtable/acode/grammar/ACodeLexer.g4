lexer grammar ACodeLexer;

CODE_FRAGMENT : '={' .*? '}=' ;

TEXT_STRING : '"' (.*?) '"' ;

CHAR_LITERAL : '\'' ( ESCAPED_APOSTROPHE | ~('\n'|'\r') )? '\'' ;

NUM_LITERAL : ('0'..'9')+ ;

MINUS : '-' ;
PLUS : '+' ;
TIMES : '*' ;
DIVIDE : '/' ;

EQUAL : '==' ;
LESS_EQUAL : '<=' ;
LESS : '<' ;
GREATER_EQUAL : '>=' ;
GREATER : '>' ;
NOT_EQUAL : '!=' ;
NOT : '!' ;
RANGE : '..' ;

EQUALS : '=' ;

SEMICOLON : ';' ;
OPEN_BRACE : '{' ;
CLOSE_BRACE : '}' ;
OPEN_PAREN : '(' ;
CLOSE_PAREN : ')' ;
OPEN_BRACKET : '[' ;
CLOSE_BRACKET : ']' ;

fragment ESCAPED_APOSTROPHE : '\'' ;
fragment ESCAPED_QUOTE : '"' ;

OPTCOMMA : ','? ;

WS : (' ' | '\t' | '\f')+ -> skip ;

COMMENT : '#' ~[\r\n]* -> skip ;

INCLUDE : 'include' ;
INCLUDEOPT : 'include?' ;
NAME : 'name' ;
VERSION : 'version' ;
DATE : 'date' ;
AUTHOR : 'author' ;
STYLE : 'style' ;
UTF8 : 'utf8' ;
FLAGS : 'flags' ;
STATE : ('state' | 'constant') ;
TEXT : 'text' ;
FRAGMENT : 'fragment' ;
PLACE : 'place' ;
OBJECT : 'object' ;
NOISE : 'noise' ;
VERB : 'verb' ;
ACTION : 'action' ;
VARIABLE : 'variable' ;
ARRAY : 'array' ;
PROC : 'proc' ;
AT : 'at' ;
INITIAL : 'initial' ;
REPEAT : 'repeat' ;

IF : 'if' ;
ELSE : 'else' ;
LOOP : 'loop' ;
IN : 'in';

IDENTIFIER : IDENT_FIRST_CHAR IDENT_REMAINING_CHAR* ;

TEXT_LINES : '{{' (.|'\n')*? '}}' ;

IDENT_FIRST_CHAR : 'a'..'z' | 'A'..'Z' | '.' | '?' ;

IDENT_REMAINING_CHAR : 'a'..'z' | 'A'..'Z' | '0'..'9' | '_' | '.' | '!' | '\'' | '&' | '/' ;

NL : '\r'? '\n' ;
