package it.unifi.attsw.employee_shift_scheduler;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Objects;
import java.util.UUID;

/**
 * Shift value object.
 * Provides constructors accepting LocalDateTime (tests use LocalDateTime).
 */
public class Shift {

    private final String id;
    private final Instant start;
    private final Instant end;
    private final String notes;

    public Shift(String id, Instant start, Instant end, String notes) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("id required");
        validate(start, end);
        this.id = id;
        this.start = start;
        this.end = end;
        this.notes = notes == null ? "" : notes;
    }

    public Shift(String id, Instant start, Instant end) {
        this(id, start, end, "");
    }

    public Shift(Instant start, Instant end) {
        this(UUID.randomUUID().toString(), start, end, "");
    }

    // LocalDateTime constructors (use system default zone)
    public Shift(String id, LocalDateTime startLocal, LocalDateTime endLocal, String notes) {
        this(id, toInstant(startLocal), toInstant(endLocal), notes);
    }

    public Shift(LocalDateTime startLocal, LocalDateTime endLocal, String notes) {
        this(UUID.randomUUID().toString(), toInstant(startLocal), toInstant(endLocal), notes);
    }

    public Shift(LocalDateTime startLocal, LocalDateTime endLocal) {
        this(startLocal, endLocal, "");
    }

    private static Instant toInstant(LocalDateTime ldt) {
        Objects.requireNonNull(ldt, "LocalDateTime required");
        return ldt.atZone(ZoneId.systemDefault()).toInstant();
    }

    private static void validate(Instant s, Instant e) {
        if (s == null || e == null) throw new IllegalArgumentException("start/end required");
        if (!e.isAfter(s)) throw new IllegalArgumentException("end must be after start");
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
        Shift s = (Shift) o;
        return id.equals(s.id) && start.equals(s.start) && end.equals(s.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, start, end);
    }

    @Override
    public String toString() {
        return "Shift{" + id + " " + start + "->" + end + "}";
    }
}
