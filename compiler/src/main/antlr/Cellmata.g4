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
expr : expr_2 ;
expr_2
    : expr_2 OP_OR expr_3 # orExpr
    | expr_3 #expr3Cont
    ;
expr_3
    : expr_3 OP_AND expr_4 # andExpr
    | expr_4 # expr4Cont
    ;
expr_4
    : expr_4 OP_COMPARE_NOT expr_5 # notEqExpr
    | expr_4 OP_COMPARE expr_5 # eqExpr
    | expr_5 # expr5Cont
    ;
expr_5
    : expr_5 OP_MORE expr_6 # moreExpr
    | expr_5 OP_MORE_EQ expr_6 # moreEqExpr
    | expr_5 OP_LESS expr_6 # lessExpr
    | expr_5 OP_LESS_EQ expr_6 # lessEqExpr
    | expr_6 # expr6Cont
    ;
expr_6
    : expr_6 OP_PLUS expr_7 # additionExpr
    | expr_6 OP_MINUS expr_7 # substractionExpr
    | expr_7 #expr7Cont
    ;
expr_7
    : expr_7 OP_MULTIPLY expr_8 # multiplictionExpr
    | expr_7 OP_DIVIDE expr_8 # divisionExpr
    | expr_7 OP_MODULO expr_8 # moduloExpr
    | expr_8 #expr8Cont
    ;
expr_8
    : OP_INCREMENT expr_9 # preIncExpr
    | OP_DECREMENT expr_9 # preDecExpr
    | OP_PLUS expr_9 # positiveExpr
    | OP_MINUS expr_9 # negativeExpr
    | OP_NOT expr_9 # inverseExpr
    | expr_9 # expr9Cont
    ;
expr_9
    : expr_10 OP_INCREMENT # postIncExpr
    | expr_10 OP_DECREMENT # postDecExpr
    | expr_10 SQ_BRACKET_START expr SQ_BRACKET_END # arrayLookupExpr
    | array_value # arrayValueExpr
    | expr_10 # expr10Cont
    ;
expr_10
    : PAREN_START expr PAREN_END # parenExpr
    | expr_11 # expr11Cont
    ;
expr_11
    : literal # literalExpr
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
