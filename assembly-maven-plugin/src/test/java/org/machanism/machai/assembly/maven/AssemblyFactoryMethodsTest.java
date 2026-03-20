package org.machanism.machai.assembly.maven;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;

/**
 * Tests for factory methods on {@link Assembly}.
 *
 * <p>
 * These methods return concrete instances and are otherwise hard to cover via the execute flow
 * because their constructors perform heavy provider initialization.
 * </p>
 */
class AssemblyFactoryMethodsTest {

	@Test
	void createPicker_createsInstance() {
		// Arrange
		Assembly mojo = new Assembly();
		mojo.pickModel = "OpenAI:gpt-5-mini";
		// Avoid provider-side parsing failures: supply a syntactically valid mongodb URI.
		mojo.registerUrl = "mongodb://localhost:27017";

		// Act
		Object picker = mojo.createPicker(dummyConfig());

		// Assert
		assertNotNull(picker);
	}

	@Test
	void createAssembly_createsInstance() {
		// Arrange
		Assembly mojo = new Assembly();
		mojo.assemblyModel = "OpenAI:gpt-5";
		mojo.basedir = new File(".");

		// Act
		Object assembly = mojo.createAssembly(dummyConfig());

		// Assert
		assertNotNull(assembly);
	}

	private static Configurator dummyConfig() {
		return new Configurator() {
			@Override
			public String get(String key) {
				return null;
			}

			@Override
			public String get(String key, String defaultValue) {
				return defaultValue;
			}

			@Override
			public int getInt(String key) {
				return 0;
			}

			@Override
			public Integer getInt(String key, Integer defaultValue) {
				return defaultValue;
			}

			@Override
			public boolean getBoolean(String key) {
				return false;
			}

			@Override
			public Boolean getBoolean(String key, Boolean defaultValue) {
				return defaultValue;
			}

			@Override
			public long getLong(String key) {
				return 0L;
			}

			@Override
			public Long getLong(String key, Long defaultValue) {
				return defaultValue;
			}

			@Override
			public File getFile(String key) {
				return null;
			}

			@Override
			public File getFile(String key, File defaultValue) {
				return defaultValue;
			}

			@Override
			public double getDouble(String key) {
				return 0.0;
			}

			@Override
			public Double getDouble(String key, Double defaultValue) {
				return defaultValue;
			}

			@Override
			public String getName() {
				return "dummy";
			}

			@Override
			public void set(String key, String value) {
				// no-op
			}
		};
	}
}
