package it.unifi.attsw.employee_shift_scheduler.integration;

import it.unifi.attsw.employee_shift_scheduler.Controller;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Integration tests exercising Controller -> Service -> MongoRepository -> MongoDB
 *
 * Uses the existing constructor:
 *   MongoEmployeeRepository(MongoDatabase injectedDb)
 *
 * and controller API methods already present in your Controller:
 *   - addShiftToEmployee(String, Shift)
 *   - addShiftToEmployeeOrThrow(String, Shift)   <- used for IT
 *   - listShiftsForEmployee(String)
 *   - removeShiftFromEmployeeAndReturn(String, String)
 */
@Testcontainers
public class ControllerIT {

    @Container
    static final MongoDBContainer MONGO = new MongoDBContainer("mongo:6.0");

    private MongoClient mongoClient;
    private MongoDatabase testDb;
    private MongoEmployeeRepository repo;
    private EmployeeService service;
    private Controller controller;

    @BeforeEach
    void setup() {
        // connect to Testcontainers MongoDB
        String uri = MONGO.getReplicaSetUrl();
        mongoClient = MongoClients.create(uri);

        // use a dedicated test database name
        testDb = mongoClient.getDatabase("it_test_db");

        // drop collection to start clean (no repo helper required)
        testDb.getCollection("employees").drop();

        // create repo using the injected-db constructor you already have
        repo = new MongoEmployeeRepository(testDb);

        // wire service and controller (Controller(service) uses NoOpEmployeeView)
        service = new EmployeeService(repo);
        controller = new Controller(service);
    }

    @AfterEach
    void tearDown() {
        // cleanup DB & close client
        if (testDb != null) {
            testDb.getCollection("employees").drop();
        }
        if (mongoClient != null) {
            mongoClient.close();
        }
    }

    @Test
    void viewAssignedShifts_returns_data_saved_in_mongo() {
        // given
        Employee emp = new Employee("Marco Rossi", "EMP_IT_01", "Clerk");
        Shift shift = new Shift("SH-100",
                Instant.parse("2025-12-01T09:00:00Z"),
                Instant.parse("2025-12-01T13:00:00Z"));
        emp.addShift(shift);
        repo.save(emp);

        // when (use the controller method that exists in your Controller)
        List<Shift> loaded = controller.listShiftsForEmployee("EMP_IT_01");

        // then
        assertThat(loaded).isNotNull();
        assertThat(loaded).hasSize(1);
        assertThat(loaded.get(0).getId()).isEqualTo("SH-100");
    }

    @Test
    void addShift_viaController_persists_to_mongo() {
        // given: employee saved with no shifts
        Employee emp = new Employee("Giulia Bianchi", "EMP_IT_02", "Cashier");
        repo.save(emp);

        // when: add shift via controller (this calls employeeService.addShiftToEmployee internally)
        Shift newShift = new Shift("SH-200",
                Instant.parse("2025-12-02T14:00:00Z"),
                Instant.parse("2025-12-02T18:00:00Z"));
        controller.addShiftToEmployee("EMP_IT_02", newShift);

        // then: read back with repo
        Optional<Employee> opt = repo.findById("EMP_IT_02");
        assertThat(opt).isPresent();
        Employee loaded = opt.get();
        assertThat(loaded.getScheduledShifts()).hasSize(1);
        assertThat(loaded.getScheduledShifts().get(0).getId()).isEqualTo("SH-200");
    }

    @Test
    void removeShift_viaController_removes_from_mongo() {
        // given: employee with one shift persisted
        Employee emp = new Employee("Luca Verdi", "EMP_IT_03", "Manager");
        Shift s = new Shift("SH-300",
                Instant.parse("2025-12-03T08:00:00Z"),
                Instant.parse("2025-12-03T12:00:00Z"));
        emp.addShift(s);
        repo.save(emp);

        // when: remove via controller. Use the helper that returns the updated Employee if available
        // Controller provides removeShiftFromEmployeeAndReturn -> prefer that for assertions
        Employee updated = controller.removeShiftFromEmployeeAndReturn("EMP_IT_03", "SH-300");

        // then
        assertThat(updated).isNotNull();
        assertThat(updated.getScheduledShifts()).isEmpty();

        // also verify persistence
        Optional<Employee> opt = repo.findById("EMP_IT_03");
        assertThat(opt).isPresent();
        assertThat(opt.get().getScheduledShifts()).isEmpty();
    }

    @Test
    void overlap_prevention_enforced_when_adding_shift_through_controller() {
        // given: existing shift
        Employee emp = new Employee("Anna Neri", "EMP_IT_04", "Staff");
        Shift existing = new Shift("SH-400",
                Instant.parse("2025-12-04T09:00:00Z"),
                Instant.parse("2025-12-04T13:00:00Z"));
        emp.addShift(existing);
        repo.save(emp);

        // when: create overlapping shift
        Shift overlap = new Shift("SH-401",
                Instant.parse("2025-12-04T12:00:00Z"),
                Instant.parse("2025-12-04T16:00:00Z"));

        // then: service/controller is expected to throw a RuntimeException (adjust if your service throws a specific type)
        assertThrows(RuntimeException.class, () -> controller.addShiftToEmployeeOrThrow("EMP_IT_04", overlap));

        // and DB still has only the original shift
        Optional<Employee> opt = repo.findById("EMP_IT_04");
        assertThat(opt).isPresent();
        assertThat(opt.get().getScheduledShifts()).hasSize(1);
        assertThat(opt.get().getScheduledShifts().get(0).getId()).isEqualTo("SH-400");
    }
}
