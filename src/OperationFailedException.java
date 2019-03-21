
public class OperationFailedException extends RuntimeException {
    private final Throwable cause;


    public OperationFailedException(Throwable t) {
        super(t.getMessage());
        cause = t;
    }

    public Throwable getTargetException() {
        return cause;
    }
}
