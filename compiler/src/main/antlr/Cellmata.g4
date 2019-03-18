grammar Cellmata;

start : world_dcl body EOF;

body : (state_decl | neighbourhood_decl | const_decl | func_decl)*;
const_decl : STMT_CONST const_ident ASSIGN expr ;
const_ident : IDENT ;

// World
world_dcl : STMT_WORLD BLOCK_START world_size world_tickrate? world_cellsize? BLOCK_END ;
world_size : WORLD_SIZE ASSIGN world_size_dim (LIST_SEP world_size_dim)?;
world_size_dim : integer_literal SQ_BRACKET_START world_size_dim_finite SQ_BRACKET_END # dimFinite ;
world_size_dim_finite
    : WORLD_WRAP # dimFiniteWrapping
    | WORLD_EDGE ASSIGN IDENT # dimFiniteEdge
    ;
world_tickrate
            : WORLD_TICKRATE ASSIGN world_tickrate_value # tickrate
            ;
world_tickrate_value : integer_literal ;
world_cellsize
            : WORLD_CELLSIZE ASSIGN world_cellsize_value # cellsize
            ;
world_cellsize_value : integer_literal ;

// State
state_decl : STMT_STATE state_ident (SQ_BRACKET_START integer_literal SQ_BRACKET_END)? state_rgb code_block ;
state_ident : IDENT ;
state_rgb : PAREN_START integer_literal LIST_SEP integer_literal LIST_SEP integer_literal PAREN_END ;

// Code
code_block : BLOCK_START stmt* BLOCK_END ;
stmt : (if_stmt | become_stmt | assign_stmt | increment_stmt | decrement_stmt | return_stmt) ;

assign_stmt : STMT_LET? (var_ident | array_lookup) ASSIGN expr END ;
if_stmt : STMT_IF PAREN_START expr PAREN_END code_block (STMT_ELSE_IF PAREN_START expr PAREN_END code_block)* (STMT_ELSE code_block)? ;
become_stmt : STMT_BECOME state_ident END ;
increment_stmt
    : modifiable_ident OP_INCREMENT END # postIncStmt
    | OP_INCREMENT modifiable_ident END # preIncStmt
    ;
decrement_stmt
    : modifiable_ident OP_DECREMENT END # postDecStmt
    | OP_DECREMENT modifiable_ident END # preDecStmt
    ;
return_stmt : STMT_RETURN expr END ;

// Neighbourhood
neighbourhood_decl : STMT_NEIGHBOUR neighbourhood_ident neighbourhood_code ;
neighbourhood_ident : IDENT ;

// Neighbourhood declaration
neighbourhood_code : BLOCK_START coords_decl (LIST_SEP coords_decl)* BLOCK_END ;
coords_decl : PAREN_START integer_literal (LIST_SEP integer_literal)? PAREN_END ;

// Identifiers
modifiable_ident : var_ident | array_lookup ;
var_ident : IDENT ;

// Type declaration
type_ident : IDENT | type_spec ;
type_spec
    : array_decl # typeArray
    | TYPE_BOOLEAN # typeBoolean
    | TYPE_NUMBER # typeNumber
    | STMT_NEIGHBOUR # typeNeighbour
    | STMT_STATE # typeState
    ;

// Array
array_decl : array_prefix type_ident ;
array_value : array_prefix type_ident array_body ;
array_body : BLOCK_START (expr (LIST_SEP expr)*)? BLOCK_END ;
array_prefix : SQ_BRACKET_START integer_literal? SQ_BRACKET_END ;
array_lookup: var_ident SQ_BRACKET_START expr SQ_BRACKET_END ;

// Literals
literal
    : number_literal # numberLiteral
    | bool_literal # boolLiteral
    ;
number_literal
    : integer_literal
    | float_literal
    ;

integer_literal : DIGITS # digitLiteral ;
float_literal : FLOAT # floatLiteral ;

bool_literal
    : LITERAL_TRUE # trueLiteral
    | LITERAL_FALSE # falseLiteral
    ;

// Math
expr : expr OP_OR expr # orExpr
    | expr OP_AND expr # andExpr
    | expr OP_COMPARE_NOT expr # notEqExpr
    | expr OP_COMPARE expr # eqExpr
    | expr OP_MORE expr # moreExpr
    | expr OP_MORE_EQ expr # moreEqExpr
    | expr OP_LESS expr # lessExpr
    | expr OP_LESS_EQ expr # lessEqExpr
    | expr OP_PLUS expr # additionExpr
    | expr OP_MINUS expr # substractionExpr
    | expr OP_MULTIPLY expr # multiplictionExpr
    | expr OP_DIVIDE expr # divisionExpr
    | expr OP_MODULO expr # moduloExpr
    | OP_INCREMENT expr # preIncExpr
    | OP_DECREMENT expr # preDecExpr
    | OP_PLUS expr # positiveExpr
    | OP_MINUS expr # negativeExpr
    | OP_NOT expr # inverseExpr
    | expr OP_INCREMENT # postIncExpr
    | expr OP_DECREMENT # postDecExpr
    | expr SQ_BRACKET_START expr SQ_BRACKET_END # arrayLookupExpr
    | array_value # arrayValueExpr
    | PAREN_START expr PAREN_END # parenExpr
    | literal # literalExpr
    | var_ident # varExpr
    | func # funcExpr
    | '#' # stateIndexExpr
    ;

// Functions
func : func_ident PAREN_START (expr (LIST_SEP expr)* )? PAREN_END ;
func_ident : IDENT ;
func_decl : STMT_FUNC func_ident PAREN_START func_decl_arg (LIST_SEP func_decl_arg)* PAREN_END type_ident code_block ;
func_decl_arg : type_ident IDENT ;

// Tokens
DIGITS : '-'? [1-9][0-9]* | [0] ;
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

OP_COMPARE : '==' ;
OP_COMPARE_NOT : '!=' ;
OP_NOT : '!' ;
OP_INCREMENT : '++' ;
OP_DECREMENT : '--' ;
OP_PLUS : '+' ;
OP_MINUS : '-' ;
OP_MULTIPLY : '*' ;
OP_DIVIDE : '/' ;
OP_MODULO : '%' ;
OP_LESS : '<' ;
OP_LESS_EQ : '<=' ;
OP_MORE : '>' ;
OP_MORE_EQ : '>=' ;
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

TYPE_NUMBER : 'number' ;
TYPE_BOOLEAN : 'boolean' | 'bool' ;

LITERAL_TRUE : 'true' ;
LITERAL_FALSE : 'false' ;

IDENT : [a-zA-Z]([a-zA-Z0-9]+)? ;
