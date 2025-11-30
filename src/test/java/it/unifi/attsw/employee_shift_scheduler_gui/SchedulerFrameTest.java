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
 * SchedulerFrame GUI test using AssertJ-Swing with JUnit5.
 * We create and clean up the Robot and FrameFixture manually.
 */
class SchedulerFrameTest {

    private FrameFixture window;
    private Robot robot;
    private Controller controller;

    @BeforeEach
    void setUp() {
        // create Mockito controller mock
        controller = mock(Controller.class);

        // create a fresh Robot for each test (new AWT hierarchy)
        robot = BasicRobot.robotWithNewAwtHierarchy();

        // create the frame on the EDT and wrap it with FrameFixture
        SchedulerFrame frame = GuiActionRunner.execute(() -> new SchedulerFrame(controller));
        window = new FrameFixture(robot, frame);
        window.show(); // shows the frame
    }

    @AfterEach
    void tearDown() {
        if (window != null) {
            window.cleanUp(); // closes windows and releases resources
        }
        if (robot != null) {
            robot.cleanUp();
        }
    }

    @Test
    void clickingAddShift_whenEmployeeExists_shouldCallControllerAndUpdateTable() {
        // arrange
        String empId = "E100";
        Shift created = new Shift(LocalDateTime.of(2025, 11, 30, 8, 0),
                                  LocalDateTime.of(2025, 11, 30, 12, 0));

        // controller will accept the add and then return a list containing the shift
        when(controller.addShift(eq(empId), any())).thenReturn(true);
        when(controller.listShifts(empId)).thenReturn(List.of(created));

        // act: enter employee id and click Add Shift
        window.textBox("employeeIdField").enterText(empId);
        window.button("addShiftButton").click();

        // assert: controller.addShift invoked and table shows one row
        verify(controller, atLeastOnce()).addShift(eq(empId), any());
        window.table("shiftsTable").requireRowCount(1);
    }

    @Test
    void clickingAddShift_whenEmployeeMissing_shouldShowNoCallAndNoRows() {
        // arrange
        String empId = "MISSING";
        when(controller.addShift(eq(empId), any())).thenReturn(false);
        when(controller.listShifts(empId)).thenReturn(List.of());

        // act
        window.textBox("employeeIdField").enterText(empId);
        window.button("addShiftButton").click();

        // assert: controller called and table remains empty
        verify(controller, atLeastOnce()).addShift(eq(empId), any());
        window.table("shiftsTable").requireRowCount(0);
    }
}
