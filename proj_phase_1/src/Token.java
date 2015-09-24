/**
 * Created by Raghav K on 9/17/15.
 */
public class Token {
    private TokenType type;
    private String tokenLiteral;
    private int lineNum, columnNum;

    public Token(TokenType type, String tokenLiteral, int lineNum, int columnNum) {
        this.type = type;
        this.tokenLiteral = tokenLiteral;
        this.lineNum = lineNum;
        this.columnNum = columnNum;
    }

    public int getLineNum() {
        return lineNum;
    }

    public void setLineNum(int lineNum) {
        this.lineNum = lineNum;
    }

    public int getColumnNum() {
        return columnNum;
    }

    public void setColumnNum(int columnNum) {
        this.columnNum = columnNum;
    }

    public String getToken() {
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
}
