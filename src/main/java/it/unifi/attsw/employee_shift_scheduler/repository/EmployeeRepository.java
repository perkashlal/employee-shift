package it.unifi.attsw.employee_shift_scheduler.repository;

import it.unifi.attsw.employee_shift_scheduler.Employee;
import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {
    /**
     * Persist the given employee and return the persisted instance (may be the same object).
     */
    Employee save(Employee employee);

    /**
     * Find an employee by id.
     */
    Optional<Employee> findById(String id);

    /**
     * List all employees.
     */
    List<Employee> findAll();

    /**
     * Delete employee by id. Implementations may throw if error occurs.
     */
    void deleteById(String id);
}
