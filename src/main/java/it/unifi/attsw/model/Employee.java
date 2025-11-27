package it.unifi.attsw.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Employee {

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
        for (Shift s : scheduledShifts) {
            if (s.overlaps(shift)) {
                throw new IllegalArgumentException("New shift overlaps existing shift: " + s);
            }
        }
        scheduledShifts.add(shift);
    }

    public boolean removeShiftById(String shiftId) {
        return scheduledShifts.removeIf(s -> s.getId().equals(shiftId));
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