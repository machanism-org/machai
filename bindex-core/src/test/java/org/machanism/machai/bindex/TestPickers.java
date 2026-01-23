package org.machanism.machai.bindex;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import com.mongodb.ClientBulkWriteException;
import com.mongodb.MongoDriverInformation;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCluster;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.bulk.ClientBulkWriteOptions;
import com.mongodb.client.model.bulk.ClientBulkWriteResult;
import com.mongodb.client.model.bulk.ClientNamespacedWriteModel;
import com.mongodb.connection.ClusterDescription;

final class TestPickers {

	private TestPickers() {
	}

	static Picker newPickerWithoutMongo() throws Exception {
		Constructor<Picker> ctor = Picker.class.getDeclaredConstructor(
				org.machanism.machai.ai.manager.GenAIProvider.class,
				String.class);
		ctor.setAccessible(true);

		// Create with a syntactically valid connection string; we will immediately swap
		// the client.
		Picker picker = ctor.newInstance(null, "mongodb://localhost:27017");

		Field mongoClientField = Picker.class.getDeclaredField("mongoClient");
		mongoClientField.setAccessible(true);
		mongoClientField.set(picker, new NoOpMongoClient());

		Field collectionField = Picker.class.getDeclaredField("collection");
		collectionField.setAccessible(true);
		collectionField.set(picker, null);

		return picker;
	}

	private static final class NoOpMongoClient implements MongoClient {

		@Override
		public void close() {
			// no-op
		}

		@Override
		public com.mongodb.client.MongoDatabase getDatabase(String databaseName) {
			throw new UnsupportedOperationException("Not used in unit tests");
		}

		@Override
		public com.mongodb.client.MongoIterable<String> listDatabaseNames() {
			throw new UnsupportedOperationException("Not used in unit tests");
		}

		@Override
		public com.mongodb.client.ListDatabasesIterable<org.bson.Document> listDatabases() {
			throw new UnsupportedOperationException("Not used in unit tests");
		}

		@Override
		public <TResult> com.mongodb.client.ListDatabasesIterable<TResult> listDatabases(Class<TResult> clazz) {
			throw new UnsupportedOperationException("Not used in unit tests");
		}

		@Override
		public com.mongodb.client.ClientSession startSession(com.mongodb.ClientSessionOptions options) {
			throw new UnsupportedOperationException("Not used in unit tests");
		}

		@Override
		public MongoClient withCodecRegistry(org.bson.codecs.configuration.CodecRegistry codecRegistry) {
			throw new UnsupportedOperationException("Not used in unit tests");
		}

		@Override
		public MongoClient withReadPreference(com.mongodb.ReadPreference readPreference) {
			throw new UnsupportedOperationException("Not used in unit tests");
		}

		@Override
		public MongoClient withWriteConcern(com.mongodb.WriteConcern writeConcern) {
			throw new UnsupportedOperationException("Not used in unit tests");
		}

		@Override
		public MongoClient withReadConcern(com.mongodb.ReadConcern readConcern) {
			throw new UnsupportedOperationException("Not used in unit tests");
		}

		@Override
		public CodecRegistry getCodecRegistry() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ReadPreference getReadPreference() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public WriteConcern getWriteConcern() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ReadConcern getReadConcern() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public Long getTimeout(TimeUnit timeUnit) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MongoCluster withTimeout(long timeout, TimeUnit timeUnit) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public MongoIterable<String> listDatabaseNames(ClientSession clientSession) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ListDatabasesIterable<Document> listDatabases(ClientSession clientSession) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <TResult> ListDatabasesIterable<TResult> listDatabases(ClientSession clientSession,
				Class<TResult> resultClass) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChangeStreamIterable<Document> watch() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <TResult> ChangeStreamIterable<TResult> watch(Class<TResult> resultClass) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChangeStreamIterable<Document> watch(List<? extends Bson> pipeline) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <TResult> ChangeStreamIterable<TResult> watch(List<? extends Bson> pipeline,
				Class<TResult> resultClass) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChangeStreamIterable<Document> watch(ClientSession clientSession) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, Class<TResult> resultClass) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ChangeStreamIterable<Document> watch(ClientSession clientSession, List<? extends Bson> pipeline) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, List<? extends Bson> pipeline,
				Class<TResult> resultClass) {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ClientBulkWriteResult bulkWrite(List<? extends ClientNamespacedWriteModel> models)
				throws ClientBulkWriteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ClientBulkWriteResult bulkWrite(List<? extends ClientNamespacedWriteModel> models,
				ClientBulkWriteOptions options) throws ClientBulkWriteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ClientBulkWriteResult bulkWrite(ClientSession clientSession,
				List<? extends ClientNamespacedWriteModel> models) throws ClientBulkWriteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ClientBulkWriteResult bulkWrite(ClientSession clientSession,
				List<? extends ClientNamespacedWriteModel> models, ClientBulkWriteOptions options)
				throws ClientBulkWriteException {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public ClusterDescription getClusterDescription() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public void appendMetadata(MongoDriverInformation mongoDriverInformation) {
			// TODO Auto-generated method stub

		}

		@Override
		public ClientSession startSession() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
