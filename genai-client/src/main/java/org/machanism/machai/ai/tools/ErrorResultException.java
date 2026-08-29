package org.machanism.machai.ai.tools;

import java.util.Objects;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A runtime exception used to signal a tool or processing error, with a
 * structured error payload that is serialized to JSON and included in the
 * exception message.
 *
 * <p>
 * This exception is typically thrown by function tools (e.g.,
 * {@code @Tool}-annotated methods) to report structured error results, such as
 * error codes, descriptions, or additional context, back to the caller (e.g.,
 * an AI provider or orchestration layer) in a machine-readable format rather
 * than plain text.
 * </p>
 *
 * <p>
 * The provided error payload (via the {@code message} parameter) is serialized
 * to a JSON string using a default {@link ObjectMapper} instance. If
 * serialization fails, the payload is converted to a string using
 * {@link Objects#toString(Object)}.
 * </p>
 * 
 * @since 1.3.0
 */
public class ErrorResultException extends RuntimeException {

	/**
	 * Serialization version for compatibility with previously serialized exception
	 * instances.
	 */
	private static final long serialVersionUID = -70565225512295088L;

	/**
	 * Creates a new exception with a message that is the JSON representation of the
	 * given object.
	 *
	 * <p>
	 * This constructor is used when the error details are provided as an object,
	 * which will be serialized to a JSON string using a default
	 * {@link ObjectMapper}. The resulting JSON string becomes the exception's
	 * message.
	 * </p>
	 *
	 * @param message the object describing the error result; serialized to JSON and
	 *                used as this exception's message
	 */
	public ErrorResultException(Object message) {
		super(message(message, null));
	}

	/**
	 * Creates a new exception using the given string directly as the message,
	 * without JSON serialization.
	 *
	 * <p>
	 * Use this constructor when the error message is already a plain string (e.g.,
	 * already JSON-formatted, or intentionally plain text) and does not need to be
	 * serialized via {@link ObjectMapper}.
	 * </p>
	 *
	 * @param message the exception message, used as-is
	 */
	public ErrorResultException(String message) {
		super(message);
	}

	/**
	 * Creates a new exception with a message that is the JSON representation of the
	 * given object, combined with additional error details from another exception.
	 *
	 * <p>
	 * This constructor is used when an error occurs during processing and you want
	 * to include both the structured error result (serialized to JSON) and the
	 * details of the underlying exception.
	 * </p>
	 *
	 * @param message the object describing the error result; serialized to JSON and
	 *                included in the exception message
	 * @param e       the underlying exception whose details are included in the
	 *                exception message
	 */
	public ErrorResultException(Object message, Exception e) {
		super(message(message, e), e);
	}

	/**
	 * Constructs a detailed error message by combining the provided message object
	 * and exception details.
	 *
	 * <p>
	 * If the {@code message} is a {@link String}, it is used as-is. Otherwise, the
	 * method attempts to serialize the {@code message} object to a JSON string
	 * using a default {@link ObjectMapper}. If serialization fails, the
	 * {@code message} object is converted to a string using
	 * {@link Objects#toString(Object)}.
	 * </p>
	 *
	 * <p>
	 * If an exception is provided, its details are also appended to the resulting
	 * message.
	 * </p>
	 *
	 * @param message the object describing the error result; can be a string or any
	 *                serializable object
	 * @param e       the exception whose details are included in the error message
	 *                (optional)
	 * @return a formatted error message containing the serialized error result and
	 *         exception details
	 */
	private static String message(Object message, Exception e) {
		String msg;
		if (message instanceof String) {
			msg = (String) message;
		} else {
			try {
				msg = new ObjectMapper().writeValueAsString(message);
			} catch (JsonProcessingException e1) {
				msg = Objects.toString(message);
			}
		}
		if (e != null) {
			msg = "Error: " + e + "\n" + "Details: " + msg;
		}
		return msg;
	}
}
