package it.unifi.attsw.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
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
    }

    @Test
    void addShiftToEmployee_andRemove_shouldWork() {
        Employee e = new Employee("Ali", "E002", "cashier");
        repo.save(e);

        Shift s = new Shift(LocalDateTime.of(2025,11,26,9,0),
                            LocalDateTime.of(2025,11,26,13,0));
        repo.addShiftToEmployee("E002", s);

        Employee got = repo.findById("E002").orElseThrow();
        assertEquals(1, got.getScheduledShifts().size());

        boolean removed = repo.removeShiftFromEmployee("E002", s.getId());
        assertTrue(removed);
        assertTrue(got.getScheduledShifts().isEmpty());
    }

    @Test
    void addingOverlappingShift_shouldThrow() {
        Employee e = new Employee("Ali", "E003", "cashier");
        repo.save(e);

        Shift s1 = new Shift(LocalDateTime.of(2025,11,26,8,0),
                             LocalDateTime.of(2025,11,26,12,0));
        Shift s2 = new Shift(LocalDateTime.of(2025,11,26,11,0),
                             LocalDateTime.of(2025,11,26,15,0));

        repo.addShiftToEmployee("E003", s1);
        assertThrows(IllegalArgumentException.class, () -> repo.addShiftToEmployee("E003", s2));
    }

    @Test
    void deleteById_shouldRemoveEmployee() {
        Employee e = new Employee("Ali", "E004", "cashier");
        repo.save(e);
        assertTrue(repo.deleteById("E004"));
        assertFalse(repo.findById("E004").isPresent());
    }

    @Test
    void findAll_shouldReturnMultiple() {
        repo.save(new Employee("A", "A1", "r"));
        repo.save(new Employee("B", "B1", "r"));
        assertEquals(2, repo.findAll().size());
    }
}