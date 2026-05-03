import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CalendarModel {
    private final List<Appointment> appointments = new ArrayList<>();
    private final List<Appointment> reminders = new ArrayList<>();

    public List<Appointment> getAppointmentsForDate(LocalDate date) {
        return appointments.stream()
                .filter(a -> a.getDate().equals(date))
                .sorted(Comparator.comparing(Appointment::getStartTime))
                .collect(Collectors.toList());
    }

    public Optional<Appointment> findConflict(Appointment appointment) {
        return appointments.stream()
                .filter(a -> a.overlapsWith(appointment))
                .findFirst();
    }

    public Optional<Appointment> findGroupMeetingMatch(Appointment appointment) {
        return appointments.stream()
                .filter(a -> a.isSameGroupMeetingCandidate(appointment))
                .findFirst();
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        if (appointment.isReminder()) {
            reminders.add(appointment);
        }
        DatabaseUtil.saveAppointment(appointment);
    }

    public void addAppointmentFromDB(Appointment appointment) {
        appointments.add(appointment);
        if (appointment.isReminder()) {
            reminders.add(appointment);
        }
    }

    public void replaceAppointment(Appointment oldAppointment, Appointment newAppointment) {
        appointments.remove(oldAppointment);
        if (oldAppointment.isReminder()) {
            reminders.remove(oldAppointment);
        }

        appointments.add(newAppointment);
        if (newAppointment.isReminder()) {
            reminders.add(newAppointment);
        }

        DatabaseUtil.deleteAppointment(oldAppointment);
        DatabaseUtil.saveAppointment(newAppointment);
    }

    public List<Appointment> getReminders() {
        return reminders;
    }

    public LocalTime findNextAvailableTime(LocalDate date, LocalTime requestedStart, java.time.Duration duration) {
        List<Appointment> dayAppts = getAppointmentsForDate(date);
        LocalTime currentTry = requestedStart;

        while (true) {
            LocalTime endTry = currentTry.plus(duration);
            
            if (endTry.isBefore(currentTry) || endTry.equals(LocalTime.MIDNIGHT)) {
                break;
            }

            Appointment temp = new Appointment("", "", date, currentTry, endTry, false, "", false);
            Optional<Appointment> conflict = dayAppts.stream()
                    .filter(a -> a.overlapsWith(temp))
                    .findFirst();

            if (conflict.isPresent()) {
                currentTry = conflict.get().getEndTime();
            } else {
                return currentTry;
            }
        }
        return null;
    }
}
