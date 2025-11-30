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
 * GUI tests using AssertJ-Swing. Uses BasicRobot and FrameFixture.
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
    void frameShouldHaveProTitle_and_buttonEnablement_and_tableUpdate() {
        // title check
        window.requireTitle("Employee Shift Scheduler PRO");

        // button disabled initially
        window.button("addShiftButton").requireDisabled();

        // type and enable, mock controller to return one shift
        String empId = "E100";
        Shift created = new Shift(LocalDateTime.of(2025,11,30,8,0), LocalDateTime.of(2025,11,30,12,0));

        when(controller.addShift(eq(empId), any())).thenReturn(true);
        when(controller.listShifts(empId)).thenReturn(List.of(created));

        window.textBox("employeeIdField").enterText(empId);
        window.button("addShiftButton").requireEnabled();
        window.button("addShiftButton").click();

        verify(controller, atLeastOnce()).addShift(eq(empId), any());
        window.table("shiftsTable").requireRowCount(1);

        // check headers
        assert "Shift ID".equals(window.table("shiftsTable").target().getColumnName(0));
        assert "Start (UTC)".equals(window.table("shiftsTable").target().getColumnName(1));
        assert "End (UTC)".equals(window.table("shiftsTable").target().getColumnName(2));
    }
}
