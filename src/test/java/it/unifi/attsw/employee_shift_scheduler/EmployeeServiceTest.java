package it.unifi.attsw.employee_shift_scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {

    private EmployeeRepository repo;
    private EmployeeService service;

    @BeforeEach
    void setUp() {
        repo = mock(EmployeeRepository.class);
        service = new EmployeeService(repo);
    }

    @Test
    void addShiftToEmployee_shouldFetchEmployee_addShiftAndSave() {
        // arrange
        String empId = "emp-1";
        Employee employee = new Employee("John Doe", empId, "cashier");
        when(repo.findById(empId)).thenReturn(Optional.of(employee));

        Shift shift = new Shift(
                "shift-1",
                LocalDateTime.of(2025, 11, 30, 8, 0),
                LocalDateTime.of(2025, 11, 30, 12, 0),
                ""
        );

        // act
        service.addShiftToEmployee(empId, shift);

        // assert
        verify(repo).save(employee);
        assertEquals(1, employee.getScheduledShifts().size());
    }

    @Test
    void removeShiftFromEmployee_shouldRemoveAndSave() {
        // arrange
        String empId = "emp-2";
        Employee employee = new Employee("Jane", empId, "manager");

        Shift shift = new Shift(
                "s-10",
                LocalDateTime.of(2025, 11, 30, 9, 0),
                LocalDateTime.of(2025, 11, 30, 11, 0),
                ""
        );

        employee.addShift(shift);
        when(repo.findById(empId)).thenReturn(Optional.of(employee));

        // act
        service.removeShiftFromEmployee(empId, "s-10");

        // assert
        verify(repo).save(employee);
        assertEquals(0, employee.getScheduledShifts().size());
    }
}
