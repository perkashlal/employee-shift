package it.unifi.attsw.employee_shift_scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryEmployeeRepositoryTest {

    private InMemoryEmployeeRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryEmployeeRepository();
    }

    @Test
    void saveAndFindById_shouldReturnSavedEmployee() {
        Employee e = new Employee("Ali", "E001", "cashier");
        repo.save(e);

        Optional<Employee> found = repo.findById("E001");
        assertTrue(found.isPresent());
        assertEquals("Ali", found.get().getName());
        assertEquals("E001", found.get().getEmployeeId());
    }

    @Test
    void findAll_shouldReturnAllSavedEmployees() {
        repo.save(new Employee("Ali","E001","cashier"));
        repo.save(new Employee("Sara","E002","manager"));
        assertEquals(2, repo.findAll().size());
    }
}
