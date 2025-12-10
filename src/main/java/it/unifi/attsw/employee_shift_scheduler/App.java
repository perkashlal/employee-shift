package it.unifi.attsw.employee_shift_scheduler;

import it.unifi.attsw.employee_shift_scheduler_gui.SchedulerFrame;
import it.unifi.attsw.employee_shift_scheduler.repository.EmployeeRepository;
import it.unifi.attsw.employee_shift_scheduler.MongoEmployeeRepository;

import javax.swing.SwingUtilities;

/**
 * Application launcher wired to MongoDB.
 * Adjust MONGO_URI if your Mongo runs on a different host/port or uses a different username/password.
 */
public class App {

    // Adjust this URI to match your Mongo credentials/host/port.
    // NOTE: percent-encode special characters in the password. Example: "appuserPass!23" -> "appuserPass%2123"
    private static final String MONGO_URI = "mongodb://appuser:appuserPass%2123@localhost:27017/?authSource=employee_shift_db";
    private static final String DB_NAME = "employee_shift_db";
    private static final String COLLECTION = "employees";

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            // Create a Mongo-backed repository using the constructor already present in your project.
            // MongoEmployeeRepository has a constructor (String uri, String dbName, String collectionName)
            EmployeeRepository repo = new MongoEmployeeRepository(MONGO_URI, DB_NAME, COLLECTION);

            // Create the real EmployeeService using the repository.
            // If your EmployeeService constructor signature differs, adapt this call accordingly.
            EmployeeService employeeService = new EmployeeService(repo);

            // Create a simple no-op view (Controller requires an EmployeeView)
            Controller.EmployeeView viewStub = new Controller.EmployeeView() {
                @Override public void showAllEmployees(java.util.List<Employee> employees) {}
                @Override public void employeeAdded(Employee employee) {}
                @Override public void employeeRemoved(Employee employee) {}
                @Override public void showError(String message) { System.err.println("[ViewStub] ERROR: " + message); }
                @Override public void shiftAddedToEmployee(Employee employee, Shift shift) {}
                @Override public void shiftRemovedFromEmployee(Employee employee, String shiftId) {}
            };

            // Create the controller wired to the real service
            Controller controller = new Controller(employeeService, viewStub);

            // Launch the UI
            SchedulerFrame frame = new SchedulerFrame(controller);
            frame.setVisible(true);
        });
    }
}
