package it.unifi.attsw.model;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository {

    Employee save(Employee employee);

    Optional<Employee> findById(String employeeId);

    List<Employee> findAll();

    boolean deleteById(String employeeId);

    /**
     * Add a shift to the employee with given id; throws IllegalArgumentException
     * if employee not found or shift overlaps existing employee shifts.
     */
    void addShiftToEmployee(String employeeId, Shift shift);

    /**
     * Remove shift by id from specified employee; returns true if removed.
     */
    boolean removeShiftFromEmployee(String employeeId, String shiftId);
}