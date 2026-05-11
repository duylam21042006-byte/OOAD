import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CalendarModel {
    // Danh sách tất cả cuộc hẹn trong bộ nhớ
    private final List<Appointment> appointments = new ArrayList<>();
    // Danh sách riêng cho các cuộc hẹn có reminder
    private final List<Appointment> reminders = new ArrayList<>();

    // Trả về danh sách cuộc hẹn theo ngày, sắp xếp theo thời gian bắt đầu
    public List<Appointment> getAppointmentsForDate(LocalDate date) {
        return appointments.stream()
                .filter(a -> a.getDate().equals(date))
                .sorted(Comparator.comparing(Appointment::getStartTime))
                .collect(Collectors.toList());
    }

    // Tìm cuộc hẹn đang xung đột thời gian với appointment mới
    public Optional<Appointment> findConflict(Appointment appointment) {
        return appointments.stream()
                .filter(a -> a.overlapsWith(appointment))
                .findFirst();
    }

    // Tìm cuộc họp nhóm trùng tên, cùng ngày và cùng thời lượng
    public Optional<Appointment> findGroupMeetingMatch(Appointment appointment) {
        return appointments.stream()
                .filter(a -> a.isSameGroupMeetingCandidate(appointment))
                .findFirst();
    }

    // Thêm cuộc hẹn mới vào bộ nhớ và lưu vào file
    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        if (appointment.isReminder()) {
            reminders.add(appointment);
        }
        DatabaseUtil.saveAppointment(appointment);
    }

    // Thêm cuộc hẹn chỉ từ database, không ghi lại file lần nữa
    public void addAppointmentFromDB(Appointment appointment) {
        appointments.add(appointment);
        if (appointment.isReminder()) {
            reminders.add(appointment);
        }
    }

    // Thay thế cuộc hẹn cũ bằng cuộc hẹn mới và cập nhật file dữ liệu
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

    // Tìm giờ khả dụng tiếp theo trong ngày nếu khoảng thời gian hiện tại bị xung đột
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
