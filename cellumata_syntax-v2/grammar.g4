grammar test;

start : board_decl BLANK* body ;

body : ((state_decl | type_decl | const_decl) NEWLINE*)* EOF;
const_decl : 'const' const_ident ASSIGN expr ;
const_ident : IDENT ;

// Board
board_decl : 'board' WHITESPACE* BLOCK_START (WHITESPACE | NEWLINE)* board_world (WHITESPACE | NEWLINE)* board_tickrate (WHITESPACE | NEWLINE)* BLOCK_END ;
board_world : 'world' WHITESPACE* ASSIGN WHITESPACE* board_world_dim (LIST_SEP WHITESPACE* board_world_dim)?;
board_world_dim : DIGITS '[' ('wrap' | 'edge' ASSIGN IDENT) ']' | 'infinite' ;
board_tickrate : 'tickrate' WHITESPACE* ASSIGN WHITESPACE* DIGITS ;

// State
state_decl : 'state' state_ident code_block ;
state_ident : IDENT ;

// Code
code_block : BLOCK_START NEWLINE* (stmt NEWLINE*)* BLOCK_END ;
stmt : (assign_stmt ';' | if_stmt | return_stmt ';') ;

assign_stmt : 'let'? var_ident '=' expr ;
if_stmt : 'if' '(' expr ')' code_block  ('else' code_block)? ;
return_stmt : STMT_RETURN expr;

ident : var_ident ;
var_ident : IDENT ;

// Type declaration
type_decl : 'type' type_ident type_spec;
type_ident : IDENT | 'bool' | 'number' ;
type_spec : array_decl  | TYPE_NUMBER | TYPE_NUMBER ;

// Array
array_decl : array_prefix type_ident ;
array_value : array_prefix type_ident array_body ;
array_body : BLOCK_START (expr (LIST_SEP expr)*)? BLOCK_END ;
array_prefix : '[' DIGITS? ']' ;

// Literals
literal : number_literal | bool_literal ;
number_literal : DIGITS ;
bool_literal : LITERAL_TRUE | LITERAL_FALSE ;

// Functions
func_ident : IDENT ;

// Math
expr : expr_1 ;
expr_1 : expr_1 OP_XOR expr_2 | expr_2 ;
expr_2 : expr_2 OP_OR expr_3 | expr_3 ;
expr_3 : expr_3 OP_AND expr_4 | expr_4 ;
expr_4 : expr_4 OP_COMPARE OP_NOT expr_5 | expr_4 OP_COMPARE expr_5 | expr_5 ;
expr_5 : expr_5 OP_MORE expr_6 | expr_5 OP_MORE_EQ expr_6 | expr_5 OP_LESS expr_6 | expr_5 OP_LESS_EQ expr_6 | expr_6 ;
expr_6 : expr_6 OP_PLUS expr_7 | expr_6 OP_MINUS expr_7 | expr_7;
expr_7 : expr_7 OP_MULTIPLY expr_8 | expr_7 OP_DIVIDE expr_8 | expr_8 ;
expr_8 : OP_DECREMENT expr_9 | OP_DECREMENT expr_9 | OP_PLUS expr_9 | OP_MINUS expr_9 | OP_NOT expr_9 | expr_9 ;
expr_9 : expr_10 OP_INCREMENT | expr_10 OP_DECREMENT | func_ident '(' expr (LIST_SEP expr)*  ')'  | expr_10 '[' DIGITS ']' | array_value | expr_10;
expr_10 : '(' expr ')' | expr_11 ;
expr_11 : literal | var_ident ;

// Tokens
DIGITS : [0-9]+ ;
ASSIGN : '=' ;
LIST_SEP : ',' ;
NEWLINE : ('\r'? '\n' | '\r') -> skip ;
WHITESPACE : ('\t' | ' ') -> skip ;
BLOCK_START : '{' ;
BLOCK_END : '}' ;

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

STMT_RETURN : 'return' ;
STMT_BECOME : 'become' ;

TYPE_NUMBER : 'number' ;
TYPE_BOOLEAN : 'boolean' | 'bool' ;

LITERAL_TRUE : 'true' ;
LITERAL_FALSE : 'false' ;

IDENT : [a-zA-Z] ([a-zA-Z0-9]+)? ;
