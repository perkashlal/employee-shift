package it.unifi.attsw.employee_shift_scheduler;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    Optional<Employee> findById(String id);
    Employee save(Employee e);
    List<Employee> findAll();
    void deleteById(String id);
}
