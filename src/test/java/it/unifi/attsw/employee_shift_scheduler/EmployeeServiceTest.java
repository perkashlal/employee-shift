package it.unifi.attsw.employee_shift_scheduler;

import it.unifi.attsw.employee_shift_scheduler.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*; // AssertJ
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests for EmployeeService.
 * Mocks EmployeeRepository and verifies add/remove/save behaviours.
 */
class EmployeeServiceTest {

    private EmployeeRepository repo;
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        repo = mock(EmployeeRepository.class);
        service = new EmployeeService(repo);
    }

    @Test
    void addShiftToEmployee_existingEmployee_addsAndSaves() {
        String empId = "E100";
        // build an employee with no shifts
        Employee existing = new Employee("Alice", empId, "role");
        when(repo.findById(empId)).thenReturn(Optional.of(existing));
        when(repo.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // create a shift (LocalDateTime constructor)
        LocalDateTime s = LocalDateTime.of(2025, 12, 1, 9, 0);
        LocalDateTime e = LocalDateTime.of(2025, 12, 1, 17, 0);
        Shift shift = new Shift(s, e);

        Employee saved = service.addShiftToEmployee(empId, shift);

        // verify repo.save called once with employee that now contains the shift
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(repo, times(1)).save(captor.capture());
        Employee persisted = captor.getValue();

        assertThat(persisted.getEmployeeId()).isEqualTo(empId);
        assertThat(persisted.getScheduledShifts()).hasSize(1);
        assertThat(persisted.getScheduledShifts().get(0).getId()).isEqualTo(shift.getId());

        // returned saved should be same instance (repo.save returns argument)
        assertThat(saved.getScheduledShifts()).hasSize(1);
    }

    @Test
    void addShiftToEmployee_nonExisting_createsEmployeeAndSaves() {
        String empId = "E200";
        when(repo.findById(empId)).thenReturn(Optional.empty());
        when(repo.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LocalDateTime s = LocalDateTime.of(2025, 11, 30, 8, 0);
        LocalDateTime e = LocalDateTime.of(2025, 11, 30, 16, 0);
        Shift shift = new Shift(s, e);

        Employee saved = service.addShiftToEmployee(empId, shift);

        // verify saved employee has the shift and employeeId set correctly
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(repo, times(1)).save(captor.capture());
        Employee persisted = captor.getValue();

        assertThat(persisted.getEmployeeId()).isEqualTo(empId);
        assertThat(persisted.getScheduledShifts()).hasSize(1);
        assertThat(persisted.getScheduledShifts().get(0).getId()).isEqualTo(shift.getId());

        assertThat(saved.getEmployeeId()).isEqualTo(empId);
        assertThat(saved.getScheduledShifts()).hasSize(1);
    }

    @Test
    void removeShiftFromEmployee_existing_removesAndSaves() {
        String empId = "E300";
        // create employee with two shifts
        LocalDateTime s1 = LocalDateTime.of(2025, 12, 2, 9, 0);
        LocalDateTime e1 = LocalDateTime.of(2025, 12, 2, 13, 0);
        Shift sh1 = new Shift(s1, e1);

        LocalDateTime s2 = LocalDateTime.of(2025, 12, 2, 14, 0);
        LocalDateTime e2 = LocalDateTime.of(2025, 12, 2, 18, 0);
        Shift sh2 = new Shift(s2, e2);

        List<Shift> initial = new ArrayList<>();
        initial.add(sh1);
        initial.add(sh2);

        Employee existing = new Employee("Bob", empId, "role", initial);

        when(repo.findById(empId)).thenReturn(Optional.of(existing));
        when(repo.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // remove the first shift
        Employee after = service.removeShiftFromEmployee(empId, sh1.getId());

        // verify repo.save called and persisted has only one shift (sh2)
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(repo, times(1)).save(captor.capture());
        Employee persisted = captor.getValue();

        assertThat(persisted.getScheduledShifts()).hasSize(1);
        assertThat(persisted.getScheduledShifts().get(0).getId()).isEqualTo(sh2.getId());

        assertThat(after.getScheduledShifts()).hasSize(1);
    }

    @Test
    void removeShiftFromEmployee_employeeNotFound_throws() {
        String empId = "E404";
        when(repo.findById(empId)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.removeShiftFromEmployee(empId, "any-id"));

        // repo.save should not be called
        verify(repo, never()).save(any());
    }

    @Test
    void deleteById_whenPresent_deletesAndReturnsOptional() {
        String empId = "E500";
        Employee existing = new Employee("Cathy", empId, "role");
        when(repo.findById(empId)).thenReturn(Optional.of(existing));

        Optional<Employee> removed = service.deleteById(empId);

        assertThat(removed).isPresent().contains(existing);
        verify(repo, times(1)).deleteById(empId);
    }

    @Test
    void deleteById_whenNotPresent_returnsEmptyOptional_andDoesNotDelete() {
        String empId = "ENULL";
        when(repo.findById(empId)).thenReturn(Optional.empty());

        Optional<Employee> removed = service.deleteById(empId);

        assertThat(removed).isEmpty();
        verify(repo, never()).deleteById(anyString());
    }
}
