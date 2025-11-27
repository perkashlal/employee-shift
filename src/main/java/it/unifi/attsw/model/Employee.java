package it.unifi.attsw.model;
<<<<<<< HEAD

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
=======
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;

@Document(collection = "employees")
public class employee {
    @Id
    private String employeeId;
    private String name;
    private String role;
    private List<Shift> scheduledShifts = new ArrayList<>();

    public employee() { }

    public employee(String employeeId, String name, String role) {
        this.employeeId = employeeId;
        this.name = name;
        this.role = role;
    }

    public String getEmployeeId() { return employeeId; }
    public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public List<Shift> getScheduledShifts() { return scheduledShifts; }
    public void setScheduledShifts(List<Shift> scheduledShifts) { this.scheduledShifts = scheduledShifts; }

    public void addShift(Shift s) { this.scheduledShifts.add(s); }

    public void removeShiftById(String shiftId) {
        this.scheduledShifts.removeIf(s -> s.getShiftId() != null && s.getShiftId().equals(shiftId));
    }
}
>>>>>>> 00aa7c207f45934b3f1904e25ba86a45cfdda36f
