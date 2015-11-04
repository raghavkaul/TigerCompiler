import java.util.Map;

public class SymbolTable {

    private Map<String, SymbolRecord> table;

    public SymbolRecord lookUp(String symbolName) {
        return table.get(symbolName);
    }

    public void insert(String symbolName, SymbolRecord sr) {
        table.put(symbolName, sr);
    }

    public Map<String, SymbolRecord> getTable() {
        return  table;
    }
}
