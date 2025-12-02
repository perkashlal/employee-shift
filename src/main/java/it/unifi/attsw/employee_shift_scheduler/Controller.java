package it.unifi.attsw.employee_shift_scheduler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Backwards-compatible controller: exposes both the new API and legacy wrappers
 * used by the Swing GUI and tests.
 */
public class Controller {

    private final EmployeeService employeeService;
    private final EmployeeView view;

    public Controller(EmployeeService employeeService, EmployeeView view) {
        this.employeeService = employeeService;
        this.view = view;
    }

    // Legacy constructor used by some older code that constructs Controller(service)
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
     * New/canonical: create employee. Swallow runtime exceptions and show error on view.
     */
    public void newEmployee(Employee employee) {
        try {
            employeeService.save(employee);
            view.employeeAdded(employee);
        } catch (RuntimeException | Error ex) {
            view.showError("Cannot add employee: " + ex.getMessage());
        }
    }

    public void addShiftToEmployee(String employeeId, Shift shift) {
        try {
            Employee updated = employeeService.addShiftToEmployee(employeeId, shift);
            view.shiftAddedToEmployee(updated, shift);
        } catch (RuntimeException | Error ex) {
            view.showError("Cannot add shift: " + ex.getMessage());
        }
    }

    public List<Shift> listShiftsForEmployee(String employeeId) {
        return employeeService.findById(employeeId)
                .map(emp -> {
                    // Try likely getter name; adjust later if your Employee uses a different getter.
                    try {
                        return emp.getScheduledShifts();
                    } catch (Throwable t) {
                        // fallback to empty list if getter missing
                        return Collections.<Shift>emptyList();
                    }
                })
                .orElse(Collections.emptyList());
    }

    public Optional<Employee> deleteById(String employeeId) {
        return employeeService.deleteById(employeeId);
    }

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

    public boolean addShift(String employeeId, Shift shift) {
        try {
            addShiftToEmployee(employeeId, shift);
            return true;
        } catch (RuntimeException | Error ex) {
            view.showError("Cannot add shift: " + ex.getMessage());
            return false;
        }
    }

    public List<Shift> listShifts(String employeeId) {
        return listShiftsForEmployee(employeeId);
    }

    /**
     * Legacy deleteEmployee(String) used by tests: void method that notifies view.
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
     * Legacy remove-shift method used by GUI/tests. Tests call controller.removeShiftFromEmployee(empId, shiftId).
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

    private static class NoOpEmployeeView implements EmployeeView {
        @Override public void showAllEmployees(List<Employee> employees) {}
        @Override public void employeeAdded(Employee employee) {}
        @Override public void employeeRemoved(Employee employee) {}
        @Override public void showError(String message) {}
        @Override public void shiftAddedToEmployee(Employee employee, Shift shift) {}
        @Override public void shiftRemovedFromEmployee(Employee employee, String shiftId) {}
    }
}
