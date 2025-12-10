package it.unifi.attsw.employee_shift_scheduler.db;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoConnectionFactory {

    private static final String CONNECTION_STRING = "mongodb://localhost:27017";
    private static final String DB_NAME = "employee_shift_db";

    private static MongoClient client;               // default client
    private static MongoClient overriddenClient = null; // test-injected client

    /**
     * Allows TestContainers to inject its own MongoClient.
     */
    public static void overrideClient(MongoClient testClient) {
        overriddenClient = testClient;
    }

    /**
     * Returns a MongoDatabase, using overridden client if present.
     */
    public static MongoDatabase getDatabase() {
        if (overriddenClient != null) {
            // TestContainers client
            return overriddenClient.getDatabase(DB_NAME);
        }

        // Normal application client
        if (client == null) {
            client = MongoClients.create(CONNECTION_STRING);
        }
        return client.getDatabase(DB_NAME);
    }

    /**
     * Close all active clients
     */
    public static void close() {
        if (client != null) {
            client.close();
            client = null;
        }
        if (overriddenClient != null) {
            overriddenClient.close();
            overriddenClient = null;
        }
    }
}
