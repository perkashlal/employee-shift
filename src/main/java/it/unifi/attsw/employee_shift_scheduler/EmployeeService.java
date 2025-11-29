package it.unifi.attsw.employee_shift_scheduler;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public void addShiftToEmployee(String employeeId, Shift shift) {
        Objects.requireNonNull(employeeId);
        Objects.requireNonNull(shift);
        Employee e = repository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        e.addShift(shift);
        repository.save(e);
    }

    public void removeShiftFromEmployee(String employeeId, String shiftId) {
        Objects.requireNonNull(employeeId);
        Objects.requireNonNull(shiftId);
        Employee e = repository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
        e.removeShiftById(shiftId);
        repository.save(e);
    }

    public Optional<Employee> findEmployee(String id) {
        return repository.findById(id);
    }

    public List<Employee> findAllEmployees() {
        return repository.findAll();
    }

    public Employee saveEmployee(Employee e) {
        return repository.save(e);
    }
}
