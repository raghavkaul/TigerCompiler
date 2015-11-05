import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private Map<String, SymbolRecord> table, builtins;

    public SymbolTable() {
        table = new HashMap<>();

        // Predefined types
        TypeRecord intType = new TypeRecord(),
                floatType = new TypeRecord(),
                arrayType = new TypeRecord();

        table.put("array", arrayType);
        table.put("int", intType);
        table.put("float", floatType);
        builtins = table;
    }

    public Map<String, SymbolRecord> getBuiltins() {
        return builtins;
    }

    public void setBuiltins(Map<String, SymbolRecord> builtins) {
        this.builtins = builtins;
    }

    public SymbolRecord lookUp(String symbolName) {
        return table.get(symbolName);
    }

    public void insert(String symbolName, SymbolRecord sr) {
        table.put(symbolName, sr);
    }

    public boolean contains(String symbol) { return table.containsKey(symbol); }

    public Map<String, SymbolRecord> getTable() {
        return  table;
    }
}
