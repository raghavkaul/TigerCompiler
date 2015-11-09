/**
 * Class representing whitespace-delimited components of some infile
 * program written in the Tiger language.
 */
public class Token {
    private TokenType type;
    private String tokenLiteral;
    public int lineNum, columnNum;

    public Token(TokenType type, String tokenLiteral, int lineNum, int columnNum) {
        this.type = type;
        this.tokenLiteral = tokenLiteral;
        this.lineNum = lineNum;
        this.columnNum = columnNum;
    }

    public Token(TokenType type) {
        this(type, "", 0, 0);
    }

    public String getTokenLiteral() {
        return tokenLiteral;
    }

    public void setToken(String tokenLiteral) {
        this.tokenLiteral = tokenLiteral;
    }

    public void setType(TokenType type) {
        this.type = type;
    }

    public TokenType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        return o != null
                && o instanceof Token
                && ((Token) o).getType().equals(this.type);
    }

    @Override
    public String toString() {
        return type.toString();
    }
}
