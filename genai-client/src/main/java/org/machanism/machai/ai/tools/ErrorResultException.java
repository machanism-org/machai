package org.machanism.machai.ai.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Runtime exception used to signal a tool or processing error whose payload is
 * serialized to JSON before being carried as the exception message.
 *
 * <p>This is typically thrown by function tools (e.g., {@code @Tool}-annotated
 * methods) to report a structured error result &mdash; such as an error code,
 * description, or additional context &mdash; back to the caller (for example, an
 * AI provider or orchestration layer) in a machine-readable form, rather than as
 * a plain text message.</p>
 *
 * <p>The provided {@code message} object is serialized to a JSON string using a
 * default {@link ObjectMapper} instance, and the resulting JSON string becomes
 * this exception's {@link #getMessage()} value.</p>
 * 
 * @since 1.2.3
 */
public class ErrorResultException extends RuntimeException {

	private static final long serialVersionUID = -70565225512295088L;

	/**
	 * Creates a new exception whose message is the JSON representation of the
	 * given object.
	 *
	 * @param message object describing the error result; serialized to JSON and
	 *                used as this exception's message
	 * @throws JsonProcessingException if {@code message} cannot be serialized to
	 *                                  JSON
	 */
	public ErrorResultException(Object message) throws JsonProcessingException {
		super(new ObjectMapper().writeValueAsString(message));
	}

	/**
	 * Creates a new exception using the given string directly as the message,
	 * without JSON serialization.
	 *
	 * <p>Use this constructor when the error message is already a plain string
	 * (e.g., already JSON-formatted, or intentionally plain text) and does not
	 * need to be serialized via {@link ObjectMapper}.</p>
	 *
	 * @param message the exception message, used as-is
	 * @throws JsonProcessingException never thrown by this constructor; declared
	 *                                  only for signature consistency with
	 *                                  {@link #ErrorResultException(Object)}
	 */
	public ErrorResultException(String message) throws JsonProcessingException {
		super(message);
	}

}