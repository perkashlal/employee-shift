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
 * GUI tests using AssertJ-Swing.
 * Ensures SchedulerFrame UI behaves as expected when adding a shift.
 */
class SchedulerFrameTest {

    private FrameFixture window;
    private Robot robot;
    private Controller controller;

    @BeforeEach
    void setUp() {
        controller = mock(Controller.class);
        // create a fresh robot for each test to avoid cross-test state
        robot = BasicRobot.robotWithNewAwtHierarchy();
        SchedulerFrame frame = GuiActionRunner.execute(() -> new SchedulerFrame(controller));
        window = new FrameFixture(robot, frame);
        window.show(); // shows the frame on screen - required for AssertJ-Swing to interact
    }

    @AfterEach
    void tearDown() {
        if (window != null) window.cleanUp();
        if (robot != null) robot.cleanUp();
    }

    @Test
    void addShift_withStartEnd_enablesAndPopulatesTable() {
        // check title
        window.requireTitle("Employee Shift Scheduler PRO");

        // add button should be disabled initially
        window.button("addShiftButton").requireDisabled();

        // prepare test data
        String empId = "E100";
        LocalDateTime s = LocalDateTime.of(2025, 11, 30, 8, 0);
        LocalDateTime e = LocalDateTime.of(2025, 11, 30, 12, 0);

        // create a Shift instance for the mocked controller to return
        Shift created = new Shift(s, e); // adapt if your Shift ctor differs

        // mock controller behavior
        when(controller.addShift(eq(empId), any())).thenReturn(true);
        when(controller.listShifts(empId)).thenReturn(List.of(created));

        // interact with UI: enter fields
        window.textBox("employeeIdField").enterText(empId);
        window.textBox("startField").enterText("2025-11-30T08:00");
        window.textBox("endField").enterText("2025-11-30T12:00");

        // after entering required fields the add button should become enabled
        window.button("addShiftButton").requireEnabled();

        // click the add button
        window.button("addShiftButton").click();

        // verify controller interactions
        verify(controller, atLeastOnce()).addShift(eq(empId), any());
        verify(controller, atLeastOnce()).listShifts(eq(empId));

        // table should have one row populated
        window.table("shiftsTable").requireRowCount(1);

        // verify column headers
        assert "Shift ID".equals(window.table("shiftsTable").target().getColumnName(0));
        assert "Start (UTC)".equals(window.table("shiftsTable").target().getColumnName(1));
        assert "End (UTC)".equals(window.table("shiftsTable").target().getColumnName(2));
    }
}
