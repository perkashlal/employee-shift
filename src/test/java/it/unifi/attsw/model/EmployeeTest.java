package it.unifi.attsw.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    void addNonOverlappingShifts_shouldSucceed() {
        Employee e = new Employee("Ali", "E001", "cashier");
        Shift s1 = new Shift(LocalDateTime.of(2025,11,26,8,0),
                             LocalDateTime.of(2025,11,26,12,0));
        Shift s2 = new Shift(LocalDateTime.of(2025,11,26,13,0),
                             LocalDateTime.of(2025,11,26,17,0));
        e.addShift(s1);
        e.addShift(s2);
        assertEquals(2, e.getScheduledShifts().size());
    }

    @Test
    void addOverlappingShift_shouldThrow() {
        Employee e = new Employee("Ali", "E001", "cashier");
        Shift s1 = new Shift(LocalDateTime.of(2025,11,26,8,0),
                             LocalDateTime.of(2025,11,26,12,0));
        Shift s2 = new Shift(LocalDateTime.of(2025,11,26,11,0),
                             LocalDateTime.of(2025,11,26,15,0));
        e.addShift(s1);
        assertThrows(IllegalArgumentException.class, () -> e.addShift(s2));
    }

    @Test
    void removeShiftById_shouldRemove() {
        Employee e = new Employee("Ali", "E001", "cashier");
        Shift s1 = new Shift(LocalDateTime.of(2025,11,26,8,0),
                             LocalDateTime.of(2025,11,26,12,0));
        e.addShift(s1);
        boolean removed = e.removeShiftById(s1.getId());
        assertTrue(removed);
        assertTrue(e.getScheduledShifts().isEmpty());
    }
}