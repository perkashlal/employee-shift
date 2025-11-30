package it.unifi.attsw.employee_shift_scheduler;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Thin controller that exposes operations used by the UI.
 * Delegates to EmployeeService for business logic.
 */
public class Controller {

    private final EmployeeService employeeService;

    public Controller(EmployeeService employeeService) {
        this.employeeService = Objects.requireNonNull(employeeService, "employeeService");
    }

    /**
     * Add a shift to an employee.
     * Returns true if the operation succeeded (employee existed and shift added).
     */
    public boolean addShift(String employeeId, Shift shift) {
        Objects.requireNonNull(employeeId);
        Objects.requireNonNull(shift);
        Optional<Employee> opt = employeeService.findEmployee(employeeId);
        if (opt.isEmpty()) return false;
        employeeService.addShiftToEmployee(employeeId, shift);
        return true;
    }

    /**
     * Remove a shift from an employee. Returns true if employee existed (operation executed).
     */
    public boolean removeShift(String employeeId, String shiftId) {
        Objects.requireNonNull(employeeId);
        Objects.requireNonNull(shiftId);
        Optional<Employee> opt = employeeService.findEmployee(employeeId);
        if (opt.isEmpty()) return false;
        employeeService.removeShiftFromEmployee(employeeId, shiftId);
        return true;
    }

    /**
     * Get scheduled shifts for the employee. If not found, returns empty list.
     */
    public List<Shift> listShifts(String employeeId) {
        Objects.requireNonNull(employeeId);
        return employeeService.findEmployee(employeeId)
                .map(Employee::getScheduledShifts)
                .orElse(List.of());
    }

    /**
     * Create or update an employee.
     */
    public Employee saveEmployee(Employee employee) {
        return employeeService.saveEmployee(employee);
    }

    /**
     * Fetch an employee by id.
     */
    public Optional<Employee> findEmployee(String employeeId) {
        return employeeService.findEmployee(employeeId);
    }
}
