package it.unifi.attsw.employee_shift_scheduler_gui;

import it.unifi.attsw.employee_shift_scheduler.Controller;
import it.unifi.attsw.employee_shift_scheduler.Shift;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * SchedulerFrame with optional UI update delay.
 * - Default constructor (controller) uses delayMillis = 0 (immediate updates) — good for tests.
 * - Use SchedulerFrame(controller, delayMillis) to enable a visual delay (milliseconds) for manual runs.
 */
public class SchedulerFrame extends JFrame {

    private final Controller controller;
    public JTextField employeeIdField;
    public JTextField startField;
    public JTextField endField;
    public JButton addShiftButton;
    public JButton removeShiftButton;
    public JButton updateShiftButton;
    public JTable shiftsTable;
    private DefaultTableModel tableModel;

    private JLabel weeklyHoursLabel;

    private static final DateTimeFormatter INPUT_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private static final DateTimeFormatter DISPLAY_UTC = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

    // If <= 0 -> immediate updates (suitable for tests). If > 0 -> delayed UI refresh in ms.
    private final int delayMillis;

    /**
     * Default constructor — immediate updates (delay = 0).
     * Keeps tests deterministic (no SwingWorker delay).
     */
    public SchedulerFrame(Controller controller) {
        this(controller, 0);
    }

