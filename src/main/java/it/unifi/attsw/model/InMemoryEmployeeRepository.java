package it.unifi.attsw.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryEmployeeRepository implements EmployeeRepository {

    private final Map<String, Employee> storage = new HashMap<>();

    @Override
    public void save(Employee e) {
        storage.put(e.getEmployeeId(), e);
    }

    @Override
    public Employee findById(String id) {
        return storage.get(id);
    }

    @Override
    public List<Employee> findAll() {
        return new ArrayList<>(storage.values());
    }
}
