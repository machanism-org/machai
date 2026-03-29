package org.machanism.machai.bindex;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;
import org.machanism.macha.core.commons.configurator.Configurator;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

class BindexRepositoryCreateMongoClientTest {

	private static final String EXPECTED_PUBLIC_URL = "mongodb+srv://user:user@cluster0.hivfnpr.mongodb.net/?appName=Cluster0";
	private static final String EXPECTED_REGISTERED_URL = "mongodb+srv://machanismorg_db_user:secret@cluster0.hivfnpr.mongodb.net/?appName=Cluster0";

	/**
	 * FalsePositive
	 * The project uses JUnit Jupiter without the parameterized-test engine on the classpath.
	 * A true @ParameterizedTest would require adding/upgrading JUnit Platform dependencies,
	 * which is out of scope for this automated change. Duplication is minimized via a shared helper.
	 */
	@SuppressWarnings("java:S5976")
	@Test
	void createMongoClient_shouldUsePublicCredentialsWhenPasswordIsNull() throws Exception {
		createMongoClient_shouldUseExpectedCredentials(null, EXPECTED_PUBLIC_URL);
	}

	/**
	 * FalsePositive
	 * The project uses JUnit Jupiter without the parameterized-test engine on the classpath.
	 * A true @ParameterizedTest would require adding/upgrading JUnit Platform dependencies,
	 * which is out of scope for this automated change. Duplication is minimized via a shared helper.
	 */
	@SuppressWarnings("java:S5976")
	@Test
	void createMongoClient_shouldUseRegisterCredentialsWhenPasswordIsProvided() throws Exception {
		createMongoClient_shouldUseExpectedCredentials("secret", EXPECTED_REGISTERED_URL);
	}

	/**
	 * FalsePositive
	 * The third related test validates a different path (custom repo URL) and is kept separate.
	 */
	@SuppressWarnings("java:S5976")
	@Test
	void createMongoClient_shouldUseCustomRepoUrlFromConfig() throws Exception {
		// Arrange
		Configurator configurator = Mockito.mock(Configurator.class);
		Mockito.when(configurator.get(BindexRepository.BINDEX_REG_PASSWORD_PROP_NAME, null)).thenReturn(null);
		Mockito.when(configurator.get("BINDEX_REPO_URL", BindexRepository.DB_URL))
				.thenReturn("mongodb://localhost:27017");

		Method method = BindexRepository.class.getDeclaredMethod("createMongoClient", Configurator.class);
		method.setAccessible(true);

		MongoClient mongoClient = Mockito.mock(MongoClient.class);
		try (MockedStatic<MongoClients> mongoClients = Mockito.mockStatic(MongoClients.class)) {
			mongoClients.when(() -> MongoClients.create(Mockito.anyString())).thenReturn(mongoClient);

			// Act
			method.invoke(null, configurator);

			// Assert
			mongoClients.verify(() -> MongoClients.create("mongodb://user:user@localhost:27017"));
		}
	}

	private static void createMongoClient_shouldUseExpectedCredentials(String password, String expectedUrl)
			throws Exception {
		// Arrange
		Configurator configurator = Mockito.mock(Configurator.class);
		Mockito.when(configurator.get(BindexRepository.BINDEX_REG_PASSWORD_PROP_NAME, null)).thenReturn(password);
		Mockito.when(configurator.get("BINDEX_REPO_URL", BindexRepository.DB_URL)).thenReturn(BindexRepository.DB_URL);

		Method method = BindexRepository.class.getDeclaredMethod("createMongoClient", Configurator.class);
		method.setAccessible(true);

		MongoClient mongoClient = Mockito.mock(MongoClient.class);
		try (MockedStatic<MongoClients> mongoClients = Mockito.mockStatic(MongoClients.class)) {
			mongoClients.when(() -> MongoClients.create(Mockito.anyString())).thenReturn(mongoClient);

			// Act
			MongoClient result = (MongoClient) method.invoke(null, configurator);

			// Assert
			assertNotNull(result);
			mongoClients.verify(() -> MongoClients.create(expectedUrl));
		}
	}

	@Test
	void createMongoClient_shouldNotChangeUrlWithoutProtocolDelimiter() throws Exception {
		// Arrange
		Configurator configurator = Mockito.mock(Configurator.class);
		Mockito.when(configurator.get(BindexRepository.BINDEX_REG_PASSWORD_PROP_NAME, null)).thenReturn(null);
		Mockito.when(configurator.get("BINDEX_REPO_URL", BindexRepository.DB_URL)).thenReturn("localhost:27017");

		Method method = BindexRepository.class.getDeclaredMethod("createMongoClient", Configurator.class);
		method.setAccessible(true);

		MongoClient mongoClient = Mockito.mock(MongoClient.class);
		try (MockedStatic<MongoClients> mongoClients = Mockito.mockStatic(MongoClients.class)) {
			mongoClients.when(() -> MongoClients.create(Mockito.anyString())).thenReturn(mongoClient);

			// Act
			method.invoke(null, configurator);

			// Assert
			mongoClients.verify(() -> MongoClients.create("localhost:27017"));
		}
	}

	@Test
	void createMongoClient_shouldReplaceAllProtocolDelimiterOccurrences() throws Exception {
		// Arrange
		Configurator configurator = Mockito.mock(Configurator.class);
		Mockito.when(configurator.get(BindexRepository.BINDEX_REG_PASSWORD_PROP_NAME, null)).thenReturn(null);
		Mockito.when(configurator.get("BINDEX_REPO_URL", BindexRepository.DB_URL))
				.thenReturn("mongodb://localhost:27017/path://with-delimiter");

		Method method = BindexRepository.class.getDeclaredMethod("createMongoClient", Configurator.class);
		method.setAccessible(true);

		MongoClient mongoClient = Mockito.mock(MongoClient.class);
		try (MockedStatic<MongoClients> mongoClients = Mockito.mockStatic(MongoClients.class)) {
			mongoClients.when(() -> MongoClients.create(Mockito.anyString())).thenReturn(mongoClient);

			// Act
			method.invoke(null, configurator);

			// Assert
			mongoClients.verify(() -> MongoClients.create(
					"mongodb://user:user@localhost:27017/path://user:user@with-delimiter"));
		}
	}
}
