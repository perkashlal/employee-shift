package it.unifi.attsw.model;

import java.util.List;

public interface EmployeeRepository {
    void save(Employee e);

    Employee findById(String id);

    List<Employee> findAll();
}
