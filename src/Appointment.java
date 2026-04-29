import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class Appointment {
    private int id;
    private String title;
    private String location;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean reminder;
    private String reminderNote;
    private boolean groupMeeting;
    private List<String> participants;

    public Appointment(String title, String location, LocalDate date, LocalTime startTime, LocalTime endTime,
                       boolean reminder, String reminderNote, boolean groupMeeting) {
        this.id = -1; // Default for new appointments
        this.title = title;
        this.location = location;
        this.date = date;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reminder = reminder;
        this.reminderNote = reminderNote;
        this.groupMeeting = groupMeeting;
        this.participants = new ArrayList<>();
    }

    public String getTitle() {
        return title;
    }

    public String getLocation() {
        return location;
    }

    public LocalDate getDate() {
        return date;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean isReminder() {
        return reminder;
    }

    public boolean hasReminder() {
        return reminder;
    }

    public String getReminderNote() {
        return reminderNote;
    }

    public boolean isGroupMeeting() {
        return groupMeeting;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDurationMinutes() {
        return (int) Duration.between(startTime, endTime).toMinutes();
    }

    public boolean overlapsWith(Appointment other) {
        if (!date.equals(other.date)) {
            return false;
        }
        return !startTime.isAfter(other.endTime) && !endTime.isBefore(other.startTime);
    }

    public boolean isSameGroupMeetingCandidate(Appointment other) {
        return other.isGroupMeeting()
                && title.equalsIgnoreCase(other.title)
                && getDurationMinutes() == other.getDurationMinutes();
    }

    public void addParticipant(String participantName) {
        if (participantName != null && !participantName.isBlank()) {
            participants.add(participantName);
        }
    }

    @Override
    public String toString() {
        String timeRange = startTime + " - " + endTime;
        String reminderText = reminder ? " [Reminder]" : "";
        String groupText = groupMeeting ? " [Group]" : "";
        String participantText = groupMeeting && !participants.isEmpty()
                ? " participants=" + participants.size()
                : "";
        return String.format("%s @ %s %s%s%s%s",
                title, location, timeRange, reminderText, groupText, participantText);
    }
}
