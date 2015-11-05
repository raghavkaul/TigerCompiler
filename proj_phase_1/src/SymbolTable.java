import java.util.HashMap;
import java.util.Map;

public class SymbolTable {

    private Map<String, SymbolRecord> table;

    public SymbolTable() {
        table = new HashMap<>();

        // Predefined types
        TypeRecord intType = new TypeRecord(), floatType = new TypeRecord();
        table.put("int", intType);
        table.put("float", floatType);
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
