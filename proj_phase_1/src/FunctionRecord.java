import java.util.*;

/**
 * Created by Raghav K on 11/5/15.
 */
public class FunctionRecord implements SymbolRecord {
    private List<Map.Entry<String, String>> params; // Map :: parameter name -> parameter type name
    private String returnType;

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public FunctionRecord() {
        params = new LinkedList<>();
    }

    public void addParam(String paramName, String paramType) {
        params.add(new AbstractMap.SimpleEntry<>(paramName, paramType));

    }

    public List<String> getParamNames() {
        List<String> paramNames = new ArrayList<>();
        for (Map.Entry<String, String> me : params) {
            paramNames.add(me.getKey());
        }
        return paramNames;
    }

    public List<Map.Entry<String, String>> getParams() {
        return params;
    }
}
