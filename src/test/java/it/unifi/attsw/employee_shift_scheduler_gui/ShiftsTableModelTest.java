package it.unifi.attsw.employee_shift_scheduler_gui;

import it.unifi.attsw.employee_shift_scheduler.Shift;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for ShiftsTableModel (GREEN).
 */
class ShiftsTableModelTest {

    @Test
    void columnNames_shouldMatchUtcHeaders() {
        ShiftsTableModel model = new ShiftsTableModel();

        assertEquals(3, model.getColumnCount(), "Expect 3 columns");
        assertEquals("Shift ID", model.getColumnName(0));
        assertEquals("Start (UTC)", model.getColumnName(1));
        assertEquals("End (UTC)", model.getColumnName(2));
    }

    @Test
    void setShifts_shouldUpdateRowCount() {
        ShiftsTableModel model = new ShiftsTableModel();

        Shift s1 = new Shift(LocalDateTime.of(2025,11,30,8,0), LocalDateTime.of(2025,11,30,12,0));
        Shift s2 = new Shift(LocalDateTime.of(2025,11,30,13,0), LocalDateTime.of(2025,11,30,17,0));

        assertEquals(0, model.getRowCount());
        model.setShifts(List.of(s1, s2));
        assertEquals(2, model.getRowCount());
    }

    @Test
    void getValueAt_shouldReturnCorrectShiftFields() {
        ShiftsTableModel model = new ShiftsTableModel();

        Shift shift = new Shift(LocalDateTime.of(2025,11,30,8,0), LocalDateTime.of(2025,11,30,12,0));
        model.setShifts(List.of(shift));

        assertEquals(1, model.getRowCount());
        assertEquals(shift.getId(), model.getValueAt(0, 0));
        assertEquals(shift.getStart().toString(), model.getValueAt(0, 1));
        assertEquals(shift.getEnd().toString(), model.getValueAt(0, 2));
    }
}
