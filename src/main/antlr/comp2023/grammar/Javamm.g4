grammar Javamm;

@header {
    package pt.up.fe.comp2023;
}

INTEGER : [0-9]+ ;
ID : [a-zA-Z_][a-zA-Z_0-9]* ;

WS : [ \t\n\r\f]+ -> skip ;
COMMENT : '/*' .*? '*/' -> skip ;
LINE_COMMENT : '//' ~[\r\n]* -> skip ;

program
    : (importDeclaration)* classDeclaration EOF
    ;

importDeclaration
    :   WS* 'import' value+=ID ('.' value+=ID)* ('.' '*')? ';'
    ;

classDeclaration
    : 'class' className=ID ( 'extends' parent=ID )? '{' ( varDeclaration )* ( methodDeclaration )* '}'
    ;

varDeclaration
    : type var=ID ';'
    ;

methodDeclaration
    : ('public')? type methodName=ID '(' (param (',' param)* )? ')' '{' varDeclaration* statement* 'return' expression ';' '}' #InstanceMethod
    | ('public')? 'static' 'void' 'main' '(' 'String' '[' ']' paramName=ID ')' '{' varDeclaration* statement* '}'              #MainMethod
    ;

param
    : type name=ID
    ;

type
    : value='int' '[' ']' #Array
    | value='String'      #Single
    | value='boolean'     #Single
    | value='int'         #Single
    | value=ID            #Single
    ;

statement
    : '{' ( statement )* '}'                                #ExprStmt
    | 'if' '(' expression ')' statement 'else' statement    #Conditional
    | 'while' '(' expression ')' statement                  #Loop
    | expression ';'                                        #ExprStmt
    | var=ID '=' expression ';'                             #Assignment
    | var=ID '[' expression ']' '=' expression ';'          #Assignment
    ;

expression
    : op='!' expression                         #UnaryOp
    | '(' expression ')'                        #PrioExpr
    | expression op=('*' | '/') expression      #BinaryOp
    | expression op=('+' | '-') expression      #BinaryOp
    | expression op=('<' | '>' ) expression     #BinaryOp
    | expression op='&&' expression             #BinaryOp
    | expression '[' expression ']'             #ArrayExpr
    | 'new' 'int' '[' expression ']'            #ArrayInit
    | expression '.' 'length'                   #Length
    | expression '.' methodName=ID '(' (expression (',' expression)*)? ')' #MethodCall
    | 'new' className=ID '(' ')'                #Constructor
    | value=INTEGER                             #Integer
    | value='true'                              #BoolExpr
    | value='false'                             #BoolExpr
    | value='this'                              #Reference
    | value=ID                                  #Identifier
    ;
