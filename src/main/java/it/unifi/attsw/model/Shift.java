package it.unifi.attsw.model;
import java.time.LocalDate;
import java.time.LocalTime;

public class Shift {
    private String shiftId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private String notes;

    public Shift() { }

    public Shift(String shiftId, LocalDate date, LocalTime startTime, LocalTime endTime, String notes) {
        this.shiftId = shiftId;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.notes = notes;
    }

    public String getShiftId() { return shiftId; }
    public void setShiftId(String shiftId) { this.shiftId = shiftId; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}