package it.unifi.attsw.employee_shift_scheduler;

import it.unifi.attsw.employee_shift_scheduler.repository.EmployeeRepository;

import java.util.*;

/**
 * Simple in-memory repository for fallback or testing use.
 */
public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final Map<String, Employee> store = new LinkedHashMap<>();

    @Override
    public Employee save(Employee employee) {
        store.put(employee.getEmployeeId(), employee);
        return employee;
    }

    @Override
    public Optional<Employee> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
