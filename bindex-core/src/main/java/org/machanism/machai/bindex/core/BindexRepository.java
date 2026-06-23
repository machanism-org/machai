package org.machanism.machai.bindex.core;

import java.util.List;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.schema.Bindex;
import org.machanism.machai.schema.Classification;

/**
 * Repository interface for managing {@link Bindex} records.
 * <p>
 * Provides methods for retrieving, saving, and recommending Bindex entries
 * based on classification, score, embedding vectors, and configuration.
 * </p>
 */
public interface BindexRepository {

	/**
	 * Finds and returns a list of {@link Bindex} records that match the specified
	 * classification and embedding criteria.
	 * <p>
	 * This method performs a vector similarity search using the provided embedding
	 * and classification string. The search can be limited by the number of
	 * dimensions, a maximum number of results, and a minimum relevance score.
	 * </p>
	 *
	 * @param classifications    The classifications used to filter or categorize
	 *                           the search.
	 * @param dimensions         The number of dimensions in the embedding vector.
	 * @param embedding          The embedding vector used for similarity search.
	 * @param vectorSearchLimits The maximum number of results to return or the
	 *                           search limit for vector similarity.
	 * @param score              The minimum relevance score threshold for results;
	 *                           only records with a score equal to or higher than
	 *                           this value will be included.
	 * @param config             The {@link Configurator} instance providing
	 *                           additional configuration or context for the search.
	 * @return A list of {@link Bindex} objects matching the search criteria.
	 */
	List<Bindex> find(Classification[] classifications, int dimensions, Iterable<Double> embedding,
			long vectorSearchLimits, Double score, Configurator config);

	/**
	 * Retrieves a {@link Bindex} entry by its unique identifier.
	 *
	 * @param bindexId the unique identifier of the Bindex entry
	 * @return the {@link Bindex} entry if found, or {@code null} if not found
	 */
	Bindex getBindex(String bindexId);

	/**
	 * Saves a {@link Bindex} entry to the repository, along with its embedding
	 * vector.
	 *
	 * @param bindex        the {@link Bindex} object to save
	 * @param embeddingBson the embedding vector associated with the Bindex entry
	 * @return the unique identifier assigned to the saved Bindex entry
	 */
	String save(Bindex bindex, List<Double> embeddingBson);

}