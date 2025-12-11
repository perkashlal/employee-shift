package it.unifi.attsw.employee_shift_scheduler.integration;

import it.unifi.attsw.employee_shift_scheduler.Employee;
import it.unifi.attsw.employee_shift_scheduler.MongoEmployeeRepository;
import it.unifi.attsw.employee_shift_scheduler.Shift;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
public class MongoEmployeeRepositoryIT {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:6.0");

    private MongoClient mongoClient;
    private MongoDatabase testDb;
    private MongoEmployeeRepository repo;

    @BeforeEach
    void setUp() {
        String uri = MONGO.getReplicaSetUrl();
        mongoClient = MongoClients.create(uri);
        testDb = mongoClient.getDatabase("it_repo_db");
        // ensure clean collection
        testDb.getCollection("employees").drop();

        repo = new MongoEmployeeRepository(testDb); // uses your injected-db ctor
    }

    @AfterEach
    void tearDown() {
        if (testDb != null) testDb.getCollection("employees").drop();
        if (mongoClient != null) mongoClient.close();
    }

    @Test
    void save_and_findById_shouldWork() {
        Employee e = new Employee("Saver Name", "EMP_R_01", "RoleX");
        Shift s = new Shift("SAV-1", Instant.parse("2025-11-01T08:00:00Z"), Instant.parse("2025-11-01T12:00:00Z"));
        e.addShift(s);

        repo.save(e);

        Optional<Employee> opt = repo.findById("EMP_R_01");
        assertThat(opt).isPresent();
        Employee loaded = opt.get();
        assertThat(loaded.getEmployeeId()).isEqualTo("EMP_R_01");
        assertThat(loaded.getScheduledShifts()).hasSize(1);
        assertThat(loaded.getScheduledShifts().get(0).getId()).isEqualTo("SAV-1");
    }

    @Test
    void findAll_and_deleteById_shouldWork() {
        // create two employees
        Employee e1 = new Employee("A", "EMP_R_02", "R");
        Employee e2 = new Employee("B", "EMP_R_03", "R");
        repo.save(e1);
        repo.save(e2);

        List<Employee> all = repo.findAll();
        assertThat(all).extracting(Employee::getEmployeeId).contains("EMP_R_02", "EMP_R_03");

        // delete one
        repo.deleteById("EMP_R_02");
        Optional<Employee> after = repo.findById("EMP_R_02");
        assertThat(after).isEmpty();

        List<Employee> allAfter = repo.findAll();
        assertThat(allAfter).extracting(Employee::getEmployeeId).doesNotContain("EMP_R_02");
    }
}
