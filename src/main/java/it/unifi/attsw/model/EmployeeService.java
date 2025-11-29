package it.unifi.attsw.model;

import java.util.Objects;

public class EmployeeService {

    private final EmployeeRepository repo;

    public EmployeeService(EmployeeRepository repo) {
        this.repo = Objects.requireNonNull(repo);
    }

    public void addShiftToEmployee(String employeeId, Shift shift) {
        Employee e = repo.findById(employeeId);
        if (e == null) {
            throw new IllegalArgumentException("Employee not found: " + employeeId);
        }
        e.addShift(shift); // business rules in Employee (overlap, weekly limit)
        repo.save(e);
    }
}
