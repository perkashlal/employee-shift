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
 *
 * Note: domain validation failures are wrapped into RuntimeException so integration
 * tests and controllers receive a runtime error as expected by the test-suite.
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
     * IMPORTANT: any domain validation failure (e.g. overlap or weekly limit)
     * is converted to RuntimeException so test callers and controllers observe it.
     */
    public Employee addShiftToEmployee(String employeeId, Shift shift) {
        System.out.println("EmployeeService.addShiftToEmployee() for employeeId="
                + employeeId + " shiftId=" + (shift == null ? "null" : shift.getId()));

        if (employeeId == null || employeeId.isBlank()) {
            throw new IllegalArgumentException("employeeId required");
        }
        if (shift == null) {
            throw new IllegalArgumentException("shift required");
        }

        Optional<Employee> maybe = repo.findById(employeeId);

        Employee toSave;
        if (maybe.isPresent()) {
            // Existing employee: use Employee domain API (addShift) so domain rules are enforced
            Employee existing = maybe.get();
            try {
                existing.addShift(shift); // may throw IllegalArgumentException on domain violation
            } catch (Throwable t) {
                // Wrap domain validation failure to satisfy integration tests' expectation
                throw new RuntimeException("Cannot add shift to existing employee: " + t.getMessage(), t);
            }
            toSave = existing;
        } else {
            // Employee not present: create minimal Employee and use domain API to validate shift
            toSave = new Employee(employeeId, employeeId, "", new ArrayList<>());
            try {
                toSave.addShift(shift);
            } catch (Throwable t) {
                throw new RuntimeException("Cannot add shift to new employee: " + t.getMessage(), t);
            }
        }

        // Persist and return only if addShift succeeded
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
        try {
            existing.removeShiftById(shiftId);
        } catch (Throwable t) {
            throw new RuntimeException("Cannot remove shift: " + t.getMessage(), t);
        }

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
