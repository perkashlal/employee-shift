package it.unifi.attsw.employee_shift_scheduler_gui;

import it.unifi.attsw.employee_shift_scheduler.Shift;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * Table model backing the JTable inside the SchedulerFrame.
 */
public class ShiftsTableModel extends AbstractTableModel {

    private final List<Shift> shifts = new ArrayList<>();
    private final String[] columns = {"Shift ID", "Start", "End"};

    public void setShifts(List<Shift> newShifts) {
        shifts.clear();
        if (newShifts != null)
            shifts.addAll(newShifts);

        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return shifts.size();
    }

    @Override
    public int getColumnCount() {
        return columns.length;
    }

    @Override
    public String getColumnName(int index) {
        return columns[index];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Shift s = shifts.get(rowIndex);

        switch (columnIndex) {
            case 0: return s.getId();
            case 1: return s.getStart().toString();
            case 2: return s.getEnd().toString();
            default: return "";
        }
    }
}
