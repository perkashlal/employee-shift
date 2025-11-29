package it.unifi.attsw.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeServiceTest {

    private EmployeeService service;
    private InMemoryEmployeeRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryEmployeeRepository();
        service = new EmployeeService(repo);
    }

    @Test
    void addShift_shouldAddShiftToEmployee() {
        Employee e = new Employee("Ali", "E001", "cashier");
        repo.save(e);

        Shift s = new Shift("S1", LocalDateTime.of(2025,11,24,9,0),
                                LocalDateTime.of(2025,11,24,17,0));
        service.addShiftToEmployee("E001", s);

        Employee updated = repo.findById("E001");
        assertEquals(1, updated.getScheduledShifts().size());
        assertEquals("S1", updated.getScheduledShifts().get(0).getShiftId());
    }

    @Test
    void addShift_exceedWeeklyLimit_shouldThrow() {
        Employee e = new Employee("Ali", "E002", "cashier");
        repo.save(e);

        // create two shifts that together exceed 48 (or match your chosen limit)
        Shift s1 = new Shift("S1", LocalDateTime.of(2025,11,24,0,0),
                                 LocalDateTime.of(2025,11,24,23,0)); // 23h
        Shift s2 = new Shift("S2", LocalDateTime.of(2025,11,25,0,0),
                                 LocalDateTime.of(2025,11,25,23,59)); // ~24h -> total > 47

        service.addShiftToEmployee("E002", s1);
        assertThrows(IllegalArgumentException.class, () -> service.addShiftToEmployee("E002", s2));
    }
}
