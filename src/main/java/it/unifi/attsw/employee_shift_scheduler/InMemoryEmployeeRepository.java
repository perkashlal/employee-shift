package it.unifi.attsw.employee_shift_scheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final Map<String, Employee> store = new HashMap<>();

    @Override
    public Optional<Employee> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public Employee save(Employee e) {
        store.put(e.getEmployeeId(), e);
        return e;
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }

    // helper for tests
    public void clear() {
        store.clear();
    }
}
