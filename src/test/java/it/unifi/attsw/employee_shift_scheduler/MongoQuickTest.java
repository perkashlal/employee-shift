package it.unifi.attsw.employee_shift_scheduler;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import it.unifi.attsw.employee_shift_scheduler.db.MongoConnectionFactory;
import it.unifi.attsw.employee_shift_scheduler.repository.EmployeeRepository;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MongoQuickTest {

    @Container
    static final MongoDBContainer mongo = new MongoDBContainer("mongo:6.0.6");

    private MongoClient testClient;
    private EmployeeRepository repo;

    @BeforeAll
    void setup() {
        // start container and create client
        String uri = mongo.getReplicaSetUrl();
        testClient = MongoClients.create(uri);

        // Inject TestContainers client into your factory
        MongoConnectionFactory.overrideClient(testClient);

        // Use your existing MongoEmployeeRepository constructor that accepts MongoDatabase
        MongoDatabase db = MongoConnectionFactory.getDatabase();
        repo = new MongoEmployeeRepository(db);
    }

    @AfterAll
    void cleanup() {
        // close factory which will close the overridden client
        MongoConnectionFactory.close();
        try {
            if (testClient != null) testClient.close();
        } catch (Exception ignored) {}
    }

    @Test
    void testSaveAndFindEmployee() {
        Employee e = new Employee("John Doe", "E001", "Manager");
        repo.save(e);

        Employee found = repo.findById("E001").orElse(null);
        assertNotNull(found, "Employee should be found");
        assertEquals("John Doe", found.getName(), "Employee name should match");
    }
}
