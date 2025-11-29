package it.unifi.attsw.employee_shift_scheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Objects;
import java.util.UUID;

public class Shift {

    private final String id;
    private final Instant start;
    private final Instant end;
    private final String notes;

    // Primary constructor: accepts Instant values
    public Shift(String id, Instant start, Instant end, String notes) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id must be provided");
        validateStartEnd(start, end);
        this.id = id;
        this.start = start;
        this.end = end;
        this.notes = notes == null ? "" : notes;
    }

    // Convenience: Instant + no notes
    public Shift(String id, Instant start, Instant end) {
        this(id, start, end, "");
    }

    // Convenience: generate id
    public Shift(Instant start, Instant end, String notes) {
        this(UUID.randomUUID().toString(), start, end, notes);
    }

    public Shift(Instant start, Instant end) {
        this(start, end, "");
    }

    // NEW: constructors that accept LocalDateTime (tests use these)
    // Convert LocalDateTime -> Instant using UTC offset
    public Shift(String id, LocalDateTime startLocal, LocalDateTime endLocal, String notes) {
        this(id, toInstantUtc(startLocal), toInstantUtc(endLocal), notes);
    }

    public Shift(LocalDateTime startLocal, LocalDateTime endLocal, String notes) {
        this(UUID.randomUUID().toString(), startLocal, endLocal, notes);
    }

    public Shift(LocalDateTime startLocal, LocalDateTime endLocal) {
        this(startLocal, endLocal, "");
    }

    private static Instant toInstantUtc(LocalDateTime ldt) {
        if (ldt == null) throw new IllegalArgumentException("LocalDateTime must not be null");
        return ldt.toInstant(ZoneOffset.UTC);
    }

    private static void validateStartEnd(Instant start, Instant end) {
        if (start == null || end == null) throw new IllegalArgumentException("start/end required");
        if (!end.isAfter(start)) throw new IllegalArgumentException("end must be after start");
    }

    public String getId() { return id; }
    public Instant getStart() { return start; }
    public Instant getEnd() { return end; }
    public String getNotes() { return notes; }

    public Duration duration() { return Duration.between(start, end); }

    public boolean overlaps(Shift other) {
        Objects.requireNonNull(other, "other");
        return this.start.isBefore(other.end) && other.start.isBefore(this.end);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shift)) return false;
        Shift shift = (Shift) o;
        return id.equals(shift.id) && start.equals(shift.start) && end.equals(shift.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, start, end);
    }

    @Override
    public String toString() {
        return "Shift{" + "id='" + id + '\'' + ", start=" + start + ", end=" + end + '}';
    }
}
