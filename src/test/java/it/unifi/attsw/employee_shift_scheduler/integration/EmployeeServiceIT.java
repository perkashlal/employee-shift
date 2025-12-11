package it.unifi.attsw.employee_shift_scheduler.integration;

import it.unifi.attsw.employee_shift_scheduler.Employee;
import it.unifi.attsw.employee_shift_scheduler.EmployeeService;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
public class EmployeeServiceIT {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:6.0");

    private MongoClient mongoClient;
    private MongoDatabase testDb;
    private MongoEmployeeRepository repo;
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        String uri = MONGO.getReplicaSetUrl();
        mongoClient = MongoClients.create(uri);
        testDb = mongoClient.getDatabase("it_service_db");
        testDb.getCollection("employees").drop();

        repo = new MongoEmployeeRepository(testDb);
        service = new EmployeeService(repo);
    }

    @AfterEach
    void tearDown() {
        if (testDb != null) testDb.getCollection("employees").drop();
        if (mongoClient != null) mongoClient.close();
    }

    @Test
    void addShiftToEmployee_shouldPersist_shiftAppearsInRepo() {
        Employee emp = new Employee("Service Emp", "EMP_S_01", "Worker");
        repo.save(emp);

        Shift shift = new Shift("SVC-1", Instant.parse("2025-11-10T09:00:00Z"), Instant.parse("2025-11-10T12:00:00Z"));
        Employee updated = service.addShiftToEmployee("EMP_S_01", shift);

        assertThat(updated).isNotNull();
        assertThat(updated.getScheduledShifts()).hasSize(1);
        assertThat(updated.getScheduledShifts().get(0).getId()).isEqualTo("SVC-1");

        // persisted check
        Optional<Employee> fromDb = repo.findById("EMP_S_01");
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getScheduledShifts()).hasSize(1);
    }

    @Test
    void removeShiftFromEmployee_shouldRemove_andReturnUpdatedEmployee() {
        Employee emp = new Employee("Rem Emp", "EMP_S_02", "Worker");
        Shift s = new Shift("SVC-2", Instant.parse("2025-11-11T09:00:00Z"), Instant.parse("2025-11-11T12:00:00Z"));
        emp.addShift(s);
        repo.save(emp);

        Employee after = service.removeShiftFromEmployee("EMP_S_02", "SVC-2");

        assertThat(after).isNotNull();
        assertThat(after.getScheduledShifts()).isEmpty();

        Optional<Employee> fromDb = repo.findById("EMP_S_02");
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getScheduledShifts()).isEmpty();
    }

    @Test
    void overlap_prevention_shouldThrow_whenAddingOverlappingShift() {
        Employee emp = new Employee("Overlap Emp", "EMP_S_03", "Worker");
        Shift existing = new Shift("SVC-3", Instant.parse("2025-11-12T08:00:00Z"), Instant.parse("2025-11-12T12:00:00Z"));
        emp.addShift(existing);
        repo.save(emp);

        Shift overlapping = new Shift("SVC-3B", Instant.parse("2025-11-12T11:00:00Z"), Instant.parse("2025-11-12T14:00:00Z"));

        // adapt exception class if your service throws a specific type
        assertThrows(RuntimeException.class, () -> service.addShiftToEmployee("EMP_S_03", overlapping));

        // db unchanged
        Optional<Employee> fromDb = repo.findById("EMP_S_03");
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getScheduledShifts()).hasSize(1);
    }

    @Test
    void weekly_hours_limit_shouldBeEnforced() {
        // This test assumes your EmployeeService enforces weekly hours limit when adding shifts.
        // Adjust shift durations and expected exception as per your implementation.

        Employee emp = new Employee("Hours Emp", "EMP_S_04", "Worker");

        // create several long shifts to exceed weekly limit (example: if weekly limit is 40h)
        Shift s1 = new Shift("H-1", Instant.parse("2025-11-13T00:00:00Z"), Instant.parse("2025-11-14T00:00:00Z")); // 24h
        Shift s2 = new Shift("H-2", Instant.parse("2025-11-15T00:00:00Z"), Instant.parse("2025-11-16T00:00:00Z")); // 24h

        emp.addShift(s1);
        repo.save(emp);

        // adding s2 should exceed weekly limit -> expect exception (adjust if your service returns false instead)
        assertThrows(RuntimeException.class, () -> service.addShiftToEmployee("EMP_S_04", s2));

        // ensure only first shift persisted
        Optional<Employee> fromDb = repo.findById("EMP_S_04");
        assertThat(fromDb).isPresent();
        assertThat(fromDb.get().getScheduledShifts()).hasSize(1);
    }
}
