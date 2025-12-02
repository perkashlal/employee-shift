package it.unifi.attsw.employee_shift_scheduler_gui;

import it.unifi.attsw.employee_shift_scheduler.Controller;
import it.unifi.attsw.employee_shift_scheduler.Employee;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Small panel that lists employees and notifies listener when selection changes.
 * Designed to be testable (component names).
 */
public class EmployeeListPanel extends JPanel {

    private final Controller controller;
    private final DefaultListModel<Employee> listModel = new DefaultListModel<>();
    private final JList<Employee> list = new JList<>(listModel);

    public EmployeeListPanel(Controller controller) {
        this.controller = controller;
        setLayout(new BorderLayout(6,6));
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer((l, value, index, isSelected, cellHasFocus) -> {
            JLabel lbl = new JLabel(value.getName() + " (" + value.getEmployeeId() + ")");
            if (isSelected) lbl.setOpaque(true);
            return lbl;
        });

        list.setName("employeeList");
        JScrollPane sp = new JScrollPane(list);
        sp.setPreferredSize(new Dimension(220, 300));
        add(sp, BorderLayout.CENTER);

        // load initial employees (if any)
        refresh();
    }

    public void refresh() {
        List<Employee> all = controller.findAllEmployees();
        listModel.clear();
        all.forEach(listModel::addElement);
    }

    public Optional<Employee> getSelectedEmployee() {
        return Optional.ofNullable(list.getSelectedValue());
    }

    public void onSelection(Consumer<Employee> consumer) {
        list.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Employee sel = list.getSelectedValue();
                if (sel != null) consumer.accept(sel);
            }
        });
    }

    public void selectEmployeeById(String id) {
        for (int i = 0; i < listModel.size(); i++) {
            if (listModel.get(i).getEmployeeId().equals(id)) {
                list.setSelectedIndex(i);
                list.ensureIndexIsVisible(i);
                return;
            }
        }
    }
}