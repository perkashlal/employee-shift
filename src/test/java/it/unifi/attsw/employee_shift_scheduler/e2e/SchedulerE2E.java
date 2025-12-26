package it.unifi.attsw.employee_shift_scheduler.e2e;

import it.unifi.attsw.employee_shift_scheduler.Controller;
import it.unifi.attsw.employee_shift_scheduler.EmployeeService;
import it.unifi.attsw.employee_shift_scheduler.InMemoryEmployeeRepository;
import it.unifi.attsw.employee_shift_scheduler_gui.SchedulerFrame;

import org.assertj.swing.edt.GuiActionRunner;
import org.assertj.swing.fixture.FrameFixture;
import org.assertj.swing.junit.testcase.AssertJSwingJUnitTestCase;
import org.junit.jupiter.api.Test;

import javax.swing.*;

import static org.assertj.core.api.Assertions.assertThat;

public class SchedulerE2E extends AssertJSwingJUnitTestCase {

    private FrameFixture window;

    @Override
    protected void onSetUp() {
        InMemoryEmployeeRepository repo = new InMemoryEmployeeRepository();
        EmployeeService service = new EmployeeService(repo);
        Controller controller = new Controller(service);

        SchedulerFrame frame = GuiActionRunner.execute(() -> {
            SchedulerFrame f = new SchedulerFrame(controller, 0);
            f.setName("mainFrame");
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            return f;
        });

        window = new FrameFixture(robot(), frame);
        window.show();
    }

    @Override
    protected void onTearDown() {
        if (window != null) window.cleanUp();
    }

    @Test
    void frame_is_visible() {
        assertThat(window.target().isShowing()).isTrue();
    }
}
