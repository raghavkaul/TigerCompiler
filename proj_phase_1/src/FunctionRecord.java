import java.util.*;

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

    public List<String> getParamTypes() {
        List<String> paramTypes = new ArrayList<>();
        for (Map.Entry<String, String> me : params) {
            paramTypes.add(me.getValue());
        }
        return paramTypes;
    }

    public List<Map.Entry<String, String>> getParams() {
        return params;
    }

    @Override
    public String toString() {
        String result = "(";

        for (Map.Entry param : params) {
            result += param.getKey() + ":" + param.getValue() + ", ";
        }

        return result + ") :: returns " + returnType;
    }
}
