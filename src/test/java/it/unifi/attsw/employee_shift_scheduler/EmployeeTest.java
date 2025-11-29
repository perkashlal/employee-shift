package it.unifi.attsw.employee_shift_scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeTest {

    @Test
    void addNonOverlappingShifts_shouldSucceed() {
        Employee e = new Employee("Ali","E001","cashier");
        Shift s1 = new Shift(LocalDateTime.of(2025,11,26,8,0),
                             LocalDateTime.of(2025,11,26,12,0));
        Shift s2 = new Shift(LocalDateTime.of(2025,11,26,13,0),
                             LocalDateTime.of(2025,11,26,17,0));
        e.addShift(s1);
        e.addShift(s2);
        assertEquals(2, e.getScheduledShifts().size());
    }
}
