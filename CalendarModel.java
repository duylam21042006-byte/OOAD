import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CalendarModel {
    private final List<Appointment> appointments = new ArrayList<>();

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
    }

    public void replaceAppointment(Appointment oldAppointment, Appointment newAppointment) {
        appointments.remove(oldAppointment);
        appointments.add(newAppointment);
    }
}