    /**
     * Constructor with configurable delay.
     * @param controller Controller instance
     * @param delayMillis milliseconds to wait before refreshing UI (use 0 for immediate)
     */
    public SchedulerFrame(Controller controller, int delayMillis) {
        super("Employee Shift Scheduler PRO");
        this.controller = controller;
        this.delayMillis = delayMillis;
        initComponents();
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 540);
        setLocationRelativeTo(null);
    }

    private void initComponents() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Employee ID:"));
        employeeIdField = new JTextField(10);
        employeeIdField.setName("employeeIdField");
        top.add(employeeIdField);

        top.add(new JLabel("Start (yyyy-MM-dd'T'HH:mm):"));
        startField = new JTextField(16);
        startField.setName("startField");
        top.add(startField);

        top.add(new JLabel("End (yyyy-MM-dd'T'HH:mm):"));
        endField = new JTextField(16);
        endField.setName("endField");
        top.add(endField);

        addShiftButton = new JButton("Add Shift");
        addShiftButton.setName("addShiftButton");
        addShiftButton.setEnabled(false);
        top.add(addShiftButton);

        updateShiftButton = new JButton("Update Shift");
        updateShiftButton.setName("updateShiftButton");
        updateShiftButton.setEnabled(false);
        top.add(updateShiftButton);

        removeShiftButton = new JButton("Remove Shift");
        removeShiftButton.setName("removeShiftButton");
        removeShiftButton.setEnabled(false);
        top.add(removeShiftButton);

        String[] cols = {"Shift ID", "Start (UTC)", "End (UTC)"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };

        shiftsTable = new JTable(tableModel);
        shiftsTable.setName("shiftsTable");
        JScrollPane scroll = new JScrollPane(shiftsTable);

        JPanel center = new JPanel(new BorderLayout());
        center.add(scroll, BorderLayout.CENTER);

        weeklyHoursLabel = new JLabel("Total Weekly Hours: 0.0h");
        weeklyHoursLabel.setName("weeklyHoursLabel");
        weeklyHoursLabel.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
        center.add(weeklyHoursLabel, BorderLayout.SOUTH);

        this.setLayout(new BorderLayout());
        this.add(top, BorderLayout.NORTH);
        this.add(center, BorderLayout.CENTER);

        DocumentListener dl = new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { updateAddEnabled(); }
            @Override public void removeUpdate(DocumentEvent e) { updateAddEnabled(); }
            @Override public void changedUpdate(DocumentEvent e) { updateAddEnabled(); }
        };
        employeeIdField.getDocument().addDocumentListener(dl);
        startField.getDocument().addDocumentListener(dl);
        endField.getDocument().addDocumentListener(dl);

        shiftsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override public void valueChanged(ListSelectionEvent e) {
                int sel = shiftsTable.getSelectedRow();
                boolean selected = sel >= 0;
                removeShiftButton.setEnabled(selected);
                updateShiftButton.setEnabled(selected);

                if (selected) {
                    Object so = tableModel.getValueAt(sel, 1);
                    Object eo = tableModel.getValueAt(sel, 2);
                    try {
                        if (so != null) {
                            Instant st = Instant.parse(so.toString());
                            startField.setText(LocalDateTime.ofInstant(st, ZoneOffset.UTC).format(INPUT_FORMAT));
                        } else startField.setText("");
                    } catch (Exception ignore) { startField.setText(""); }
                    try {
                        if (eo != null) {
                            Instant en = Instant.parse(eo.toString());
                            endField.setText(LocalDateTime.ofInstant(en, ZoneOffset.UTC).format(INPUT_FORMAT));
                        } else endField.setText("");
                    } catch (Exception ignore) { endField.setText(""); }
                }
            }
        });

        addShiftButton.addActionListener(ae -> {
            String emp = employeeIdField.getText().trim();
            LocalDateTime s, e;
            try {
                s = LocalDateTime.parse(startField.getText().trim(), INPUT_FORMAT);
                e = LocalDateTime.parse(endField.getText().trim(), INPUT_FORMAT);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date/time!");
                return;
            }
            Shift shift = new Shift(s, e);
            boolean ok = controller.addShift(emp, shift);
            if (ok) {
                List<Shift> shifts = controller.listShifts(emp);
                runReloadWithDelay(shifts);
            }
        });

        updateShiftButton.addActionListener(ae -> {
            int sel = shiftsTable.getSelectedRow();
            if (sel < 0) return;

            String emp = employeeIdField.getText().trim();
            Object idObj = tableModel.getValueAt(sel, 0);
            String shiftId = idObj == null ? null : idObj.toString();

            LocalDateTime s, e;
            try {
                s = LocalDateTime.parse(startField.getText().trim(), INPUT_FORMAT);
                e = LocalDateTime.parse(endField.getText().trim(), INPUT_FORMAT);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date/time!");
                return;
            }

            int ans = JOptionPane.showConfirmDialog(this, "Update this shift?", "Confirm update", JOptionPane.YES_NO_OPTION);
            if (ans != JOptionPane.YES_OPTION) return;

            try { if (shiftId != null) controller.removeShiftFromEmployee(emp, shiftId); } catch (Exception ignore) {}
            controller.addShift(emp, new Shift(s, e));

            List<Shift> shifts = controller.listShifts(emp);
            runReloadWithDelay(shifts);
        });

        removeShiftButton.addActionListener(ae -> {
            int sel = shiftsTable.getSelectedRow();
            if (sel < 0) return;
            String emp = employeeIdField.getText().trim();
            Object idObj = tableModel.getValueAt(sel, 0);
            String shiftId = idObj == null ? null : idObj.toString();

            int ans = JOptionPane.showConfirmDialog(this, "Remove this shift?", "Confirm delete", JOptionPane.YES_NO_OPTION);
            if (ans != JOptionPane.YES_OPTION) return;

            controller.removeShiftFromEmployee(emp, shiftId);
            List<Shift> shifts = controller.listShifts(emp);
            runReloadWithDelay(shifts);
        });
    }

    private void updateAddEnabled() {
        try {
            boolean ok = !employeeIdField.getText().isEmpty()
                    && !startField.getText().isEmpty()
                    && !endField.getText().isEmpty()
                    && LocalDateTime.parse(startField.getText().trim(), INPUT_FORMAT) != null
                    && LocalDateTime.parse(endField.getText().trim(), INPUT_FORMAT) != null;

            addShiftButton.setEnabled(ok);
        } catch (Exception ex) {
            addShiftButton.setEnabled(false);
        }
    }

    /**
     * Reload table and weekly hours either immediately (delayMillis <= 0)
     * or after a SwingWorker delay (delayMillis > 0).
     */
    private void runReloadWithDelay(List<Shift> shifts) {
        if (delayMillis <= 0) {
            // Immediate — keeps unit tests stable and deterministic
            reloadShiftsTable(shifts);
            return;
        }

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override protected Void doInBackground() throws Exception {
                Thread.sleep(delayMillis);
                return null;
            }
            @Override protected void done() {
                reloadShiftsTable(shifts);
            }
        };
        worker.execute();
    }

    private void reloadShiftsTable(List<Shift> shifts) {
        tableModel.setRowCount(0);
        if (shifts != null) {
            for (Shift sh : shifts) {
                String id = sh == null ? null : sh.getId();
                String start = null;
                String end = null;
                try { if (sh != null && sh.getStart() != null) start = DISPLAY_UTC.format(sh.getStart()); } catch (Exception ignore) {}
                try { if (sh != null && sh.getEnd() != null) end = DISPLAY_UTC.format(sh.getEnd()); } catch (Exception ignore) {}
                tableModel.addRow(new Object[]{id, start, end});
            }
        }
        updateWeeklyHours(computeWeeklyHours(shifts));
    }

    private double computeWeeklyHours(List<Shift> shifts) {
        double total = 0;
        if (shifts != null) {
            for (Shift sh : shifts) {
                try {
                    Instant s = sh.getStart();
                    Instant e = sh.getEnd();
                    if (s != null && e != null) {
                        Duration d = Duration.between(s, e);
                        total += d.toMinutes() / 60.0;
                    }
                } catch (Exception ignore) {}
            }
        }
        return total;
    }

    private void updateWeeklyHours(double hours) {
        weeklyHoursLabel.setText(String.format(Locale.US, "Total Weekly Hours: %.1fh", hours));
    }
}
