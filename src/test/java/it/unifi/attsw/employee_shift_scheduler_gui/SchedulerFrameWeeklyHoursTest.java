package it.unifi.attsw.employee_shift_scheduler_gui;

import it.unifi.attsw.employee_shift_scheduler.Controller;
import it.unifi.attsw.employee_shift_scheduler.Shift;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Verify weekly hours label updates when shifts are added / reloaded.
 */
class SchedulerFrameWeeklyHoursTest {

    private FrameFixture window;
    private Robot robot;
    private Controller controller;

    @BeforeEach
    void setUp() {
        controller = mock(Controller.class);
        robot = BasicRobot.robotWithNewAwtHierarchy();
        SchedulerFrame frame = GuiActionRunner.execute(() -> new SchedulerFrame(controller));
        window = new FrameFixture(robot, frame);
        window.show();
    }

    @AfterEach
    void tearDown() {
        if (window != null) window.cleanUp();
        if (robot != null) robot.cleanUp();
    }

    @Test
    void addingShifts_updatesWeeklyHoursLabel() {
        String empId = "E500";

        // Create two shifts: 8 hours and 4 hours (total 12.0)
        LocalDateTime s1 = LocalDateTime.of(2025, 12, 1, 8, 0);
        LocalDateTime e1 = LocalDateTime.of(2025, 12, 1, 16, 0); // 8h
        Shift shift1 = new Shift(s1, e1);

        LocalDateTime s2 = LocalDateTime.of(2025, 12, 2, 8, 0);
        LocalDateTime e2 = LocalDateTime.of(2025, 12, 2, 12, 0); // 4h
        Shift shift2 = new Shift(s2, e2);

        // Mock controller behavior:
        // - first addShift returns true
        // - listShifts returns the list with both shifts (simulate UI reloading to the final state)
        when(controller.addShift(eq(empId), any())).thenReturn(true);
        when(controller.listShifts(empId)).thenReturn(List.of(shift1, shift2));

        // Type employee id and fill fields to add first shift
        window.textBox("employeeIdField").enterText(empId);
        window.textBox("startField").enterText(s1.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        window.textBox("endField").enterText(e1.format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Click add (this triggers controller.addShift and then controller.listShifts which returns both shifts)
        window.button("addShiftButton").requireEnabled();
        window.button("addShiftButton").click();

        // The table should show 2 rows (as the mock listShifts returned both shifts)
        window.table("shiftsTable").requireRowCount(2);

        // Check weekly hours label text (formats to one decimal place)
        window.label("weeklyHoursLabel").requireText("Total Weekly Hours: 12.0h");

        // Also verify controller interactions
        verify(controller, atLeastOnce()).addShift(eq(empId), any());
        verify(controller, atLeastOnce()).listShifts(eq(empId));
    }
}
