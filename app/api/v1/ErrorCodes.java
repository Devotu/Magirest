package v1;

public enum ErrorCodes {

    PARAMETER_EMPTY ("Parameter empty. "),
    EXPECTED_STRING ("Expected string. ");

    private final String name;

    private ErrorCodes(String name) {
        this.name = name;
    }

    public boolean equalsName(String compareName) {
        return name.equals(compareName);
    }

    public String code() {
       return this.name;
    }
}