grammar test;

start : board_decl (WHITESPACE | NEWLINE)* body EOF;

body : ((state_decl | const_decl | func_decl) NEWLINE*)*;
const_decl : 'const' const_ident ASSIGN expr ;
const_ident : IDENT ;

// Board
board_decl : STMT_BOARD WHITESPACE* BLOCK_START (WHITESPACE | NEWLINE)* board_world (WHITESPACE | NEWLINE)* board_tickrate (WHITESPACE | NEWLINE)* BLOCK_END ;
board_world : 'world' WHITESPACE* ASSIGN WHITESPACE* board_world_dim (LIST_SEP WHITESPACE* board_world_dim)?;
board_world_dim : DIGITS '[' ('wrap' | 'edge' ASSIGN IDENT) SQ_BRACKET_END | 'infinite' ;
board_tickrate : 'tickrate' WHITESPACE* ASSIGN WHITESPACE* DIGITS ;

// State
state_decl : STMT_STATE state_ident code_block ;
state_ident : IDENT ;

// Code
code_block : BLOCK_START NEWLINE* (stmt NEWLINE*)* BLOCK_END ;
stmt : (if_stmt | return_stmt | become_stmt | switch_stmt | assign_stmt) ;

assign_stmt : 'let'? (var_ident | array_lookup) ASSIGN expr END;
if_stmt : STMT_IF PARAN_START expr PARAN_END code_block (STMT_ELSE STMT_IF PARAN_START expr PARAN_END code_block)* (STMT_ELSE code_block)? ;
return_stmt : STMT_RETURN expr (LIST_SEP expr)*? END;
become_stmt : STMT_BECOME state_ident END ;

ident : var_ident ;
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

// Functions
func_ident : IDENT ;
func_decl : STMT_FUNC func_ident func_args_decl func_return_decl func_body ;
func_args_decl : PARAN_START (type_ident (LIST_SEP type_ident)?)? PARAN_END ;
func_return_decl : (PARAN_START (type_ident (LIST_SEP type_ident)?)? PARAN_END)? ;
func_body: BLOCK_START (stmt)*? BLOCK_END ;

// Switch
switch_stmt : STMT_SWITCH PARAN_START expr PARAM_END BLOCK_START switch_case* BLOCK_END;
switch_case : (STMT_CASE expr | STMT_DEFAULT) ':' (stmt | fallthrough_stmt)* ;
fallthrough_stmt : STMT_FALLTHROUGH END ;

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
expr_9 : expr_10 OP_INCREMENT | expr_10 OP_DECREMENT | func_ident PARAN_START (expr (LIST_SEP expr)*)?  PARAM_END  | expr_10 SQ_BRACKET_START DIGITS SQ_BRACKET_END | array_value | expr_10;
expr_10 : PARAN_START expr PARAM_END | expr_11 ;
expr_11 : literal | var_ident ;

// Tokens
DIGITS : [1-9][0-9]* | [0] ;
ASSIGN : '=' ;
LIST_SEP : ',' ;
NEWLINE : ('\r'? '\n' | '\r') -> skip ;
WHITESPACE : ('\t' | ' ') -> skip ;
BLOCK_START : '{' ;
BLOCK_END : '}' ;
SQ_BRACKET_START : '[' ;
SQ_BRACKET_END : ']' ;
PARAN_START : '(' ;
PARAN_END : ')' ;
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

STMT_FALLTHROUGH : 'fallthrough' ;
STMT_SWITCH : 'switch' ;
STMT_CASE : 'case' ;
STMT_DEFAULT : 'default' ;
STMT_FUNC : 'func' ;
STMT_STATE : 'state' ;
STMT_BOARD : 'board' ;
STMT_RETURN : 'return' ;
STMT_BECOME : 'become' ;
STMT_IF : 'if' ;
STMT_ELSE : 'else' ;

TYPE_NUMBER : 'number' ;
TYPE_BOOLEAN : 'boolean' | 'bool' ;

LITERAL_TRUE : 'true' ;
LITERAL_FALSE : 'false' ;

IDENT : [a-zA-Z] ([a-zA-Z0-9]+)? ;
