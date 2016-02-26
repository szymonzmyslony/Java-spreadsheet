package spreadsheet.api.value;

/**
 * A value for spreadsheet cells that are currently invalid.
 * 
 */
public final class InvalidValue implements Value {

    private final String expression;

    public InvalidValue(String expression) {
        this.expression = expression;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        InvalidValue that = (InvalidValue) o;

        return expression != null ? expression.equals(that.expression) : that.expression == null;

    }

    @Override
    public int hashCode() {
        return expression != null ? expression.hashCode() : 0;
    }

    @Override
    public void visit(ValueVisitor visitor) {
        visitor.visitInvalid(expression);
    }

    public String toString() {
        return "{" + expression + "}";
    }

}
