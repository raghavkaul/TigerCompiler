public class TypeRecord implements SymbolRecord {
    private String superType;

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }

    private String parentType;
    private int numElements;

    public String getSuperType() {

        return superType;
    }

    public void setSuperType(String superType) {
        this.superType = superType;
    }

    public int getNumElements() {
        return numElements;
    }

    public void setNumElements(int numElements) {
        this.numElements = numElements;
    }

    public String toString() {
        return "Num elements: " + numElements + " :: supertype: " + superType;
    }
}
