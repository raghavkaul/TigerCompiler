/**
 * Enumerated type for tokens.
 */
public enum TokenType {
    // Punctuation token type
    COMMA, COLON, SEMI, LPAREN, RPAREN, LBRACK, RBRACK, LBRACE, RBRACE, PERIOD,
    PLUS, MINUS, MULT, DIV, EQ, NEQ, LESSER, GREATER, LESSEREQ, GREATEREQ, AND,
    OR, ASSIGN,
    // Keyword token types
    ARRAY, BREAK, DO, ELSE, FOR, FUNC, IF, IN, LET, NIL, OF, THEN, TO, TYPE,
    VAR, WHILE, ENDIF, BEGIN, END, ENDDO,
    // Literal token types
    ID, FLOATLIT, INTLIT;
}
