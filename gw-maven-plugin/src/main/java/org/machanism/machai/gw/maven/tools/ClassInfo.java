package org.machanism.machai.gw.maven.tools;

public class ClassInfo {

	private String path;
	private String packageName;
	private String className;
	private String scope;

	public ClassInfo(String packageName, String className, String scope, String path) {
		super();
		this.packageName = packageName;
		this.className = className;
		this.scope = scope;
		this.path = path;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

}
