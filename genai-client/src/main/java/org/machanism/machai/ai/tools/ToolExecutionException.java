package org.machanism.machai.ai.tools;

/**
 * Indicates that a tool callback could not complete its requested operation.
 *
 * @author Viktor Tovstyi
 */
public class ToolExecutionException extends Exception {

    /**
     * Serialization version for compatibility with previously serialized exception
     * instances.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Creates an exception with a descriptive message.
     *
     * @param message failure description
     */
    public ToolExecutionException(String message) {
        super(message);
    }

    /**
     * Creates an exception that preserves the underlying failure.
     *
     * @param cause underlying failure
     */
    public ToolExecutionException(Throwable cause) {
        super(cause);
    }
}
