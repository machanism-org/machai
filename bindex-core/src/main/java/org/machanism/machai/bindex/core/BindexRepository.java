package org.machanism.machai.bindex.core;

import java.util.List;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.schema.Bindex;

/**
 * Repository interface for managing {@link Bindex} records.
 * <p>
 * Provides methods for retrieving, saving, and recommending Bindex entries
 * based on classification, score, embedding vectors, and configuration.
 * </p>
 */
public interface BindexRepository {

    /**
     * Finds and recommends a list of {@link Bindex} entries based on the provided classification string,
     * embedding vector, vector search limits, minimum score threshold, and configuration.
     *
     * @param classificationStr a string describing the desired classification or requirements
     * @param embedding the embedding vector used for semantic search
     * @param vectorSearchLimits the maximum number of results to return from vector search
     * @param score the minimum relevance score threshold for recommended entries
     * @param config the configuration object used for filtering or additional context
     * @return a list of recommended {@link Bindex} entries matching the criteria
     */
    List<Bindex> find(String classificationStr, Iterable<Double> embedding, long vectorSearchLimits, Double score,
                     Configurator config);

    /**
     * Retrieves a {@link Bindex} entry by its unique identifier.
     *
     * @param bindexId the unique identifier of the Bindex entry
     * @return the {@link Bindex} entry if found, or {@code null} if not found
     */
    Bindex getBindex(String bindexId);

    /**
     * Saves a {@link Bindex} entry to the repository, along with its embedding vector.
     *
     * @param bindex the {@link Bindex} object to save
     * @param embeddingBson the embedding vector associated with the Bindex entry
     * @return the unique identifier assigned to the saved Bindex entry
     */
    String save(Bindex bindex, List<Double> embeddingBson);

}