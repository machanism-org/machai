package org.machanism.machai.ai.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ErrorResultException extends SpecialException {

	private static final long serialVersionUID = -70565225512295088L;

	public ErrorResultException(Object message) throws JsonProcessingException {
		super(new ObjectMapper().writeValueAsString(message));
	}

}
