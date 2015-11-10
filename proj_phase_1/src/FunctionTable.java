import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;

public class FunctionTable {
    private Map<String, FunctionRecord> table, builtins;

    public FunctionTable() {
        table = new HashMap<>();
        builtins = new HashMap<>();

        String[] stdLib = {
                "print",
                "printi",
                "flush",
                "getchar",
                "ord",
                "chr",
                "size",
                "substring",
                "concat",
                "not",
                "exit"
        };

        String[][] paramLists = {
                {"s:string"},
                {"i:int"},
                {},
                {},
                {"s:string"},
                {"i:int"},
                {"s:string"},
                {"s:string", "f:int", "n:int"},
                {"s1:string", "s2:string"},
                {"i:int"},
                {"i:int"}
        };

        String[] returnTypes = {
                null,
                null,
                null,
                "string",
                "int",
                "string",
                "int",
                "string",
                "string",
                "int",
                null
        };

        for (int i = 0; i < stdLib.length; i++) {
            FunctionRecord functionRecord = new FunctionRecord();
            for (String param : paramLists[i]) {
                String[] myParam = param.split(":");
                functionRecord.addParam(myParam[0], myParam[1]);
            }
            functionRecord.setReturnType(
                    returnTypes[i] == null ? "void" : returnTypes[i]);
            builtins.put(stdLib[i], functionRecord);
        }

        table.putAll(builtins);

    }

    public Map<String, FunctionRecord> getBuiltins() {
        return builtins;
    }

    public void setBuiltins(Map<String, FunctionRecord> builtins) {
        this.builtins = builtins;
    }

    public void insert(String name, FunctionRecord functionRecord) {
        table.put(name, functionRecord);
    }


    public Map<String, FunctionRecord> getTable() {
        return table;
    }

    public FunctionRecord lookUp(String symbolName) {
        return table.get(symbolName);
    }

    public boolean contains(String symbol) { return table.containsKey(symbol); }
}
