package org.machanism.machai.ai.manager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;

import org.machanism.macha.core.commons.configurator.Configurator;

/**
 * Delegating {@link Genai} implementation.
 *
 * <p>This adapter forwards all {@link Genai} calls to an underlying provider instance configured via
 * {@link #setProvider(Genai)}.
 *
 * <p>Intended use cases include decorating providers (for example, adding cross-cutting concerns like logging,
 * metrics, retries, or request shaping) while preserving the {@link Genai} contract.
 *
 * <p><strong>Thread-safety:</strong> Instances are not thread-safe unless the delegated provider is thread-safe and
 * access is externally synchronized.
 */
public class GenaiAdapter implements Genai {

	/**
	 * Delegate provider.
	 *
	 * <p>Subclasses typically set this value during construction or initialization using
	 * {@link #setProvider(Genai)}.
	 */
	protected Genai provider;

	/**
	 * Creates an adapter without a delegate.
	 *
	 * <p>Call {@link #setProvider(Genai)} before invoking any other methods.
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
	protected void setProvider(Genai provider) {
		if (provider == null) {
			throw new IllegalArgumentException("provider must not be null");
		}
		this.provider = provider;
	}

	@Override
	public void init(Configurator conf) {
		provider.init(conf);
	}

	@Override
	public void prompt(String text) {
		provider.prompt(text);
	}

	@Override
	public void addFile(File file) throws IOException {
		provider.addFile(file);
	}

	@Override
	public void addFile(URL fileUrl) throws IOException {
		provider.addFile(fileUrl);
	}

	@Override
	public List<Double> embedding(String text, long dimensions) {
		return provider.embedding(text, dimensions);
	}

	@Override
	public void clear() {
		provider.clear();
	}

	@Override
	public void addTool(String name, String description, ToolFunction function, String... paramsDesc) {
		provider.addTool(name, description, function, paramsDesc);
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
	public void setWorkingDir(File workingDir) {
		provider.setWorkingDir(workingDir);
	}

	@Override
	public Usage usage() {
		return provider.usage();
	}

}
