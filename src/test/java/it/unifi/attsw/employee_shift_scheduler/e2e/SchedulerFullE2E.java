package it.unifi.attsw.employee_shift_scheduler.e2e;

import it.unifi.attsw.employee_shift_scheduler.*;
import it.unifi.attsw.employee_shift_scheduler_gui.SchedulerFrame;

import org.assertj.swing.core.matcher.JButtonMatcher;
import org.assertj.swing.data.TableCell;
import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * FULL END-TO-END TEST:
 * GUI → Controller → Service → InMemoryRepo → GUI Update
 */
public class SchedulerFullE2E extends AssertJSwingJUnitTestCase {

    private FrameFixture window;
    private InMemoryEmployeeRepository repo;
    private EmployeeService service;
    private Controller controller;

    @Override
    protected void onSetUp() {
        repo = new InMemoryEmployeeRepository();
        service = new EmployeeService(repo);
        controller = new Controller(service);

        SchedulerFrame frame = GuiActionRunner.execute(() ->
                new SchedulerFrame(controller, 0)  // deterministic UI updates
        );

        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @Test
    void frame_is_visible() {
        window.requireVisible();
    }

    @Test
    void user_can_add_and_remove_shift() {

        // Fill input fields
        window.textBox("employeeIdField").enterText("E100");
        window.textBox("startField").enterText("2025-12-10T09:00");
        window.textBox("endField").enterText("2025-12-10T12:00");

        // Add shift
        window.button(JButtonMatcher.withName("addShiftButton")).click();

        // Verify table row count
        assertThat(window.table("shiftsTable").rowCount()).isEqualTo(1);

        // Read start cell (safe for byte[] case)
        Object rawStart = window.table("shiftsTable")
                .valueAt(TableCell.row(0).column(1));

        Object rawEnd = window.table("shiftsTable")
                .valueAt(TableCell.row(0).column(2));

        String startTxt = (rawStart instanceof byte[])
                ? new String((byte[]) rawStart)
                : rawStart.toString();

        String endTxt = (rawEnd instanceof byte[])
                ? new String((byte[]) rawEnd)
                : rawEnd.toString();

        assertThat(startTxt).contains("2025-12-10T09:00");
        assertThat(endTxt).contains("2025-12-10T12:00");

        // Select row & remove shift
        window.table("shiftsTable").selectRows(0);
        window.button(JButtonMatcher.withName("removeShiftButton")).click();

        // Table should now be empty
        assertThat(window.table("shiftsTable").rowCount()).isZero();

        // Ensure the controller repo is updated
        List<Shift> shiftsAfter = controller.listShifts("E100");
        assertThat(shiftsAfter).isEmpty();
    }
}
