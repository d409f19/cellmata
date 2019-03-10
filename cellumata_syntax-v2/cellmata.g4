grammar cellmata;

start : world_dcl body EOF;

body : (state_decl | neighbourhood_decl | const_decl)*;
const_decl : 'const' const_ident ASSIGN expr ;
const_ident : IDENT ;

// World
world_dcl : STMT_WORLD BLOCK_START world_size world_tickrate? world_cellsize? BLOCK_END ;
world_size : 'size' ASSIGN world_size_dim (LIST_SEP world_size_dim)?;
world_size_dim : DIGITS SQ_BRACKET_START ('wrap' | 'edge' ASSIGN IDENT) SQ_BRACKET_END ;
world_tickrate : 'tickrate' ASSIGN DIGITS ;
world_cellsize : 'cellsize' ASSIGN DIGITS ;

// State
state_decl : STMT_STATE state_ident state_rgb code_block ;
state_ident : IDENT ;
state_rgb : PAREN_START (DIGITS LIST_SEP DIGITS LIST_SEP DIGITS) PAREN_END ;

// Code
code_block : BLOCK_START stmt* BLOCK_END ;
stmt : (if_stmt | become_stmt | assign_stmt | increment_stmt | decrement_stmt) ;

assign_stmt : 'let'? (var_ident | array_lookup) ASSIGN expr END ;
if_stmt : STMT_IF PAREN_START expr PAREN_END code_block (STMT_ELSE STMT_IF PAREN_START expr PAREN_END code_block)* (STMT_ELSE code_block)? ;
become_stmt : STMT_BECOME state_ident END ;
increment_stmt : modifiable_ident OP_INCREMENT ';' | OP_INCREMENT modifiable_ident ';';
decrement_stmt : modifiable_ident OP_DECREMENT ';' | OP_DECREMENT modifiable_ident ';';

// Neighbourhood
neighbourhood_decl : STMT_NEIGHBOUR neighbourhood_ident neighbourhood_code ;
neighbourhood_ident : IDENT ;

// Neighbourhood declaration
neighbourhood_code : BLOCK_START coords_decl (LIST_SEP coords_decl)* BLOCK_END ;
coords_decl : PAREN_START DIGITS (LIST_SEP DIGITS)? PAREN_END ;

// Identifiers
modifiable_ident : var_ident | array_lookup ;
var_ident : IDENT ;

// Type declaration
type_ident : IDENT | type_spec ;
type_spec : array_decl  | TYPE_BOOLEAN | TYPE_NUMBER ;

// Array
array_decl : array_prefix type_ident ;
array_value : array_prefix type_ident array_body ;
array_body : BLOCK_START (expr (LIST_SEP expr)*)? BLOCK_END ;
array_prefix : SQ_BRACKET_START DIGITS? SQ_BRACKET_END ;
array_lookup: var_ident SQ_BRACKET_START DIGITS SQ_BRACKET_END ;

// Literals
literal : number_literal | bool_literal ;
number_literal : DIGITS ;
bool_literal : LITERAL_TRUE | LITERAL_FALSE ;

// Math
expr : expr_1 ;
expr_1 : expr_1 OP_XOR expr_2 | expr_2 ;
expr_2 : expr_2 OP_OR expr_3 | expr_3 ;
expr_3 : expr_3 OP_AND expr_4 | expr_4 ;
expr_4 : expr_4 OP_COMPARE OP_NOT expr_5 | expr_4 OP_COMPARE expr_5 | expr_5 ;
expr_5 : expr_5 OP_MORE expr_6 | expr_5 OP_MORE_EQ expr_6 | expr_5 OP_LESS expr_6 | expr_5 OP_LESS_EQ expr_6 | expr_6 ;
expr_6 : expr_6 OP_PLUS expr_7 | expr_6 OP_MINUS expr_7 | expr_7;
expr_7 : expr_7 OP_MULTIPLY expr_8 | expr_7 OP_DIVIDE expr_8 | expr_8 ;
expr_8 : OP_INCREMENT expr_9 | OP_DECREMENT expr_9 | OP_PLUS expr_9 | OP_MINUS expr_9 | OP_NOT expr_9 | expr_9 ;
expr_9 : expr_10 OP_INCREMENT | expr_10 OP_DECREMENT | expr_10 SQ_BRACKET_START DIGITS SQ_BRACKET_END | array_value | expr_10;
expr_10 : PAREN_START expr PAREN_END | expr_11 ;
expr_11 : literal | var_ident | func ;

// Built-in funcitons
func : (func_count | func_rand | func_abs) ;
func_count : FUNC_COUNT PAREN_START neighbourhood_ident LIST_SEP state_ident PAREN_END ;
func_rand : FUNC_RAND PAREN_START DIGITS PAREN_END ;
func_abs : FUNC_ABS PAREN_START expr PAREN_END ;

// Tokens
DIGITS : '-'? [1-9][0-9]* | [0] ;
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

OP_COMPARE : 'is' ;
OP_NOT : 'not' ;
OP_INCREMENT : '++' ;
OP_DECREMENT : '--' ;
OP_PLUS : '+' ;
OP_MINUS : '-' ;
OP_MULTIPLY : '*' ;
OP_DIVIDE : '/' ;
OP_MODULUS : '%' ; // PRAISE!
OP_LESS : '<' ;
OP_LESS_EQ : '<=' ;
OP_MORE : '>' ;
OP_MORE_EQ : '>=' ;
OP_AND : 'and' ;
OP_OR : 'or' ;
OP_XOR : 'xor' ;

STMT_STATE : 'state' ;
STMT_NEIGHBOUR : 'neighbourhood' ;
STMT_WORLD : 'world' ;
STMT_BECOME : 'become' ;
STMT_IF : 'if' ;
STMT_ELSE : 'else' ;

FUNC_COUNT : 'count' ;
FUNC_RAND : 'rand' ;
FUNC_ABS : 'abs' ;

TYPE_NUMBER : 'number' ;
TYPE_BOOLEAN : 'boolean' | 'bool' ;

LITERAL_TRUE : 'true' ;
LITERAL_FALSE : 'false' ;

IDENT : [a-zA-Z]([a-zA-Z0-9]+)? ;
