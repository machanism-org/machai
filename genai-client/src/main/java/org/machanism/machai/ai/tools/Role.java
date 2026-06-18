package org.machanism.machai.ai.tools;

/**
 * Enumeration representing participant roles in an AI interaction context.
 *
 * <p>
 * The {@code Role} enum defines the possible roles for entities involved in a conversation or tool invocation:
 * </p>
 * <ul>
 *   <li>{@link #ASSISTANT}: Represents the AI assistant or provider responding to requests.</li>
 *   <li>{@link #USER}: Represents the end user or client initiating requests or interactions.</li>
 * </ul>
 *
 * <p>
 * This enum is typically used to distinguish between the source and target of messages or actions within the AI framework.
 * </p>
 */
public enum Role {
    /** Represents the AI assistant or provider. */
    ASSISTANT,

    /** Represents the end user or client. */
    USER
}