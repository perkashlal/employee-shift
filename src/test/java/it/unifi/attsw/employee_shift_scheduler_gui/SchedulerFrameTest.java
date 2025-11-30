package it.unifi.attsw.employee_shift_scheduler_gui;

import it.unifi.attsw.employee_shift_scheduler.Controller;
import it.unifi.attsw.employee_shift_scheduler.Shift;

import org.assertj.swing.core.BasicRobot;
import org.assertj.swing.core.Robot;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Failing-first tests for SchedulerFrame.
 */
class SchedulerFrameTest {

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
    void frameShouldHaveProTitle_failingFirst() {
        // EXPECTED TITLE (intentionally wrong → will fail)
        window.requireTitle("Employee Shift Scheduler PRO");
    }

    @Test
    void addShiftButton_shouldBeDisabledInitially_failingFirst() {
        // Your current UI ENABLES the button → this should fail.
        window.button("addShiftButton").requireDisabled();
    }

    @Test
    void shiftsTable_shouldHaveUtcColumnHeaders_failingFirst() {
        // The current ShiftsTableModel uses "Start", "End"
        // This test expects "Start (UTC)" and "End (UTC)" → failing-first
        window.table("shiftsTable").requireColumnCount(3);

        assertEquals("Shift ID",
                window.table("shiftsTable").target().getColumnName(0));
        assertEquals("Start (UTC)",
                window.table("shiftsTable").target().getColumnName(1));
        assertEquals("End (UTC)",
                window.table("shiftsTable").target().getColumnName(2));
    }

    @Test
    void clickingAddShift_whenEmployeeExists_shouldUpdateTable_failingFirst() {
        String empId = "E100";
        Shift shift = new Shift(
                LocalDateTime.of(2025, 11, 30, 8, 0),
                LocalDateTime.of(2025, 11, 30, 12, 0)
        );

        when(controller.addShift(eq(empId), any())).thenReturn(true);
        when(controller.listShifts(empId)).thenReturn(List.of(shift));

        window.textBox("employeeIdField").enterText(empId);
        window.button("addShiftButton").click();

        // Failing expected row count (forces RED)
        window.table("shiftsTable").requireRowCount(2); // Your UI will give only 1 row
    }
}
