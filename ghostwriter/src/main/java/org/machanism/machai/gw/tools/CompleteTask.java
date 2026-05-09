package org.machanism.machai.gw.tools;

public class CompleteTask extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public CompleteTask(String message) {
		super(message);
	}
}