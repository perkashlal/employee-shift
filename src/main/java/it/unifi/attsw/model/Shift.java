package it.unifi.attsw.model;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Shift {

    private final String id;
    private final LocalDateTime start;
    private final LocalDateTime end;
    private final String note;

    public Shift(LocalDateTime start, LocalDateTime end) {
        this(start, end, null);
    }

    public Shift(LocalDateTime start, LocalDateTime end, String note) {
        Objects.requireNonNull(start, "start must not be null");
        Objects.requireNonNull(end, "end must not be null");

        if (!end.isAfter(start)) {
            throw new IllegalArgumentException("Shift end must be after start");
        }

        this.id = UUID.randomUUID().toString();
        this.start = start;
        this.end = end;
        this.note = note == null ? "" : note;
    }

    public String getId() { return id; }
    public LocalDateTime getStart() { return start; }
    public LocalDateTime getEnd() { return end; }
    public String getNote() { return note; }

    public Duration duration() { return Duration.between(start, end); }

    public boolean overlaps(Shift other) {
        Objects.requireNonNull(other, "other must not be null");
        return this.start.isBefore(other.end) && this.end.isAfter(other.start);
    }

    @Override
    public String toString() {
        return "Shift{" +
                "id='" + id + '\'' +
                ", start=" + start +
                ", end=" + end +
                ", note='" + note + '\'' +
                '}';
    }
}