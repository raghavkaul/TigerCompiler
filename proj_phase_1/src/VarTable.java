import java.util.HashMap;
import java.util.Map;

public class VarTable {

    private Map<String, VarRecord> table;

    public VarTable() {
        table = new HashMap<>();
    }

    public void insert(String name, VarRecord varRecord) {
        table.put(name, varRecord);
    }

    public Map<String, VarRecord> getTable() {
        return  table;
    }

    public VarRecord lookUp(String symbolName) {
        return table.get(symbolName);
    }

    public boolean contains(String symbol) { return table.containsKey(symbol); }
}
