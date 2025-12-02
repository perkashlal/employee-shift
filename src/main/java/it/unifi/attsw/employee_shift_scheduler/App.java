package it.unifi.attsw.employee_shift_scheduler;

import it.unifi.attsw.employee_shift_scheduler_gui.EmployeeListPanel;
import it.unifi.attsw.employee_shift_scheduler_gui.SchedulerFrame;

import javax.swing.*;
import java.awt.*;

/**
 * Small launcher that wires in-memory repo -> service -> controller and shows the frame.
 */
public class App {

    public static void main(String[] args) {
        // create wiring
        EmployeeRepository repo = new InMemoryEmployeeRepository();
        EmployeeService service = new EmployeeService(repo);
        Controller controller = new Controller(service);

        // add some sample employees for manual testing
        controller.saveEmployee(new Employee("Alice", "E001", "cashier"));
        controller.saveEmployee(new Employee("Bob", "E002", "manager"));

        SwingUtilities.invokeLater(() -> {
            SchedulerFrame frame = new SchedulerFrame(controller);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(900, 600);
            frame.setVisible(true);

            // optionally show EmployeeListPanel on side for manual testing
            JFrame side = new JFrame("Employees");
            side.setLayout(new BorderLayout());
            side.add(new EmployeeListPanel(controller), BorderLayout.CENTER);
            side.setSize(260, 420);
            side.setLocation(frame.getX() - 280, frame.getY());
            side.setVisible(true);
        });
    }
}