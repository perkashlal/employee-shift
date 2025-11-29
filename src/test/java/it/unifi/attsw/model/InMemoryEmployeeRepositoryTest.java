package it.unifi.attsw.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

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

        Employee found = repo.findById("E001");
        assertNotNull(found);
        assertEquals("Ali", found.getName());
        assertEquals("E001", found.getEmployeeId());
    }

    @Test
    void findAll_shouldReturnAllSavedEmployees() {
        repo.save(new Employee("Ali", "E001", "cashier"));
        repo.save(new Employee("Sara", "E002", "manager"));

        List<Employee> all = repo.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void saveWithSameId_shouldOverwrite() {
        repo.save(new Employee("Ali", "E001", "cashier"));
        repo.save(new Employee("Ali2", "E001", "cashier"));

        Employee found = repo.findById("E001");
        assertEquals("Ali2", found.getName());
    }
}
