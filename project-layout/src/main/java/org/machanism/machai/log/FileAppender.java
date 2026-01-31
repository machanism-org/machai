package org.machanism.machai.log;

import java.io.File;

import org.apache.commons.lang3.SystemUtils;

import ch.qos.logback.core.rolling.RollingFileAppender;

public class FileAppender<E> extends RollingFileAppender<E> {

	private static File executionDir = SystemUtils.getUserDir();

	@Override
	public void setFile(String file) {
		super.setFile(new File(executionDir, file).getAbsolutePath());
	}

	public static File getExecutionDir() {
		return executionDir;
	}

	public static void setExecutionDir(File executionDir) {
		FileAppender.executionDir = executionDir;
	}
}
