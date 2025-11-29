package it.unifi.attsw.employee_shift_scheduler;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeWeeklyHourTddTest {

    @Test
    void addingShiftThatExceedsWeeklyLimit_shouldThrow() {
        Employee e = new Employee("Tdd","T001","tester");

        // five 8-hour shifts = 40 hours, adding a 9th hour would exceed 48 if we add more
        Shift day1 = new Shift(LocalDateTime.of(2025,11,17,8,0), LocalDateTime.of(2025,11,17,16,0)); // 8
        Shift day2 = new Shift(LocalDateTime.of(2025,11,18,8,0), LocalDateTime.of(2025,11,18,16,0));
        Shift day3 = new Shift(LocalDateTime.of(2025,11,19,8,0), LocalDateTime.of(2025,11,19,16,0));
        Shift day4 = new Shift(LocalDateTime.of(2025,11,20,8,0), LocalDateTime.of(2025,11,20,16,0));
        Shift day5 = new Shift(LocalDateTime.of(2025,11,21,8,0), LocalDateTime.of(2025,11,21,16,0));

        e.addShift(day1);
        e.addShift(day2);
        e.addShift(day3);
        e.addShift(day4);
        e.addShift(day5);

        // add a 9-hour shift that would push weekly hours over 48
        Shift extra = new Shift(LocalDateTime.of(2025,11,22,8,0), LocalDateTime.of(2025,11,22,18,0)); // 10 hours
        assertThrows(IllegalArgumentException.class, () -> e.addShift(extra));
    }
}
