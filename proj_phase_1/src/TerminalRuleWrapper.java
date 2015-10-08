import java.util.Objects;

/**
 * My name is Inseok and I am a cool guy
 */
public class TerminalRuleWrapper {
    protected Terminal t;
    protected Rule r;

    public TerminalRuleWrapper(Terminal t, Rule r) {
        this.t = t;
        this.r = r;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof TerminalRuleWrapper
                && ((TerminalRuleWrapper) o).t.equals(this.t);
//                && ((TerminalRuleWrapper) o).r.equals(this.r);
    }

    @Override
    public int hashCode() {
        return t.hashCode();
    }

    @Override
    public String toString() {
        return t.toString() + r.getRuleNo();
    }
}