package it.unifi.attsw.model;
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
