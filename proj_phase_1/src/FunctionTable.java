import java.util.HashMap;
import java.util.Map;

public class FunctionTable {
    private Map<String, FunctionRecord> table, builtins;

    public FunctionTable() {
        table = new HashMap<>();
        builtins = new HashMap<>();

        String[] stdLib = {"print", "printi", "flush", "getchar",
                "ord", "chr", "size", "substring", "concat", "not", "exit"};

        for (String funcName : stdLib) {
            FunctionRecord fr = new FunctionRecord();
            builtins.put(funcName, fr);
        }

        table.putAll(builtins);

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
