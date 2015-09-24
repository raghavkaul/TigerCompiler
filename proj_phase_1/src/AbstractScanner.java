/**
 * Created by Raghav K on 9/23/15.
 */
public interface AbstractScanner {
    /**
     * Reads current token without advancing scanner to next token
     * @return current token
     */
    Token peekToken();

    /**
     * Reads current token and advances scanner to next token
     * @return current token
     */
    Token nextToken();


}
