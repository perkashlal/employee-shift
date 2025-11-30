package it.unifi.attsw.employee_shift_scheduler_gui;

import it.unifi.attsw.employee_shift_scheduler.Controller;
import it.unifi.attsw.employee_shift_scheduler.Shift;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDateTime;

/**
 * Minimal GUI wired to Controller, components named for AssertJ lookups.
 */
public class SchedulerFrame extends JFrame {

    private final Controller controller;

    public final JTextField employeeIdField = new JTextField(10);
    public final JButton addShiftButton = new JButton("Add Shift");
    public final JTable shiftsTable = new JTable(new ShiftsTableModel());

    public SchedulerFrame(Controller controller) {
        super("Employee Shift Scheduler PRO"); // test expects PRO title
        this.controller = controller;

        setName("schedulerFrame");
        employeeIdField.setName("employeeIdField");
        addShiftButton.setName("addShiftButton");
        shiftsTable.setName("shiftsTable");

        // start disabled until id typed
        addShiftButton.setEnabled(false);

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

        employeeIdField.getDocument().addDocumentListener(new DocumentListener() {
            private void update() {
                String t = employeeIdField.getText();
                addShiftButton.setEnabled(t != null && !t.trim().isEmpty());
            }
            @Override public void insertUpdate(DocumentEvent e) { update(); }
            @Override public void removeUpdate(DocumentEvent e) { update(); }
            @Override public void changedUpdate(DocumentEvent e) { update(); }
        });

        addShiftButton.addActionListener(this::onAddShift);

        JScrollPane scroll = new JScrollPane(shiftsTable);
        scroll.setPreferredSize(new Dimension(600, 250));

        setLayout(new BorderLayout(10,10));
        add(top, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void onAddShift(ActionEvent ev) {
        String empId = employeeIdField.getText().trim();
        if (empId.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Employee ID required");
            return;
        }

        LocalDateTime start = LocalDateTime.now().withHour(8).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(4);
        Shift shift = new Shift(start, end);

        boolean ok = controller.addShift(empId, shift);
        if (!ok) {
            JOptionPane.showMessageDialog(this, "Employee not found");
            return;
        }
        ShiftsTableModel model = (ShiftsTableModel) shiftsTable.getModel();
        model.setShifts(controller.listShifts(empId));
    }
}
