package it.unifi.attsw.employee_shift_scheduler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {

    private EmployeeService service;
    private Controller controller;

    @BeforeEach
    void setUp() {
        service = mock(EmployeeService.class);
        controller = new Controller(service);
    }

    @Test
    void addShift_shouldReturnFalseWhenEmployeeMissing() {
        when(service.findEmployee("E1")).thenReturn(Optional.empty());

        Shift s = new Shift(LocalDateTime.of(2025,11,30,8,0),
                LocalDateTime.of(2025,11,30,12,0));
        boolean result = controller.addShift("E1", s);

        assertFalse(result);
        verify(service, never()).addShiftToEmployee(anyString(), any());
    }

    @Test
    void addShift_shouldDelegateAndReturnTrueWhenEmployeeExists() {
        Employee e = new Employee("Bob", "E2", "cashier");
        when(service.findEmployee("E2")).thenReturn(Optional.of(e));

        Shift s = new Shift(LocalDateTime.of(2025,11,30,8,0),
                LocalDateTime.of(2025,11,30,12,0));
        boolean result = controller.addShift("E2", s);

        assertTrue(result);
        verify(service).addShiftToEmployee("E2", s);
    }

    @Test
    void removeShift_shouldReturnFalseWhenEmployeeMissing() {
        when(service.findEmployee("X")).thenReturn(Optional.empty());

        boolean res = controller.removeShift("X", "shift-1");
        assertFalse(res);
        verify(service, never()).removeShiftFromEmployee(anyString(), anyString());
    }

    @Test
    void removeShift_shouldDelegateWhenEmployeeExists() {
        Employee e = new Employee("Anna", "A1", "mgr");
        when(service.findEmployee("A1")).thenReturn(Optional.of(e));

        boolean res = controller.removeShift("A1", "s1");
        assertTrue(res);
        verify(service).removeShiftFromEmployee("A1", "s1");
    }

    @Test
    void listShifts_returnsShiftsOrEmpty() {
        Employee e = new Employee("Sam","S1","staff");
        Shift s = new Shift(LocalDateTime.of(2025,11,30,8,0),
                LocalDateTime.of(2025,11,30,12,0));
        e.addShift(s);

        when(service.findEmployee("S1")).thenReturn(Optional.of(e));
        assertEquals(1, controller.listShifts("S1").size());

        when(service.findEmployee("missing")).thenReturn(Optional.empty());
        assertEquals(List.of(), controller.listShifts("missing"));
    }

    @Test
    void saveAndFindEmployee_delegates() {
        Employee e = new Employee("Sam","S2","staff");
        when(service.saveEmployee(e)).thenReturn(e);
        when(service.findEmployee("S2")).thenReturn(Optional.of(e));

        Employee saved = controller.saveEmployee(e);
        assertEquals(e, saved);
        Optional<Employee> fetched = controller.findEmployee("S2");
        assertTrue(fetched.isPresent());
        assertEquals(e, fetched.get());

        verify(service).saveEmployee(e);
        verify(service).findEmployee("S2");
    }
}
