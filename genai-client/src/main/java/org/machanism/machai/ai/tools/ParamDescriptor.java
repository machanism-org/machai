package org.machanism.machai.ai.tools;

public class ParamDescriptor {

	String name;
	String type;
	boolean required;
	String description;

	public ParamDescriptor(String name, String type, boolean required, String description) {
		super();
		this.name = name;
		this.type = type;
		this.required = required;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getDescription() {
		return description;
	}

	public boolean isRequired() {
		return required;
	}

}
