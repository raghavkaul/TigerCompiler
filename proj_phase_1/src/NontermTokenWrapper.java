/**
 * Wrapper class around non-terminals from the grammar and tokens read from input
 * Used to index into parse table, with each pair returning some nonterminal expansion
 */
public class NontermTokenWrapper {
    private Nonterminal nonterminal;
    private Token token;

    public NontermTokenWrapper(Nonterminal nonterminal, Token token) {
        this.nonterminal = nonterminal;
        this.token = token;
    }

    public Nonterminal getNonterminal() {
        return nonterminal;
    }

    public void setNonterminal(Nonterminal nonterminal) {
        this.nonterminal = nonterminal;
    }

    public Token getToken() {
        return token;
    }

    public void setToken(Token token) {
        this.token = token;
    }

    public boolean equals(Object o) {
        return o instanceof NontermTokenWrapper
                && ((NontermTokenWrapper) o).getNonterminal().equals(this.nonterminal)
                && ((NontermTokenWrapper) o).getToken().getType().equals(this.token.getType());
    }
}
