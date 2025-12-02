package it.unifi.attsw.employee_shift_scheduler_gui;

import it.unifi.attsw.employee_shift_scheduler.Controller;
import it.unifi.attsw.employee_shift_scheduler.Shift;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.finder.JOptionPaneFinder;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.fixture.JOptionPaneFixture;
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
 * Test removing a shift from SchedulerFrame, confirming the JOptionPane dialog.
 */
class SchedulerFrameRemoveTest {

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
    void addThenRemove_shiftRemovesAndTableUpdates_withConfirmation() {
        window.requireTitle("Employee Shift Scheduler PRO");

        String empId = "E200";
        LocalDateTime s = LocalDateTime.of(2025, 12, 1, 9, 0);
        LocalDateTime e = LocalDateTime.of(2025, 12, 1, 17, 0);
        Shift created = new Shift(s, e);
        String createdId = created.getId();

        // Mock: addShift returns true
        when(controller.addShift(eq(empId), any())).thenReturn(true);
        // Mock: listShifts returns created shift first, then empty after removal
        when(controller.listShifts(empId)).thenReturn(List.of(created), List.of());

        // When remove called, do nothing (void)
        doNothing().when(controller).removeShiftFromEmployee(eq(empId), eq(createdId));

        // Fill fields and add shift
        window.textBox("employeeIdField").enterText(empId);
        window.textBox("startField").enterText("2025-12-01T09:00");
        window.textBox("endField").enterText("2025-12-01T17:00");
        window.button("addShiftButton").requireEnabled();
        window.button("addShiftButton").click();

        // Table should now have 1 row
        window.table("shiftsTable").requireRowCount(1);

        // Select the row and ensure remove button becomes enabled
        window.table("shiftsTable").selectRows(0);
        window.button("removeShiftButton").requireEnabled();

        // Click remove — a confirmation dialog appears. Use JOptionPaneFinder to find it and click YES.
        window.button("removeShiftButton").click();
        JOptionPaneFixture opt = JOptionPaneFinder.findOptionPane().using(robot);
        opt.yesButton().click();

        // Verify controller removal called
        verify(controller, atLeastOnce()).removeShiftFromEmployee(eq(empId), eq(createdId));

        // Table should now have 0 rows
        window.table("shiftsTable").requireRowCount(0);
    }
}
