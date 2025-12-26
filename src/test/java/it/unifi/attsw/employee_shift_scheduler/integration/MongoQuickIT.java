package it.unifi.attsw.employee_shift_scheduler.integration;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

import it.unifi.attsw.employee_shift_scheduler.repository.EmployeeRepository;
import it.unifi.attsw.employee_shift_scheduler.Employee;
import it.unifi.attsw.employee_shift_scheduler.MongoEmployeeRepository;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class MongoQuickIT {

    @Container
    static final MongoDBContainer mongo =
            new MongoDBContainer("mongo:6.0.6");

    private MongoClient client;
    private EmployeeRepository repo;

    @BeforeAll
    void setup() {
        // Connect using Testcontainers URI
        client = MongoClients.create(mongo.getConnectionString());

        // Select the test database
        MongoDatabase database = client.getDatabase("testdb");

        // Use your existing repository implementation
        repo = new MongoEmployeeRepository(database);
    }

    @AfterAll
    void cleanup() {
        if (client != null) client.close();
    }

    @Test
    void testSaveAndFindEmployee() {
        Employee e = new Employee("John Doe", "E001", "Manager");
        repo.save(e);

        Employee found = repo.findById("E001").orElse(null);

        assertNotNull(found, "Employee should be found");
        assertEquals("John Doe", found.getName());
    }
}
