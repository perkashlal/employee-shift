package it.unifi.attsw.employee_shift_scheduler_gui;

import it.unifi.attsw.employee_shift_scheduler.Controller;
import it.unifi.attsw.employee_shift_scheduler.Shift;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.function.BiConsumer;

/**
 * Small modal dialog to enter a shift (date + start hour + duration).
 * On OK it calls a callback with created Shift and optional notes.
 */
public class AddShiftDialog extends JDialog {

    // exposed for test lookup
    public final JComboBox<Integer> hourBox = new JComboBox<>();
    public final JComboBox<Integer> minuteBox = new JComboBox<>();
    public final JSpinner dateSpinner;
    public final JTextField durationField = new JTextField("4", 3);
    public final JButton okButton = new JButton("OK");
    public final JButton cancelButton = new JButton("Cancel");

    private Shift created;

    public AddShiftDialog(Window owner) {
        super(owner, "Add Shift", ModalityType.APPLICATION_MODAL);

        // date spinner
        dateSpinner = new JSpinner(new SpinnerDateModel());
        dateSpinner.setName("shiftDateSpinner");

        // hours/minutes
        for (int h = 0; h < 24; h++) hourBox.addItem(h);
        for (int m = 0; m < 60; m += 5) minuteBox.addItem(m);

        hourBox.setName("hourBox");
        minuteBox.setName("minuteBox");
        durationField.setName("durationField");
        okButton.setName("okButton");
        cancelButton.setName("cancelButton");

        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6,6));
        p.add(new JLabel("Date:")); p.add(dateSpinner);
        p.add(new JLabel("Hour:")); p.add(hourBox);
        p.add(new JLabel("Min:")); p.add(minuteBox);
        p.add(new JLabel("Hours:")); p.add(durationField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(okButton);
        buttons.add(cancelButton);

        getContentPane().setLayout(new BorderLayout(6,6));
        add(p, BorderLayout.CENTER);
        add(buttons, BorderLayout.SOUTH);
        pack();
        setLocationRelativeTo(owner);

        okButton.addActionListener(e -> onOk());
        cancelButton.addActionListener(e -> onCancel());
    }

    private void onOk() {
        // read values and create Shift
        java.util.Date d = (java.util.Date) dateSpinner.getValue();
        LocalDate date = d.toInstant().atZone(java.time.ZoneId.systemDefault()).toLocalDate();
        int hour = (Integer) hourBox.getSelectedItem();
        int min = (Integer) minuteBox.getSelectedItem();
        int dur;
        try {
            dur = Integer.parseInt(durationField.getText().trim());
            if (dur <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Invalid duration");
            return;
        }
        LocalDateTime start = LocalDateTime.of(date, LocalTime.of(hour, min));
        LocalDateTime end = start.plusHours(dur);
        created = new Shift(start, end);
        setVisible(false);
    }

    private void onCancel() {
        created = null;
        setVisible(false);
    }

    public Optional<Shift> showAndGet() {
        setVisible(true); // blocking
        return Optional.ofNullable(created);
    }
}