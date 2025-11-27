package it.unifi.attsw.model;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final Map<String, Employee> store = new ConcurrentHashMap<>();

    @Override
    public Employee save(Employee employee) {
        Objects.requireNonNull(employee, "employee");
        store.put(employee.getEmployeeId(), employee);
        return employee;
    }

    @Override
    public Optional<Employee> findById(String employeeId) {
        return Optional.ofNullable(store.get(employeeId));
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public boolean deleteById(String employeeId) {
        return store.remove(employeeId) != null;
    }

    @Override
    public void addShiftToEmployee(String employeeId, Shift shift) {
        Objects.requireNonNull(shift, "shift");
        Employee emp = store.get(employeeId);
        if (emp == null) throw new IllegalArgumentException("Employee not found: " + employeeId);
        emp.addShift(shift); // will throw if overlap
    }

    @Override
    public boolean removeShiftFromEmployee(String employeeId, String shiftId) {
        Employee emp = store.get(employeeId);
        if (emp == null) return false;
        return emp.removeShiftById(shiftId);
    }
}