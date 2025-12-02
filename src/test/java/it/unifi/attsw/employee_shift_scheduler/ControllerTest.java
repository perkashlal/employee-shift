package it.unifi.attsw.employee_shift_scheduler;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Controller using Mockito.
 */
class ControllerTest {

    @Mock
    private EmployeeService service;

    @Mock
    private Controller.EmployeeView view;

    @InjectMocks
    private Controller controller;

    private AutoCloseable mocks;

    @BeforeEach
    void setUp() {
        mocks = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        mocks.close();
    }

    @Test
    void allEmployees_delegatesAndUpdatesView() {
        Employee e1 = new Employee("Alice", "E1", "cashier");
        Employee e2 = new Employee("Bob", "E2", "manager");
        when(service.findAll()).thenReturn(List.of(e1, e2));

        List<Employee> result = controller.allEmployees();

        verify(service).findAll();
        verify(view).showAllEmployees(List.of(e1, e2));
        assertEquals(2, result.size());
    }

    @Test
    void newEmployee_success_callsServiceAndView() {
        Employee e = new Employee("Alice", "E1", "cashier");
        when(service.save(e)).thenReturn(e);

        controller.newEmployee(e);

        verify(service).save(e);
        verify(view).employeeAdded(e);
        verify(view, never()).showError(anyString());
    }

    @Test
    void newEmployee_serviceThrows_showsError() {
        Employee e = new Employee("Alice", "E1", "cashier");
        when(service.save(e)).thenThrow(new RuntimeException("DB down"));

        controller.newEmployee(e);

        verify(service).save(e);
        verify(view).showError(contains("DB down"));
        verify(view, never()).employeeAdded(any());
    }

    @Test
    void deleteEmployee_existing_callsServiceAndView() {
        Employee e = new Employee("Alice", "E1", "cashier");
        when(service.deleteById("E1")).thenReturn(Optional.of(e));

        controller.deleteEmployee("E1");

        verify(service).deleteById("E1");
        verify(view).employeeRemoved(e);
        verify(view, never()).showError(anyString());
    }

    @Test
    void deleteEmployee_notFound_showsError() {
        when(service.deleteById("not-found")).thenReturn(Optional.empty());

        controller.deleteEmployee("not-found");

        verify(service).deleteById("not-found");
        verify(view).showError(contains("not found"));
        verify(view, never()).employeeRemoved(any());
    }

    @Test
    void addShiftToEmployee_success_delegatesAndNotifiesView() {
        Shift s = new Shift("S1",
                LocalDateTime.of(2025,1,10,9,0),
                LocalDateTime.of(2025,1,10,13,0),
                "morning");
        Employee e = new Employee("Alice", "E1", "cashier");
        when(service.addShiftToEmployee("E1", s)).thenReturn(e);

        controller.addShiftToEmployee("E1", s);

        verify(service).addShiftToEmployee("E1", s);
        verify(view).shiftAddedToEmployee(e, s);
        verify(view, never()).showError(anyString());
    }

    @Test
    void addShiftToEmployee_serviceThrows_showsError() {
        Shift s = new Shift("S1",
                LocalDateTime.of(2025,1,10,9,0),
                LocalDateTime.of(2025,1,10,13,0),
                "morning");
        when(service.addShiftToEmployee("E1", s)).thenThrow(new IllegalArgumentException("overlap"));

        controller.addShiftToEmployee("E1", s);

        verify(service).addShiftToEmployee("E1", s);
        verify(view).showError(contains("overlap"));
        verify(view, never()).shiftAddedToEmployee(any(), any());
    }

    @Test
    void removeShiftFromEmployee_success_delegatesAndNotifiesView() {
        Employee e = new Employee("Alice", "E1", "cashier");
        when(service.removeShiftFromEmployee("E1", "S1")).thenReturn(e);

        controller.removeShiftFromEmployee("E1", "S1");

        verify(service).removeShiftFromEmployee("E1", "S1");
        verify(view).shiftRemovedFromEmployee(e, "S1");
        verify(view, never()).showError(anyString());
    }

    @Test
    void removeShiftFromEmployee_serviceThrows_showsError() {
        when(service.removeShiftFromEmployee("E1", "S1")).thenThrow(new IllegalArgumentException("not found"));

        controller.removeShiftFromEmployee("E1", "S1");

        verify(service).removeShiftFromEmployee("E1", "S1");
        verify(view).showError(contains("not found"));
        verify(view, never()).shiftRemovedFromEmployee(any(), anyString());
    }
}
