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
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

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

    // primary input format (preferred)
    private static final DateTimeFormatter INPUT_FORMAT_PRIMARY = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");
    // fallback with seconds
    private static final DateTimeFormatter INPUT_FORMAT_WITH_SECONDS = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
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

        // When the frame becomes visible, ensure a component has focus so AssertJ can focus text fields
        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                // request focus on the employeeIdField after show; use invokeLater to avoid timing races
                SwingUtilities.invokeLater(() -> {
                    if (employeeIdField != null && employeeIdField.isFocusable()) {
                        employeeIdField.requestFocusInWindow();
                    }
                });
            }
        });
    }

    private void initComponents() {
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Employee ID:"));
        employeeIdField = new JTextField(10);
        employeeIdField.setName("employeeIdField");
        employeeIdField.setFocusable(true); // defensive: ensure focusable
        top.add(employeeIdField);

        top.add(new JLabel("Start (yyyy-MM-dd'T'HH:mm or yyyy-MM-dd'T'HH:mm:ss):"));
        startField = new JTextField(16);
        startField.setName("startField");
        startField.setFocusable(true);
        top.add(startField);

        top.add(new JLabel("End (yyyy-MM-dd'T'HH:mm or yyyy-MM-dd'T'HH:mm:ss):"));
        endField = new JTextField(16);
        endField.setName("endField");
        endField.setFocusable(true);
        top.add(endField);

        addShiftButton = new JButton("Add Shift");
        addShiftButton.setName("addShiftButton");
        addShiftButton.setEnabled(false); // default; updateAddEnabled() will set the correct state
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

        // Ensure the initial enabled state respects any pre-filled fields (tests sometimes set fields before showing)
        updateAddEnabled();

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
                            startField.setText(LocalDateTime.ofInstant(st, ZoneOffset.UTC).format(INPUT_FORMAT_PRIMARY));
                        } else startField.setText("");
                    } catch (Exception ignore) { startField.setText(""); }
                    try {
                        if (eo != null) {
                            Instant en = Instant.parse(eo.toString());
                            endField.setText(LocalDateTime.ofInstant(en, ZoneOffset.UTC).format(INPUT_FORMAT_PRIMARY));
                        } else endField.setText("");
                    } catch (Exception ignore) { endField.setText(""); }
                }
            }
        });

        addShiftButton.addActionListener(ae -> {
            String emp = employeeIdField.getText().trim();
            LocalDateTime s, e;
            try {
                s = parseLocalDateTimeFlexible(startField.getText().trim());
                e = parseLocalDateTimeFlexible(endField.getText().trim());
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date/time!");
                return;
            }
            Shift shift = new Shift(s, e);
            // call controller (mock in tests will intercept this)
            boolean ok;
            try {
                ok = controller.addShift(emp, shift);
            } catch (Throwable t) {
                // controller might throw in some implementations — show but don't crash tests
                JOptionPane.showMessageDialog(this, "Cannot add shift: " + t.getMessage());
                return;
            }
            if (ok) {
                List<Shift> shifts = controller.listShifts(emp);
                runReloadWithDelay(shifts);
            } else {
                // show user feedback (and tests that expect false will handle it)
                JOptionPane.showMessageDialog(this, "Add operation reported failure");
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
                s = parseLocalDateTimeFlexible(startField.getText().trim());
                e = parseLocalDateTimeFlexible(endField.getText().trim());
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

    /**
     * Flexible parser: tries primary pattern (yyyy-MM-dd'T'HH:mm) and falls back to seconds pattern.
     * Throws Exception if none match.
     */
    private LocalDateTime parseLocalDateTimeFlexible(String txt) {
        if (txt == null || txt.isEmpty()) throw new IllegalArgumentException("empty");
        try {
            return LocalDateTime.parse(txt, INPUT_FORMAT_PRIMARY);
        } catch (Exception ignored) {}
        try {
            return LocalDateTime.parse(txt, INPUT_FORMAT_WITH_SECONDS);
        } catch (Exception ignored) {}
        // try ISO_LOCAL_DATE_TIME (even more permissive)
        try {
            return LocalDateTime.parse(txt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid date/time format: " + txt);
        }
    }

    private void updateAddEnabled() {
        try {
            boolean ok = !employeeIdField.getText().isEmpty()
                    && !startField.getText().isEmpty()
                    && !endField.getText().isEmpty();
            // quick parse test: ensure parsing succeeds with flexible parser
            if (ok) {
                try {
                    parseLocalDateTimeFlexible(startField.getText().trim());
                    parseLocalDateTimeFlexible(endField.getText().trim());
                } catch (Exception ex) {
                    ok = false; // parsing failed -> don't enable button
                }
            }
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
