/**
 * Created by Raghav K on 10/31/15.
 */
public class VarRecord implements SymbolRecord {
    private int numElements;
    private String typeName;

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getNumElements() {
        return numElements;
    }

    public void setNumElements(int numElements) {
        this.numElements = numElements;
    }

    public String toString() {
        return "Size: " + numElements + " :: Type: " + typeName;
    }
}
