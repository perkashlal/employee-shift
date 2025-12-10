// src/main/java/it/unifi/attsw/employee_shift_scheduler/EmployeeService.java
package it.unifi.attsw.employee_shift_scheduler;

import it.unifi.attsw.employee_shift_scheduler.repository.EmployeeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for employees and shifts.
 *
 * - addShiftToEmployee will create a new Employee if none exists for the given id.
 * - Uses Employee API (addShift/removeShiftById) so domain rules are enforced.
 */
public class EmployeeService {

    private final EmployeeRepository repo;

    public EmployeeService(EmployeeRepository repo) {
        this.repo = repo;
    }

    public Employee save(Employee e) {
        return repo.save(e);
    }

    public Optional<Employee> findById(String id) {
        return repo.findById(id);
    }

    public List<Employee> findAll() {
        return repo.findAll();
    }

    public Optional<Employee> deleteById(String id) {
        Optional<Employee> removed = repo.findById(id);
        if (removed.isPresent()) {
            repo.deleteById(id);
        }
        return removed;
    }

    /**
     * Add a shift to the employee. If the employee does not exist, create a new Employee
     * with the given id and persist it (with the shift added).
     *
     * Returns the saved/updated Employee object.
     */
    public Employee addShiftToEmployee(String employeeId, Shift shift) {
        System.out.println("EmployeeService.addShiftToEmployee() for employeeId=" + employeeId + " shiftId=" + (shift == null ? "null" : shift.getId()));

        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("employeeId required");
        }
        if (shift == null) {
            throw new IllegalArgumentException("shift required");
        }

        Optional<Employee> maybe = repo.findById(employeeId);

        Employee toSave;
        if (maybe.isPresent()) {
            // Use the Employee API to add the shift (enforces overlap/weekly checks)
            Employee existing = maybe.get();
            existing.addShift(shift); // may throw IllegalArgumentException on domain violation
            toSave = existing;
        } else {
            // Employee not found -> create a new one with a minimal payload (name = id, role empty)
            List<Shift> shifts = new ArrayList<>();
            shifts.add(shift);
            toSave = new Employee(employeeId, employeeId, "", shifts);
        }

        // Persist and return
        Employee saved = repo.save(toSave);
        return saved;
    }

    /**
     * Remove a shift by id from the specified employee.
     * Returns the updated Employee (throws if employee not found).
     */
    public Employee removeShiftFromEmployee(String employeeId, String shiftId) {
        if (employeeId == null || employeeId.isBlank()) throw new IllegalArgumentException("employeeId required");
        if (shiftId == null || shiftId.isBlank()) throw new IllegalArgumentException("shiftId required");

        Optional<Employee> maybe = repo.findById(employeeId);
        if (maybe.isEmpty()) {
            throw new IllegalArgumentException("Employee not found: " + employeeId);
        }
        Employee existing = maybe.get();

        // Use employee API to remove shift
        existing.removeShiftById(shiftId);

        // Persist and return
        Employee saved = repo.save(existing);
        return saved;
    }

    /**
     * Remove employee (alias)
     */
    public boolean removeEmployee(String employeeId) {
        Optional<Employee> removed = deleteById(employeeId);
        return removed.isPresent();
    }
}
