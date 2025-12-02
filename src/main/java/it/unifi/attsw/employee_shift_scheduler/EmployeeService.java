package it.unifi.attsw.employee_shift_scheduler;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service layer for Employee operations.
 */
public class EmployeeService {

    private final EmployeeRepository repository;

    public EmployeeService(EmployeeRepository repository) {
        this.repository = repository;
    }

    /* ---------------- Canonical API ---------------- */

    public Employee save(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("employee cannot be null");
        }
        return repository.save(employee);
    }

    public List<Employee> findAll() {
        return repository.findAll();
    }

    public Optional<Employee> findById(String id) {
        return repository.findById(id);
    }

    public Optional<Employee> deleteById(String id) {
        Optional<Employee> found = repository.findById(id);
        if (found.isPresent()) {
            repository.deleteById(id);
        }
        return found;
    }

    public Employee addShiftToEmployee(String employeeId, Shift shift) {
        Employee employee = repository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));
        employee.addShift(shift);
        return repository.save(employee);
    }

    /**
     * Robust removal using domain method if present, otherwise defensive copy + reflective setter fallback.
     */
    public Employee removeShiftFromEmployee(String employeeId, String shiftId) {
        Employee employee = repository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        // 1) Preferred: try domain remover if available (void)
        try {
            employee.removeShiftById(shiftId);
            return repository.save(employee);
        } catch (NoSuchMethodError | AbstractMethodError nsme) {
            // fall back
        } catch (RuntimeException ex) {
            if (ex instanceof IllegalArgumentException) {
                throw ex;
            }
            // otherwise fall through to fallback
        } catch (Throwable t) {
            // fall back
        }

        // 2) Fallback: defensive copy + remove by id + setter via reflection
        List<Shift> current = employee.getScheduledShifts(); // adjust getter name if different in your domain
        List<Shift> mutable = new ArrayList<>(current);

        boolean removed = mutable.removeIf(s -> {
            String sid = s.getId(); // adjust to s.getShiftId() if needed
            return shiftId != null && shiftId.equals(sid);
        });

        if (!removed) {
            throw new IllegalArgumentException("Shift not found: " + shiftId);
        }

        // try to set updated list back using reflection
        try {
            Method setter = employee.getClass().getMethod("setScheduledShifts", List.class);
            setter.invoke(employee, mutable);
            return repository.save(employee);
        } catch (NoSuchMethodException nsme) {
            throw new IllegalStateException("Employee does not expose setScheduledShifts(List<Shift>) and domain remover failed", nsme);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to update employee shifts via setter", e);
        }
    }

    /* ---------------- Legacy wrappers ---------------- */

    public boolean saveEmployee(Employee employee) {
        save(employee);
        return true;
    }

    public boolean findEmployee(String employeeId) {
        return repository.findById(employeeId).isPresent();
    }

    public List<Employee> findAllEmployees() {
        return findAll();
    }

    public boolean removeEmployee(String employeeId) {
        Optional<Employee> found = repository.findById(employeeId);
        if (found.isPresent()) {
            repository.deleteById(employeeId);
            return true;
        }
        return false;
    }
}
