package it.unifi.attsw.employee_shift_scheduler.integration;

import it.unifi.attsw.employee_shift_scheduler.Controller;
import it.unifi.attsw.employee_shift_scheduler.EmployeeService;
import it.unifi.attsw.employee_shift_scheduler.InMemoryEmployeeRepository;
import it.unifi.attsw.employee_shift_scheduler_gui.SchedulerFrame;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.edt.GuiQuery;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.Test;
import javax.swing.*;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E2E smoke test running under JUnit 4 (required by AssertJ Swing)
 */
public class SchedulerE2EIT extends AssertJSwingJUnitTestCase {

    private FrameFixture window;

    @Override
    protected void onSetUp() {
        InMemoryEmployeeRepository repo = new InMemoryEmployeeRepository();
        EmployeeService service = new EmployeeService(repo);
        Controller controller = new Controller(service);

        SchedulerFrame frame = GuiActionRunner.execute(new GuiQuery<SchedulerFrame>() {
            @Override
            protected SchedulerFrame executeInEDT() {
                SchedulerFrame f = new SchedulerFrame(controller);
                f.setName("mainFrame");
                f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                return f;
            }
        });

        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @Override
    protected void onTearDown() {
        if (window != null) window.cleanUp();
    }

    @Test
    public void frame_is_visible() {
        JFrame f = (JFrame) window.target();
        assertThat(f.isShowing()).isTrue();
    }
}
