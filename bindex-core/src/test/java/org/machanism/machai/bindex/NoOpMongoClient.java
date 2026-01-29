package org.machanism.machai.bindex;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bson.Document;
import org.bson.codecs.configuration.CodecRegistry;
import org.bson.conversions.Bson;

import com.mongodb.ClientBulkWriteException;
import com.mongodb.ClientSessionOptions;
import com.mongodb.MongoDriverInformation;
import com.mongodb.ReadConcern;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.client.ChangeStreamIterable;
import com.mongodb.client.ClientSession;
import com.mongodb.client.ListDatabasesIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoCluster;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.bulk.ClientBulkWriteOptions;
import com.mongodb.client.model.bulk.ClientBulkWriteResult;
import com.mongodb.client.model.bulk.ClientNamespacedWriteModel;
import com.mongodb.connection.ClusterDescription;

final class NoOpMongoClient implements MongoClient {

    @Override
    public void close() {
        // no-op
    }

    @Override
    public MongoDatabase getDatabase(String databaseName) {
        throw new UnsupportedOperationException("Not used in unit tests");
    }

    @Override
    public MongoIterable<String> listDatabaseNames() {
        throw new UnsupportedOperationException("Not used in unit tests");
    }

    @Override
    public ListDatabasesIterable<Document> listDatabases() {
        throw new UnsupportedOperationException("Not used in unit tests");
    }

    @Override
    public <TResult> ListDatabasesIterable<TResult> listDatabases(Class<TResult> clazz) {
        throw new UnsupportedOperationException("Not used in unit tests");
    }

    @Override
    public ClientSession startSession(ClientSessionOptions options) {
        throw new UnsupportedOperationException("Not used in unit tests");
    }

    @Override
    public ClientSession startSession() {
        throw new UnsupportedOperationException("Not used in unit tests");
    }

    @Override
    public MongoClient withCodecRegistry(CodecRegistry codecRegistry) {
        throw new UnsupportedOperationException("Not used in unit tests");
    }

    @Override
    public MongoClient withReadPreference(ReadPreference readPreference) {
        throw new UnsupportedOperationException("Not used in unit tests");
    }

    @Override
    public MongoClient withWriteConcern(WriteConcern writeConcern) {
        throw new UnsupportedOperationException("Not used in unit tests");
    }

    @Override
    public MongoClient withReadConcern(ReadConcern readConcern) {
        throw new UnsupportedOperationException("Not used in unit tests");
    }

    @Override
    public CodecRegistry getCodecRegistry() {
        return null;
    }

    @Override
    public ReadPreference getReadPreference() {
        return null;
    }

    @Override
    public WriteConcern getWriteConcern() {
        return null;
    }

    @Override
    public ReadConcern getReadConcern() {
        return null;
    }

    @Override
    public Long getTimeout(TimeUnit timeUnit) {
        return null;
    }

    @Override
    public MongoCluster withTimeout(long timeout, TimeUnit timeUnit) {
        return null;
    }

    @Override
    public MongoIterable<String> listDatabaseNames(ClientSession clientSession) {
        return null;
    }

    @Override
    public ListDatabasesIterable<Document> listDatabases(ClientSession clientSession) {
        return null;
    }

    @Override
    public <TResult> ListDatabasesIterable<TResult> listDatabases(ClientSession clientSession, Class<TResult> resultClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable<Document> watch() {
        return null;
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(Class<TResult> resultClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable<Document> watch(List<? extends Bson> pipeline) {
        return null;
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(List<? extends Bson> pipeline, Class<TResult> resultClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable<Document> watch(ClientSession clientSession) {
        return null;
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, Class<TResult> resultClass) {
        return null;
    }

    @Override
    public ChangeStreamIterable<Document> watch(ClientSession clientSession, List<? extends Bson> pipeline) {
        return null;
    }

    @Override
    public <TResult> ChangeStreamIterable<TResult> watch(ClientSession clientSession, List<? extends Bson> pipeline,
            Class<TResult> resultClass) {
        return null;
    }

    @Override
    public ClientBulkWriteResult bulkWrite(List<? extends ClientNamespacedWriteModel> models) throws ClientBulkWriteException {
        return null;
    }

    @Override
    public ClientBulkWriteResult bulkWrite(List<? extends ClientNamespacedWriteModel> models, ClientBulkWriteOptions options)
            throws ClientBulkWriteException {
        return null;
    }

    @Override
    public ClientBulkWriteResult bulkWrite(ClientSession clientSession, List<? extends ClientNamespacedWriteModel> models)
            throws ClientBulkWriteException {
        return null;
    }

    @Override
    public ClientBulkWriteResult bulkWrite(ClientSession clientSession, List<? extends ClientNamespacedWriteModel> models,
            ClientBulkWriteOptions options) throws ClientBulkWriteException {
        return null;
    }

    @Override
    public ClusterDescription getClusterDescription() {
        return null;
    }

    @Override
    public void appendMetadata(MongoDriverInformation mongoDriverInformation) {
        // no-op
    }
}
