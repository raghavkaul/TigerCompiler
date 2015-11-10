import java.util.HashMap;
import java.util.Map;

/**
 * Class representing symbol table of types. Populated with default types on initialization
 * This class is queried to check every variable type declaration
 */
public class TypeTable {

    private Map<String, TypeRecord> table;
    private static Map<String, TypeRecord> builtins = null;

    public TypeTable() {
        table = new HashMap<>();

        if (builtins == null) {
            builtins = new HashMap<>();
            TypeRecord intTr = new TypeRecord(),
                    floatTr = new TypeRecord(),
                    _floatArrTr = new TypeRecord(),
                    _intArrTr = new TypeRecord();

            intTr.setSuperType("int");
            intTr.setParentType("int");
            floatTr.setSuperType("float");
            floatTr.setParentType("float");
            _floatArrTr.setSuperType("_array_float");
            _floatArrTr.setParentType("_array_float");
            _intArrTr.setSuperType("_array_int");
            _intArrTr.setParentType("_array_int");

            builtins.put("int", intTr);
            builtins.put("float", floatTr);
            builtins.put("_array_float", _floatArrTr);
            builtins.put("_array_int", _intArrTr);
        }


        table.putAll(builtins);
    }

    public void insert(String name, TypeRecord typeRecord) {
        table.put(name, typeRecord);
    }

    public Map<String, TypeRecord> getTable() {
        return  table;
    }

    public TypeRecord lookUp(String symbolName) {
        return table.get(symbolName);
    }

    public boolean contains(String symbolName) { return table.containsKey(symbolName); }
}
