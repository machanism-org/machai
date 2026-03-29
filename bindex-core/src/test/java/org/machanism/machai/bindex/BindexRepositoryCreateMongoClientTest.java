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

	@Test
	void createMongoClient_shouldUsePublicCredentialsWhenPasswordIsNull() throws Exception {
		// Arrange
		Configurator configurator = Mockito.mock(Configurator.class);
		Mockito.when(configurator.get(BindexRepository.BINDEX_REG_PASSWORD_PROP_NAME, null)).thenReturn(null);
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
			mongoClients.verify(() -> MongoClients.create(
					"mongodb+srv://user:user@cluster0.hivfnpr.mongodb.net/?appName=Cluster0"));
		}
	}

	@Test
	void createMongoClient_shouldUseRegisterCredentialsWhenPasswordIsProvided() throws Exception {
		// Arrange
		Configurator configurator = Mockito.mock(Configurator.class);
		Mockito.when(configurator.get(BindexRepository.BINDEX_REG_PASSWORD_PROP_NAME, null)).thenReturn("secret");
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
			mongoClients.verify(() -> MongoClients.create(
					"mongodb+srv://machanismorg_db_user:secret@cluster0.hivfnpr.mongodb.net/?appName=Cluster0"));
		}
	}

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
