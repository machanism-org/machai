package org.machanism.machai.ai.provider;

import java.util.List;

import org.machanism.macha.core.commons.configurator.Configurator;

/**
 * EmbeddingProvider defines the contract for AI embedding providers.
 * <p>
 * Implementations of this interface are responsible for initializing with application
 * configuration and generating embedding vectors for input text. Embeddings are typically
 * used for tasks such as semantic search, similarity comparison, and natural language processing.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 * <pre>
 *     EmbeddingProvider provider = ...;
 *     provider.init(configurator);
 *     List&lt;Double&gt; vector = provider.embedding("example text", 384);
 * </pre>
 *
 * @author Viktor Tovstyi
 * @since 1.1.14
 */
public interface EmbeddingProvider {

	/**
	 * Initializes the provider with application configuration.
	 * @param model model identifier to use
	 * @param conf configuration source
	 */
	void init(String model, Configurator conf);

	/**
	 * Computes an embedding vector for the provided text.
	 *
	 * @param text       the input text
	 * @param dimensions desired embedding dimensionality (provider-specific)
	 * @return the embedding vector
	 */
	List<Double> embedding(String text, long dimensions);

}
