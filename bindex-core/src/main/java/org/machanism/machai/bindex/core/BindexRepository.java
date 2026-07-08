package org.machanism.machai.bindex.core;

import java.util.Collection;
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
	 * Finds and retrieves matching Bindex records using a combination of vector
	 * similarity search and metadata classification filtering.
	 * <p>
	 * This method performs semantic searches using the provided vector embedding,
	 * narrows down results based on specific classification tags, and applies
	 * threshold filters to deliver highly relevant results.
	 * </p>
	 *
	 * @param classifications    an array of {@link Classification} filters to
	 *                           restrict the search scope, or {@code null}/empty to
	 *                           search across all categories
	 * @param embedding          the query vector representation used for
	 *                           calculating semantic similarity
	 * @param vectorSearchLimits the maximum number of candidates to evaluate or
	 *                           retrieve during the vector search phase
	 * @param score              the minimum similarity score threshold (only
	 *                           records with a relevance score equal to or higher
	 *                           than this value will be returned)
	 * @param config             the {@link Configurator} containing execution or
	 *                           contextual configurations
	 * @return a collection of {@link BindexInfo} elements matching the search
	 *         criteria, typically sorted by descending similarity score
	 */
	Collection<BindexInfo> find(Classification[] classifications, List<Double> embedding, long vectorSearchLimits,
			double score,
			Configurator config);

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