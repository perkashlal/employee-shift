package it.unifi.attsw.employee_shift_scheduler;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;

/**
 * Simple provider for a MongoClient instance.
 * Use the connection string form: mongodb://user:pwd@host:port/?authSource=db
 */
public final class MongoClientProvider {

    private final MongoClient client;

    /**
     * Create a provider using the given connection string.
     * Example: "mongodb://appuser:pass%2123@localhost:27017/?authSource=employee_shift_db"
     *
     * @param connectionString Mongo connection string
     */
    public MongoClientProvider(String connectionString) {
        this.client = MongoClients.create(connectionString);
    }

    /**
     * Create a provider using a connection string that does not include credentials.
     * Example: "mongodb://localhost:27017"
     *
     * @param connectionString connection string
     * @param unused           overload marker (pass null)
     */
    public MongoClientProvider(String connectionString, Object unused) {
        this.client = MongoClients.create(connectionString);
    }

    public MongoClient getClient() {
        return client;
    }

    public void close() {
        client.close();
    }
}
