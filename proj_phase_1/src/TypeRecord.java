public class TypeRecord implements SymbolRecord {
    private String superType;
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


}
