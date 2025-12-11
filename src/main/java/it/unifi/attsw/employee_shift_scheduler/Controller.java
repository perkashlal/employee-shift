package it.unifi.attsw.employee_shift_scheduler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Backwards-compatible controller used by GUI, unit-tests and integration-tests.
 *
 * - GUI / unit tests use the GUI-friendly methods that swallow exceptions and call view.showError(...)
 * - Integration tests can call the "...OrThrow" variants which notify the view then rethrow the exception
 *   so the test harness can observe failures.
 */
public class Controller {

    private final EmployeeService employeeService;
    private final EmployeeView view;

    /**
     * Primary constructor used in production/tests when a view is available.
     */
    public Controller(EmployeeService employeeService, EmployeeView view) {
        if (employeeService == null) throw new IllegalArgumentException("employeeService required");
        if (view == null) throw new IllegalArgumentException("view required");
        this.employeeService = employeeService;
        this.view = view;
    }

    /**
     * Convenience constructor used by many tests and legacy code: supplies a NoOp view.
     * This is why ControllerIT and other tests can call new Controller(service).
     */
    public Controller(EmployeeService employeeService) {
        this(employeeService, new NoOpEmployeeView());
    }

    /* ---------------- canonical API ---------------- */

    public List<Employee> allEmployees() {
        List<Employee> all = employeeService.findAll();
        view.showAllEmployees(all);
        return all;
    }

    /**
     * New / canonical API: create employee. GUI-friendly: swallow runtime exceptions and show error on view.
     */
    public void newEmployee(Employee employee) {
        try {
            employeeService.save(employee);
            view.employeeAdded(employee);
        } catch (RuntimeException | Error ex) {
            view.showError("Cannot add employee: " + ex.getMessage());
        }
    }

    /**
     * GUI-friendly: add shift and notify view; swallow exceptions (do not rethrow).
     * ControllerTest expects this to catch service exceptions and call view.showError(...)
     */
    public void addShiftToEmployee(String employeeId, Shift shift) {
        try {
            Employee updated = employeeService.addShiftToEmployee(employeeId, shift);
            view.shiftAddedToEmployee(updated, shift);
        } catch (RuntimeException | Error ex) {
            view.showError("Cannot add shift: " + ex.getMessage());
            // GUI variant - swallow the exception so UI remains responsive.
        }
    }

    /**
     * Programmatic / integration-test variant: notify view then rethrow the exception so the caller can assert it.
     * Integration tests should call this method when they expect an exception to propagate.
     */
    public Employee addShiftToEmployeeOrThrow(String employeeId, Shift shift) {
        try {
            Employee updated = employeeService.addShiftToEmployee(employeeId, shift);
            view.shiftAddedToEmployee(updated, shift);
            return updated;
        } catch (RuntimeException | Error ex) {
            view.showError("Cannot add shift: " + ex.getMessage());
            throw ex; // rethrow for programmatic callers / integration tests
        }
    }

    public List<Shift> listShiftsForEmployee(String employeeId) {
        return employeeService.findById(employeeId)
                .map(emp -> {
                    try {
                        return emp.getScheduledShifts();
                    } catch (Throwable t) {
                        return Collections.<Shift>emptyList();
                    }
                })
                .orElse(Collections.emptyList());
    }

    public Optional<Employee> deleteById(String employeeId) {
        return employeeService.deleteById(employeeId);
    }

    /**
     * Remove shift and return the updated employee (programmatic helper).
     */
    public Employee removeShiftFromEmployeeAndReturn(String employeeId, String shiftId) {
        return employeeService.removeShiftFromEmployee(employeeId, shiftId);
    }

    /* ---------------- legacy / compatibility methods expected by GUI/tests ---------------- */

    public boolean saveEmployee(Employee employee) {
        try {
            employeeService.save(employee);
            return true;
        } catch (RuntimeException | Error ex) {
            view.showError("Cannot save employee: " + ex.getMessage());
            return false;
        }
    }

    public List<Employee> findAllEmployees() {
        return allEmployees();
    }

    /**
     * Legacy GUI-friendly wrapper for addShiftToEmployee.
     */
    public boolean addShift(String employeeId, Shift shift) {
        try {
            addShiftToEmployee(employeeId, shift);
            return true;
        } catch (RuntimeException | Error ex) {
            // addShiftToEmployee already swallows, but keep defensive handling
            view.showError("Cannot add shift: " + ex.getMessage());
            return false;
        }
    }

    public List<Shift> listShifts(String employeeId) {
        return listShiftsForEmployee(employeeId);
    }

    /**
     * Legacy deleteEmployee(String) used by GUI/tests: void method that notifies view.
     */
    public void deleteEmployee(String employeeId) {
        try {
            Optional<Employee> removed = employeeService.deleteById(employeeId);
            if (removed.isPresent()) {
                view.employeeRemoved(removed.get());
            } else {
                view.showError("Employee not found: " + employeeId);
            }
        } catch (RuntimeException | Error ex) {
            view.showError("Error removing employee: " + ex.getMessage());
        }
    }

    /**
     * Legacy remove-shift method used by GUI/tests. Swallows exceptions and notifies view.
     */
    public void removeShiftFromEmployee(String employeeId, String shiftId) {
        try {
            Employee updated = employeeService.removeShiftFromEmployee(employeeId, shiftId);
            view.shiftRemovedFromEmployee(updated, shiftId);
        } catch (RuntimeException | Error ex) {
            view.showError("Cannot remove shift: " + ex.getMessage());
        }
    }

    public boolean removeEmployee(String employeeId) {
        try {
            return employeeService.removeEmployee(employeeId);
        } catch (RuntimeException | Error ex) {
            view.showError("Cannot remove employee: " + ex.getMessage());
            return false;
        }
    }

    /* --------- View interface & no-op implementation --------- */

    public interface EmployeeView {
        void showAllEmployees(List<Employee> employees);
        void employeeAdded(Employee employee);
        void employeeRemoved(Employee employee);
        void showError(String message);
        void shiftAddedToEmployee(Employee employee, Shift shift);
        void shiftRemovedFromEmployee(Employee employee, String shiftId);
    }

    /**
     * Default no-op view used when tests/legacy code don't need a real GUI.
     */
    private static class NoOpEmployeeView implements EmployeeView {
        @Override public void showAllEmployees(List<Employee> employees) {}
        @Override public void employeeAdded(Employee employee) {}
        @Override public void employeeRemoved(Employee employee) {}
        @Override public void showError(String message) {}
        @Override public void shiftAddedToEmployee(Employee employee, Shift shift) {}
        @Override public void shiftRemovedFromEmployee(Employee employee, String shiftId) {}
    }
}
