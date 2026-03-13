package org.machanism.machai.ai.provider.openai;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.machanism.macha.core.commons.configurator.Configurator;

final class TestConfigurators {

	private TestConfigurators() {
	}

	static Configurator mapBacked() {
		return new MapBackedConfigurator("test");
	}

	static final class MapBackedConfigurator implements Configurator {

		private final String name;
		private final Map<String, String> values = new HashMap<>();

		MapBackedConfigurator(String name) {
			this.name = name;
		}

		@Override
		public String get(String key) {
			String v = values.get(key);
			if (v == null) {
				throw new IllegalArgumentException("Missing config key: " + key);
			}
			return v;
		}

		@Override
		public String get(String key, String defaultValue) {
			return values.getOrDefault(key, defaultValue);
		}

		@Override
		public int getInt(String key) {
			return Integer.parseInt(get(key));
		}

		@Override
		public Integer getInt(String key, Integer defaultValue) {
			String v = values.get(key);
			return v == null ? defaultValue : Integer.valueOf(v);
		}

		@Override
		public boolean getBoolean(String key) {
			return Boolean.parseBoolean(get(key));
		}

		@Override
		public Boolean getBoolean(String key, Boolean defaultValue) {
			String v = values.get(key);
			return v == null ? defaultValue : Boolean.valueOf(v);
		}

		@Override
		public long getLong(String key) {
			return Long.parseLong(get(key));
		}

		@Override
		public Long getLong(String key, Long defaultValue) {
			String v = values.get(key);
			return v == null ? defaultValue : Long.valueOf(v);
		}

		@Override
		public File getFile(String key) {
			return new File(get(key));
		}

		@Override
		public File getFile(String key, File defaultValue) {
			String v = values.get(key);
			return v == null ? defaultValue : new File(v);
		}

		@Override
		public double getDouble(String key) {
			return Double.parseDouble(get(key));
		}

		@Override
		public Double getDouble(String key, Double defaultValue) {
			String v = values.get(key);
			return v == null ? defaultValue : Double.valueOf(v);
		}

		@Override
		public String getName() {
			return name;
		}

		@Override
		public void set(String key, String value) {
			values.put(key, value);
		}
	}
}
