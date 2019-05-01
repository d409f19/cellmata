grammar Cellmata;

start : world_dcl body EOF;

body : (state_decl | neighbourhood_decl | const_decl | func_decl)*;
const_decl : STMT_CONST const_ident ASSIGN expr END ;
const_ident : IDENT ;

// World
world_dcl : STMT_WORLD BLOCK_START size=world_size world_options BLOCK_END ;
world_size : WORLD_SIZE ASSIGN width=world_size_dim (LIST_SEP height=world_size_dim)?;
world_size_dim : size=integer_literal SQ_BRACKET_START type=world_size_dim_finite SQ_BRACKET_END ;
world_size_dim_finite
    : WORLD_WRAP # dimFiniteWrapping
    | WORLD_EDGE ASSIGN state=IDENT # dimFiniteEdge
    ;
world_options
    : tickrate=world_tickrate? cellsize=world_cellsize?
    | cellsize=world_cellsize? tickrate=world_tickrate?
    ;
world_tickrate : WORLD_TICKRATE ASSIGN value=integer_literal ;
world_cellsize : WORLD_CELLSIZE ASSIGN value=integer_literal ;

// State
state_decl : STMT_STATE state_ident (SQ_BRACKET_START integer_literal SQ_BRACKET_END)? state_rgb code_block ;
state_ident : IDENT ;
state_rgb : PAREN_START red=integer_literal LIST_SEP green=integer_literal LIST_SEP blue=integer_literal PAREN_END ;

// Code
code_block : BLOCK_START stmt* BLOCK_END ;
stmt : (if_stmt | become_stmt | assign_stmt | return_stmt | for_stmt | break_stmt | continue_stmt) ;

assign_stmt : assignment END ;
assignment : STMT_LET? (var_ident | array_lookup) ASSIGN expr ;
if_stmt : if_stmt_if if_stmt_elif* if_stmt_else? ;
if_stmt_condition: expr ;
if_stmt_elif: STMT_ELSE_IF if_stmt_block ;
if_stmt_if : STMT_IF if_stmt_block ;
if_stmt_block : PAREN_START if_stmt_condition PAREN_END code_block ;
if_stmt_else : STMT_ELSE code_block ;
become_stmt : STMT_BECOME state=expr END ;
return_stmt : STMT_RETURN expr END ;

//For-loop
for_stmt : STMT_FOR PAREN_START for_init? END for_condition END for_post_iteration? PAREN_END code_block ;
for_init : assignment ;
for_condition : expr ;
for_post_iteration : assignment ;
continue_stmt : STMT_CONTINUE END ;
break_stmt : STMT_BREAK END ;

// Neighbourhood
neighbourhood_decl : STMT_NEIGHBOUR neighbourhood_ident neighbourhood_code ;
neighbourhood_ident : IDENT ;

// Neighbourhood declaration
neighbourhood_code : BLOCK_START coords_decl (LIST_SEP coords_decl)* BLOCK_END ;
coords_decl : PAREN_START integer_literal (LIST_SEP integer_literal)? PAREN_END ;

// Identifiers
var_ident : IDENT ;

// Type declaration
type_ident
    : array_decl # typeArray
    | TYPE_BOOLEAN # typeBoolean
    | TYPE_INTEGER # typeInteger
    | TYPE_FLOAT # typeFloat
    | STMT_NEIGHBOUR # typeNeighbour
    | STMT_STATE # typeState
    ;

// Array
array_decl : array_prefix type_ident ;
array_value_sized : array_decl array_value_literal? ;
array_value_literal : BLOCK_START (expr (LIST_SEP expr)*)? BLOCK_END ;
array_prefix : SQ_BRACKET_START index=integer_literal? SQ_BRACKET_END ;
array_lookup: array=expr SQ_BRACKET_START index=expr SQ_BRACKET_END ;

// Literals
literal
    : value=bool_literal # boolLiteral
    | value=integer_literal # integerLiteral
    | value=float_literal # floatLiteral
    ;

integer_literal : '-'? value=DIGITS ;
float_literal : '-'? value=FLOAT ;

bool_literal
    : LITERAL_TRUE # trueLiteral
    | LITERAL_FALSE # falseLiteral
    ;

