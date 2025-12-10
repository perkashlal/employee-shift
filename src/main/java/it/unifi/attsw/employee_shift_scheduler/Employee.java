// src/main/java/it/unifi/attsw/employee_shift_scheduler/Employee.java
package it.unifi.attsw.employee_shift_scheduler;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Minimal Employee with shift management.
 *
 * - Keeps an internal mutable list of scheduledShifts (final reference).
 * - Exposes addShift/removeShiftById and a defensive getter.
 * - Provides setScheduledShifts(...) to replace internal contents (keeps instance).
 */
public class Employee {

    private final String name;
    private final String employeeId;
    private final String role;
    private final List<Shift> scheduledShifts = new ArrayList<>();

    private static final long WEEKLY_LIMIT_HOURS = 48; // simple default

    /**
     * Main constructor used in the application.
     */
    public Employee(String name, String employeeId, String role) {
        this.name = Objects.requireNonNull(name, "name");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId");
        this.role = role == null ? "" : role;
    }

    /**
     * Convenience constructor that accepts initial shifts.
     * The provided list is copied into the internal list (defensive).
     * Use this from repositories when you want to hydrate an Employee with existing shifts.
     */
    public Employee(String name, String employeeId, String role, List<Shift> initialShifts) {
        this(name, employeeId, role);
        if (initialShifts != null && !initialShifts.isEmpty()) {
            this.scheduledShifts.addAll(initialShifts);
        }
    }

    // --- Accessors ---

    public String getName() { return name; }
    public String getEmployeeId() { return employeeId; }
    public String getRole() { return role; }

    /**
     * Returns an unmodifiable snapshot view of the scheduled shifts.
     */
    public List<Shift> getScheduledShifts() {
        return Collections.unmodifiableList(scheduledShifts);
    }

    // --- Mutators / domain behaviour ---

    /**
     * Replace the internal scheduledShifts content with the provided list.
     * Defensive copy semantics: internal list instance is retained.
     */
    public void setScheduledShifts(List<Shift> shifts) {
        this.scheduledShifts.clear();
        if (shifts != null && !shifts.isEmpty()) {
            this.scheduledShifts.addAll(shifts);
        }
    }

    /**
     * Add a shift after enforcing domain rules: no overlaps and weekly-hour limit.
     * Throws IllegalArgumentException on violation.
     */
    public void addShift(Shift shift) {
        Objects.requireNonNull(shift, "shift");
        // overlap check
        for (Shift s : scheduledShifts) {
            if (s.overlaps(shift)) {
                throw new IllegalArgumentException("Shift overlaps existing shift: " + s.getId());
            }
        }
        // weekly hours check (sum of hours)
        long currentHours = scheduledShifts.stream().mapToLong(s -> s.duration().toHours()).sum();
        long toAdd = shift.duration().toHours();
        if (currentHours + toAdd > WEEKLY_LIMIT_HOURS) {
            throw new IllegalArgumentException("Exceeds weekly hour limit");
        }
        scheduledShifts.add(shift);
    }

    /**
     * Remove the first shift that matches the provided id.
     */
    public void removeShiftById(String shiftId) {
        if (shiftId == null) return;
        Iterator<Shift> it = scheduledShifts.iterator();
        while (it.hasNext()) {
            if (shiftId.equals(it.next().getId())) {
                it.remove();
                return;
            }
        }
    }

    // equals/hashCode based on employeeId
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Employee)) return false;
        Employee employee = (Employee) o;
        return Objects.equals(employeeId, employee.employeeId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(employeeId);
    }

    @Override
    public String toString() {
        return "Employee{" +
                "name='" + name + '\'' +
                ", employeeId='" + employeeId + '\'' +
                ", role='" + role + '\'' +
                ", scheduledShifts=" + scheduledShifts.size() +
                '}';
    }
}
