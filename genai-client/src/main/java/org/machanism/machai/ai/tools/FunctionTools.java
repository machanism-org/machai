package org.machanism.machai.ai.tools;

import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Strings;
import org.apache.commons.text.StringSubstitutor;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.manager.Genai;

/**
 * Service-provider interface (SPI) for installing host-provided function tools into a {@link Genai}.
 *
 * <p>
 * Implementations contribute one or more named tools via
 * {@link Genai#addTool(String, String, org.machanism.machai.ai.manager.Genai.ToolFunction, String...)}. Tools are
 * typically discovered via {@link java.util.ServiceLoader} and applied at runtime by {@link FunctionToolsLoader}.
 * </p>
 *
 * <p>
 * Implementations may optionally accept a {@link Configurator} via {@link #setConfigurator(Configurator)} to
 * resolve runtime configuration (for example, API tokens for web calls).
 * </p>
 */
public interface FunctionTools {

	/**
	 * Registers this tool set with the given provider.
	 *
	 * @param provider provider to register tools with
	 */
	void applyTools(Genai provider);

	/**
	 * Provides a configurator instance to the tool set.
	 *
	 * <p>
	 * The default implementation does nothing.
	 * </p>
	 *
	 * @param configurator configurator to use for runtime value resolution
	 */
	default void setConfigurator(Configurator configurator) {
		// no-op
	}

	/**
	 * Resolves ${...} placeholders using the provided configurator.
	 *
	 * <p>
	 * Unresolvable placeholders are left as-is.
	 * </p>
	 *
	 * @param value raw value that may contain placeholders
	 * @param conf  configurator used for lookup; if {@code null}, the value is returned unchanged
	 * @return resolved value
	 */
	default String replace(String value, Configurator conf) {
		if (value == null || conf == null) {
			return value;
		}

		String current = value;
		for (int i = 0; i < 10; i++) {
			Properties properties = new Properties();

			Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
			Matcher matcher = pattern.matcher(current);
			while (matcher.find()) {
				String propName = matcher.group(1);
				String propValue = conf.get(propName);
				if (propValue != null) {
					properties.put(propName, propValue);
				}
			}

			String replaced = StringSubstitutor.replace(current, properties);
			if (replaced.equals(current) || !Strings.CS.contains(replaced, "${")) {
				return replaced;
			}
			current = replaced;
		}

		return current;
	}
}