// Math
expr : '#' # stateIndexExpr
    | value=func # funcExpr
    | ident=var_ident # varExpr
    | value=literal # literalExpr
    | PAREN_START value=expr PAREN_END # parenExpr
    | value=array_value_sized # arraySizedValueExpr
    | value=array_value_literal # arrayLiteralExpr
    | value=expr SQ_BRACKET_START index=expr SQ_BRACKET_END # arrayLookupExpr
    | OP_NOT value=expr # notExpr
    | OP_MINUS value=expr # negationExpr
    | left=expr OP_MODULO right=expr # moduloExpr
    | left=expr OP_DIVIDE right=expr # divisionExpr
    | left=expr OP_MULTIPLY right=expr # multiplictionExpr
    | left=expr OP_MINUS right=expr # substractionExpr
    | left=expr OP_PLUS right=expr # additionExpr
    | left=expr OP_LESS_OR_EQ right=expr # lessOrEqExpr
    | left=expr OP_LESS_THAN right=expr # lessThanExpr
    | left=expr OP_GREATER_OR_EQ right=expr # greaterOrEqExpr
    | left=expr OP_GREATER_THAN right=expr # greaterThanExpr
    | left=expr OP_EQUALITY right=expr # equalityExpr
    | left=expr OP_INEQUALITY right=expr # inequalityExpr
    | left=expr OP_AND right=expr # andExpr
    | left=expr OP_OR right=expr # orExpr
    ;

// Functions
func : ident=func_ident PAREN_START (expr (LIST_SEP expr)* )? PAREN_END ;
func_ident : IDENT ;
func_decl : STMT_FUNC func_ident PAREN_START func_decl_arg (LIST_SEP func_decl_arg)* PAREN_END type_ident code_block ;
func_decl_arg : type_ident IDENT ;

// Tokens
DIGITS : [1-9][0-9]* | [0] ;
FLOAT : DIGITS '.' [0-9]* ;
ASSIGN : '=' ;
LIST_SEP : ',' ;
NEWLINE : ('\r'? '\n' | '\r') -> skip ;
WHITESPACE : ('\t' | ' ') -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;
COMMENT : '/*' .*? '*/' -> skip ;
BLOCK_START : '{' ;
BLOCK_END : '}' ;
SQ_BRACKET_START : '[' ;
SQ_BRACKET_END : ']' ;
PAREN_START : '(' ;
PAREN_END : ')' ;
END : ';' ;

OP_EQUALITY : '==' ;
OP_INEQUALITY : '!=' ;
OP_NOT : '!' ;
OP_PLUS : '+' ;
OP_MINUS : '-' ;
OP_MULTIPLY : '*' ;
OP_DIVIDE : '/' ;
OP_MODULO : '%' ;
OP_LESS_THAN : '<' ;
OP_LESS_OR_EQ : '<=' ;
OP_GREATER_THAN : '>' ;
OP_GREATER_OR_EQ : '>=' ;
OP_AND : '&&' ;
OP_OR : '||' ;

WORLD_SIZE : 'size' ;
WORLD_WRAP : 'wrap' ;
WORLD_EDGE : 'edge' ;
WORLD_TICKRATE : 'tickrate' ;
WORLD_CELLSIZE : 'cellsize' ;

STMT_CONST : 'const' ;
STMT_LET : 'let' ;
STMT_STATE : 'state' ;
STMT_NEIGHBOUR : 'neighbourhood' ;
STMT_WORLD : 'world' ;
STMT_BECOME : 'become' ;
STMT_IF : 'if' ;
STMT_ELSE_IF : 'elif' ;
STMT_ELSE : 'else' ;
STMT_FUNC : 'function' ;
STMT_RETURN : 'return' ;
STMT_FOR : 'for' ;
STMT_BREAK : 'break' ;
STMT_CONTINUE : 'continue' ;

TYPE_INTEGER : 'int' ;
TYPE_FLOAT : 'float' ;
TYPE_BOOLEAN : 'bool' ;

LITERAL_TRUE : 'true' ;
LITERAL_FALSE : 'false' ;

IDENT : [a-zA-Z]([a-zA-Z0-9]+)? ;
