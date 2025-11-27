package it.unifi.attsw.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;

class EmployeeWeeklyHoursTddTest {

    /**
     * Build an employee with 5 shifts of 8 hours in the same ISO week (40 hours).
     * Adding any further 8-hour shift in that same week should throw.
     */
    @Test
    void addingShiftThatExceedsWeeklyLimit_shouldThrow() {
        Employee e = new Employee("Tdd", "T001", "tester");

        // create five 8-hour shifts inside the same week
        Shift day1 = new Shift(LocalDateTime.of(2025,11,17,8,0),
                               LocalDateTime.of(2025,11,17,16,0)); // Monday
        Shift day2 = new Shift(LocalDateTime.of(2025,11,18,8,0),
                               LocalDateTime.of(2025,11,18,16,0)); // Tue
        Shift day3 = new Shift(LocalDateTime.of(2025,11,19,8,0),
                               LocalDateTime.of(2025,11,19,16,0)); // Wed
        Shift day4 = new Shift(LocalDateTime.of(2025,11,20,8,0),
                               LocalDateTime.of(2025,11,20,16,0)); // Thu
        Shift day5 = new Shift(LocalDateTime.of(2025,11,21,8,0),
                               LocalDateTime.of(2025,11,21,16,0)); // Fri

        e.addShift(day1);
        e.addShift(day2);
        e.addShift(day3);
        e.addShift(day4);
        e.addShift(day5);

        // now try to add another 8-hour shift in the same week (should fail)
        Shift extra = new Shift(LocalDateTime.of(2025,11,22,8,0),
                                LocalDateTime.of(2025,11,22,16,0)); // Saturday same week

        assertThrows(IllegalArgumentException.class, () -> e.addShift(extra));
    }
}
