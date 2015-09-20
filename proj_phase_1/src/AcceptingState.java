/**
 * Created by Raghav K on 9/20/15.
 */
public class AcceptingState extends State {
    private TokenType tokenType;

    public AcceptingState(String name) {
        super(name);
    }

    public AcceptingState(String name, boolean isAccepting) {
        super(name, isAccepting);
    }
}
