package org.machanism.machai.bindex;

import java.util.List;

import org.machanism.macha.core.commons.configurator.Configurator;
import org.machanism.machai.schema.Bindex;

/**
 * Repository interface for managing {@link Bindex} records.
 * <p>
 * Provides methods for retrieving, saving, and recommending Bindex entries
 * based on classification, score, and configuration.
 * </p>
 */
public interface BindexRepository {

	List<Bindex> find(String classificationStr, Iterable<Double> embedding, long vectorSearchLimits, Double score,
			Configurator config);

	/**
	 * Retrieves a {@link Bindex} entry by its unique identifier.
	 *
	 * @param bindexId the unique identifier of the Bindex entry
	 * @return the {@link Bindex} entry if found, or {@code null} if not found
	 */
	Bindex getBindex(String bindexId);

	String save(Bindex bindex, List<Double> embeddingBson);

}