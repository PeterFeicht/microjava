/**
 * Define a grammar for MicroJava
 */
grammar MicroJava;
prog : 'program' Ident (constDecl | varDecl | classDecl)* '{' methodDecl* '}' EOF ;

/*
 * Rules
 */
constDecl  : 'final' type Ident '=' literal ';' ;
varDecl    : type Ident ( ',' Ident )* ';' ;
classDecl  : 'class' Ident '{' varDecl* '}' ;
methodDecl : ( type | 'void' ) Ident '(' formPars? ')' varDecl* block ;
formPars   : type Ident ( ',' type Ident )* ;
type       : Ident ( '[' ']' )? ;

block     : '{' statement* '}' ;
statement : designator ( assignop expr | actPars | '++' | '--' ) ';'
          | 'if' '(' condition ')' statement ( 'else' statement )?
          | 'while' '(' condition ')' statement
          | 'switch' '(' expr ')' '{' ( 'case' expr ':' statement* )* ( 'default' ':' statement* )? '}'
          | 'break' ';'
          | 'return' expr? ';'
          | 'read' '(' designator ')' ';'
          | 'print' '(' expr ( ',' Number )? ')' ';'
          | block
          | ';' ;
assignop  : '=' | '+=' | '-=' | '*=' | '/=' | '%=' ;
actPars   : '(' ( expr ( ',' expr )* )? ')' ;

condition : condTerm ( '||' condTerm )* ;
condTerm  : condFact ( '&&' condFact )* ;
condFact  : expr relop expr ;
relop     : '==' | '!=' | '>' | '>=' | '<' | '<=' ;

expr       : '-'? term ( addop term )* ;
term       : factor ( mulop factor )* ;
factor     : designator actPars?
           | literal
           | 'new' Ident ( '[' expr ']' )?
           | '(' expr ')' ;
designator : Ident ( '.' Ident | '[' expr ']' )* ;
addop      : '+' | '-' ;
mulop      : '*' | '/' | '%' ;
literal    : Number | CharConst ;

/*
 * Keywords
 */
PROGRAM : 'program' ;
CLASS   : 'class' ;
IF      : 'if' ;
ELSE    : 'else' ;
WHILE   : 'while' ;
SWITCH  : 'switch' ;
CASE    : 'case' ;
DEFAULT : 'default' ;
READ    : 'read' ;
PRINT   : 'print' ;
RETURN  : 'return' ;
BREAK   : 'break' ;
VOID    : 'void' ;
FINAL   : 'final' ;
NEW     : 'new' ;
 
 /*
  * Separators
  */
LPAR      : '(' ;
RPAR      : ')' ;
LBRACE    : '{' ;
RBRACE    : '}' ;
LBRACK    : '[' ;
RBRACK    : ']' ;
SEMICOLON : ';' ;
COMMA     : ',' ;
PERIOD    : '.' ;
COLON     : ':' ;

/*
 * Operators
 */
LSS    : '<' ;
LEQ    : '<=' ;
GTR    : '>' ;
GEQ    : '>=' ;
EQL    : '==' ;
NEQ    : '!=' ;
AND    : '&&' ;
OR     : '||' ;
PLUS   : '+' ;
MINUS  : '-' ;
TIMES  : '*' ;
SLASH  : '/' ;
REM    : '%' ;
PPLUS  : '++' ;
MMINUS : '--' ;

ASSIGN  : '=' ;
PLUSAS  : '+=' ;
MINUSAS : '-=' ;
TIMESAS : '*=' ;
SLASHAS : '/=' ;
REMAS   : '%=' ;

/*
 * Tokens
 */
Ident      : IdentStart IdentPart* ;
fragment
IdentStart : [a-zA-Z_] ;
fragment
IdentPart  : IdentStart | Digit ;


Number     : Digit+ ;
fragment
Digit      : [0-9] ;

CharConst  : '\'' ( SingleChar | EscapeSeq ) '\'' ;
fragment
SingleChar : ~['\\] ;
fragment
EscapeSeq  : '\\' [rn'\\] ;

WS         : [ \t\r\n]+ -> channel(HIDDEN) ;
Comment   : '/*' ( Comment | '*' ~[/]? | ~[*] )*? '*/' -> channel(HIDDEN) ;
