package it.unifi.attsw.model;

import java.time.Duration;
import java.time.temporal.TemporalField;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Simple Employee domain class with:
 *  - addShift(Shift) checking overlaps and weekly-hours limit (40h)
 *  - removeShiftById(String)
 */
public class Employee {

    // many tests expect a 40-hour weekly limit — use 40
    private static final long MAX_WEEKLY_HOURS = 40L;

    private final String name;
    private final String employeeId;
    private final String role;
    private final List<Shift> scheduledShifts = new ArrayList<>();

    public Employee(String name, String employeeId, String role) {
        this.name = Objects.requireNonNull(name);
        this.employeeId = Objects.requireNonNull(employeeId);
        this.role = role == null ? "" : role;
    }

    public String getName() { return name; }
    public String getEmployeeId() { return employeeId; }
    public String getRole() { return role; }

    public List<Shift> getScheduledShifts() {
        return Collections.unmodifiableList(scheduledShifts);
    }

    public void addShift(Shift shift) {
        Objects.requireNonNull(shift);

        // 1) check overlap
        for (Shift s : scheduledShifts) {
            if (s.overlaps(shift)) {
                throw new IllegalArgumentException("New shift overlaps existing shift: " + s);
            }
        }

        // 2) weekly-hours check (ISO week)
        TemporalField weekField = WeekFields.ISO.weekOfWeekBasedYear();
        TemporalField yearField = WeekFields.ISO.weekBasedYear();

        int newWeek = shift.getStart().get(weekField);
        int newYear = shift.getStart().get(yearField);

        long accumulatedMinutes = 0L;
        for (Shift s : scheduledShifts) {
            int w = s.getStart().get(weekField);
            int y = s.getStart().get(yearField);
            if (w == newWeek && y == newYear) {
                accumulatedMinutes += s.duration().toMinutes();
            }
        }
        accumulatedMinutes += shift.duration().toMinutes();

        long allowedMinutes = MAX_WEEKLY_HOURS * 60L;
        if (accumulatedMinutes > allowedMinutes) {
            throw new IllegalArgumentException(
                    "Adding this shift would exceed weekly limit of " + MAX_WEEKLY_HOURS + " hours");
        }

        scheduledShifts.add(shift);
    }

    /** Remove a shift by id — returns true if removed */
    public boolean removeShiftById(String shiftId) {
        if (shiftId == null) return false;
        return scheduledShifts.removeIf(s -> shiftId.equals(s.getId()));
    }

    public boolean removeShift(Shift shift) {
        return scheduledShifts.remove(shift);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", role='" + role + '\'' +
                ", scheduledShifts=" + scheduledShifts +
                '}';
    }
}
