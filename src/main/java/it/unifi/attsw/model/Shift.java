package it.unifi.attsw.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Shift {

    private final String id;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String notes;

    // Primary constructor with explicit id
    public Shift(String id, LocalDateTime start, LocalDateTime end) {
        this(id, start, end, "");
    }

    public Shift(String id, LocalDateTime start, LocalDateTime end, String notes) {
        this.id = Objects.requireNonNull(id);
        this.start = Objects.requireNonNull(start);
        this.end = Objects.requireNonNull(end);
        if (end.isBefore(start) || end.isEqual(start)) {
            throw new IllegalArgumentException("Shift end must be after start");
        }
        this.notes = notes == null ? "" : notes;
    }

    // Convenience constructors used by tests that do not pass an id:
    public Shift(LocalDateTime start, LocalDateTime end) {
        this(UUID.randomUUID().toString(), start, end, "");
    }

    public Shift(LocalDateTime start, LocalDateTime end, String notes) {
        this(UUID.randomUUID().toString(), start, end, notes);
    }

    // getters
    public String getId() { return id; }

    // convenience alias because some tests call getShiftId()
    public String getShiftId() { return id; }

    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public String getNotes() { return notes; }

    public Duration duration() {
        return Duration.between(start, end);
    }

    /** Do this shift overlap with other? */
    public boolean overlaps(Shift other) {
        Objects.requireNonNull(other);
        // overlap when start < other.end && other.start < end
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }

    @Override
    public String toString() {
        return "Shift{" + "id='" + id + '\'' + ", start=" + start + ", end=" + end + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shift)) return false;
        Shift shift = (Shift) o;
        return id.equals(shift.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
