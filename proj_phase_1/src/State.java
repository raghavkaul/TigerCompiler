/**
 * Class representing specific state in DFA
 */
public class State {
    private String name;
    private TokenType tokenType;

    public State(String name, TokenType tokenType) {
        this.name = name;
        this.tokenType = tokenType;
    }

    public TokenType tokenType() {
        return tokenType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o instanceof State) {
            if (((State) o).getName().equals(this.getName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String result = "State name : " + name;
        result += "\tToken type: " + tokenType.toString();

        return result;
    }

    public String getName() {
        return name;
    }
}
