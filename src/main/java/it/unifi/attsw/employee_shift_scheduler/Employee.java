package it.unifi.attsw.employee_shift_scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

/**
 * Minimal Employee with shift management.
 */
public class Employee {

    private final String name;
    private final String employeeId;
    private final String role;
    private final List<Shift> scheduledShifts = new ArrayList<>();

    private static final long WEEKLY_LIMIT_HOURS = 48; // simple default

    public Employee(String name, String employeeId, String role) {
        this.name = Objects.requireNonNull(name, "name");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId");
        this.role = role == null ? "" : role;
    }

    public String getName() { return name; }
    public String getEmployeeId() { return employeeId; }
    public String getRole() { return role; }

    public List<Shift> getScheduledShifts() {
        return Collections.unmodifiableList(scheduledShifts);
    }

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
}
