package it.unifi.attsw.employee_shift_scheduler_gui;

import it.unifi.attsw.employee_shift_scheduler.Controller;
import it.unifi.attsw.employee_shift_scheduler.Shift;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;

public class SchedulerFrame extends JFrame {

    private final Controller controller;

    public final JTextField employeeIdField = new JTextField(10);
    public final JButton addShiftButton = new JButton("Add Shift");
    public final JTable shiftsTable = new JTable(new ShiftsTableModel());

    public SchedulerFrame(Controller controller) {
        super("Employee Shift Scheduler");
        this.controller = controller;

        setName("schedulerFrame");
        employeeIdField.setName("employeeIdField");
        addShiftButton.setName("addShiftButton");
        shiftsTable.setName("shiftsTable");

        initUi();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        pack();
        setLocationRelativeTo(null);
    }

    private void initUi() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Employee ID:"));
        top.add(employeeIdField);
        top.add(addShiftButton);

        addShiftButton.addActionListener(this::onAddShift);

        JScrollPane scrollPane = new JScrollPane(shiftsTable);
        scrollPane.setPreferredSize(new Dimension(600, 250));

        setLayout(new BorderLayout(10, 10));
        add(top, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void onAddShift(ActionEvent event) {
        String empId = employeeIdField.getText().trim();
        if (empId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Employee ID required");
            return;
        }
        LocalDateTime start = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(4);
        Shift shift = new Shift(start, end);
        boolean success = controller.addShift(empId, shift);
        if (!success) {
            JOptionPane.showMessageDialog(this, "Employee not found");
            return;
        }
        ShiftsTableModel model = (ShiftsTableModel) shiftsTable.getModel();
        model.setShifts(controller.listShifts(empId));
    }
}
