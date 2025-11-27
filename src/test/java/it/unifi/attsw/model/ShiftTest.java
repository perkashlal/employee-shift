package it.unifi.attsw.model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class ShiftTest {

    @Test
    void createShift_withEndBeforeStart_shouldThrow() {
        LocalDateTime start = LocalDateTime.of(2025, 11, 26, 10, 0);
        LocalDateTime end = LocalDateTime.of(2025, 11, 26, 9, 0);
        assertThrows(IllegalArgumentException.class, () -> new Shift(start, end));
    }

    @Test
    void overlapDetection_shouldDetectOverlap() {
        Shift s1 = new Shift(LocalDateTime.of(2025,11,26,9,0),
                             LocalDateTime.of(2025,11,26,13,0));
        Shift s2 = new Shift(LocalDateTime.of(2025,11,26,12,0),
                             LocalDateTime.of(2025,11,26,16,0));
        assertTrue(s1.overlaps(s2));
    }

    @Test
    void nonOverlapping_shiftsShouldNotOverlap() {
        Shift s1 = new Shift(LocalDateTime.of(2025,11,26,8,0),
                             LocalDateTime.of(2025,11,26,10,0));
        Shift s2 = new Shift(LocalDateTime.of(2025,11,26,10,0),
                             LocalDateTime.of(2025,11,26,12,0));
        assertFalse(s1.overlaps(s2));
    }
}