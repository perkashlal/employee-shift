package it.unifi.attsw.employee_shift_scheduler_gui;

import it.unifi.attsw.employee_shift_scheduler.Shift;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Failing-first tests for ShiftsTableModel.
 *
 * - columnNames_shouldReportUtcSuffix is intentionally failing-first:
 *   it expects "Start (UTC)" and "End (UTC)" which the current model does not provide.
 *
 * - setShifts_shouldUpdateRowCount is expected to pass once model.setShifts is implemented.
 */
class ShiftsTableModelTest {

    @Test
    void columnNames_shouldReportUtcSuffix() {
        ShiftsTableModel model = new ShiftsTableModel();

        // EXPECTED (intentionally strict — will fail until you adapt model to include "(UTC)")
        assertEquals("Shift ID", model.getColumnName(0));
        assertEquals("Start (UTC)", model.getColumnName(1), "Expected column header to specify UTC");
        assertEquals("End (UTC)", model.getColumnName(2), "Expected column header to specify UTC");
    }

    @Test
    void setShifts_shouldUpdateRowCount() {
        ShiftsTableModel model = new ShiftsTableModel();

        Shift s1 = new Shift(LocalDateTime.of(2025, 11, 30, 8, 0),
                             LocalDateTime.of(2025, 11, 30, 12, 0));
        Shift s2 = new Shift(LocalDateTime.of(2025, 11, 30, 13, 0),
                             LocalDateTime.of(2025, 11, 30, 17, 0));

        assertEquals(0, model.getRowCount(), "Model should start empty");
        model.setShifts(List.of(s1, s2));
        assertEquals(2, model.getRowCount(), "Model should contain two shifts after setShifts");
    }
}
