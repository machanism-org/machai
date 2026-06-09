package org.machanism.machai.ai.provider;

import java.io.File;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.ai.tools.FunctionTools;

/**
 * Delegating {@link Genai} implementation.
 *
 * <p>
 * This adapter forwards all {@link Genai} calls to an underlying provider
 * instance configured via {@link #setProvider(Genai)}.
 *
 * <p>
 * Intended use cases include decorating providers (for example, adding
 * cross-cutting concerns like logging, metrics, retries, or request shaping)
 * while preserving the {@link Genai} contract.
 *
 * <p>
 * <strong>Thread-safety:</strong> Instances are not thread-safe unless the
 * delegated provider is thread-safe and access is externally synchronized.
 */
public class GenaiAdapter implements Genai {

	/**
	 * Delegate provider.
	 *
	 * <p>
	 * Subclasses typically set this value during construction or initialization
	 * using {@link #setProvider(Genai)}.
	 */
	protected Genai provider;

	/**
	 * Creates an adapter without a delegate.
	 *
	 * <p>
	 * Call {@link #setProvider(Genai)} before invoking any other methods.
	 */
	public GenaiAdapter() {
		super();
	}

	/**
	 * Sets the delegate provider.
	 *
	 * @param provider the provider to delegate to
	 * @throws IllegalArgumentException if {@code provider} is {@code null}
	 */
	public void setProvider(Genai provider) {
		if (provider == null) {
			throw new IllegalArgumentException("provider must not be null");
		}
		this.provider = provider;
	}

	@Override
	public void init(String model, Configurator conf) {
		provider.init(model, conf);
	}

	@Override
	public void prompt(String text) {
		provider.prompt(text);
	}

	@Override
	public void clear() {
		provider.clear();
	}

	@Override
	public void instructions(String instructions) {
		provider.instructions(instructions);
	}

	@Override
	public String perform() {
		return provider.perform();
	}

	@Override
	public void inputsLog(File bindexTempDir) {
		provider.inputsLog(bindexTempDir);
	}

	@Override
	public void setProjectDir(File projectDir) {
		provider.setProjectDir(projectDir);
	}

	@Override
	public void addTool(FunctionTools tools) {
		provider.addTool(tools);
	}

}
