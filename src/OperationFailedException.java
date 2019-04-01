/**
 * Thrown to indicate that the attempted operation failed.
 * This exception wraps the exception that were originally thrown.
 */
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
