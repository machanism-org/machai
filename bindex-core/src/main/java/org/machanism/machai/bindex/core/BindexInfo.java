package org.machanism.machai.bindex.core;

/**
 * Represents metadata information for a Bindex record, including its
 * identifier, version, description, and relevance score.
 * <p>
 * This class is used to encapsulate summary details about a Bindex entry,
 * typically for search, recommendation, or reporting purposes.
 * </p>
 *
 * @author Viktor Tovstyi
 * @since 1.2.0
 */
public class BindexInfo {

	/** The unique identifier for the Bindex record. */
	private String id;

	/** The version string of the Bindex record. */
	private String version;

	/** A human-readable description of the Bindex record. */
	private String description;

	/** The relevance score associated with the Bindex record. */
	private double score;

	/**
	 * Returns the unique identifier for the Bindex record.
	 *
	 * @return the Bindex id
	 */
	public String getId() {
		return id;
	}

	/**
	 * Sets the unique identifier for the Bindex record.
	 *
	 * @param id the Bindex id
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the version string of the Bindex record.
	 *
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * Sets the version string of the Bindex record.
	 *
	 * @param version the version
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * Returns the description of the Bindex record.
	 *
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the description of the Bindex record.
	 *
	 * @param description the description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * Returns the relevance score associated with the Bindex record.
	 *
	 * @return the score
	 */
	public double getScore() {
		return score;
	}

	/**
	 * Sets the relevance score associated with the Bindex record.
	 *
	 * @param score the score
	 */
	public void setScore(double score) {
		this.score = score;
	}
}