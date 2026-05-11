import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {
    private static final String DATA_FILE = "appointments.dat";
    
    // Tạo file dữ liệu nếu chưa tồn tại
    public static void initializeDatabase() {
        File file = new File(DATA_FILE);
        if (!file.exists()) {
            try {
                file.createNewFile();
                System.out.println("Database file created: " + DATA_FILE);
            } catch (IOException e) {
                System.err.println("Error creating database file: " + e.getMessage());
            }
        }
    }
    
    // Tải tất cả cuộc hẹn từ file vào CalendarModel
    public static void loadAllAppointments(CalendarModel model) {
        File file = new File(DATA_FILE);
        if (!file.exists() || file.length() == 0) {
            return;
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            List<Appointment> appointments = parseAppointmentsFromText(reader);
            
            for (Appointment appointment : appointments) {
                model.addAppointmentFromDB(appointment);
            }
            
            System.out.println("Loaded " + appointments.size() + " appointments from database");
            
        } catch (IOException e) {
            System.err.println("Error loading appointments: " + e.getMessage());
        }
    }
    
    // Lưu cuộc hẹn mới vào file dữ liệu
    public static void saveAppointment(Appointment appointment) {
        List<Appointment> appointments = loadAppointmentsFromFile();
        appointments.add(appointment);
        saveAppointmentsToFile(appointments);
    }
    
    // Xóa cuộc hẹn khỏi file dựa trên tiêu chí tiêu đề, ngày và giờ bắt đầu
    public static void deleteAppointment(Appointment appointment) {
        List<Appointment> appointments = loadAppointmentsFromFile();
        appointments.removeIf(a -> 
            a.getTitle().equals(appointment.getTitle()) && 
            a.getDate().equals(appointment.getDate()) &&
            a.getStartTime().equals(appointment.getStartTime())
        );
        saveAppointmentsToFile(appointments);
    }
    
    // Cập nhật thông tin cuộc hẹn trong file nếu đã tồn tại cùng tiêu đề, ngày và giờ bắt đầu
    public static void updateAppointment(Appointment appointment) {
        List<Appointment> appointments = loadAppointmentsFromFile();
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appt = appointments.get(i);
            if (appt.getTitle().equals(appointment.getTitle()) && 
                appt.getDate().equals(appointment.getDate()) &&
                appt.getStartTime().equals(appointment.getStartTime())) {
                appointments.set(i, appointment);
                break;
            }
        }
        saveAppointmentsToFile(appointments);
    }
    
    // Đọc danh sách các cuộc hẹn đã lưu từ file
    private static List<Appointment> loadAppointmentsFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            return parseAppointmentsFromText(reader);
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    // Ghi toàn bộ danh sách cuộc hẹn vào file dưới dạng text dễ đọc
    private static void saveAppointmentsToFile(List<Appointment> appointments) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(DATA_FILE))) {
            for (Appointment appointment : appointments) {
                writeAppointmentToText(writer, appointment);
                writer.write("---\n");
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving appointments: " + e.getMessage());
        }
    }
    
    // Parse danh sách appointments từ text
    private static List<Appointment> parseAppointmentsFromText(BufferedReader reader) throws IOException {
        List<Appointment> appointments = new ArrayList<>();
        String line;
        StringBuilder appointmentText = new StringBuilder();
        
        while ((line = reader.readLine()) != null) {
            if (line.equals("---")) {
                if (appointmentText.length() > 0) {
                    Appointment appointment = parseAppointmentFromText(appointmentText.toString());
                    if (appointment != null) {
                        appointments.add(appointment);
                    }
                    appointmentText = new StringBuilder();
                }
            } else {
                appointmentText.append(line).append("\n");
            }
        }
        
        // Parse the last appointment if no trailing ---
        if (appointmentText.length() > 0) {
            Appointment appointment = parseAppointmentFromText(appointmentText.toString());
            if (appointment != null) {
                appointments.add(appointment);
            }
        }
        
        return appointments;
    }
    
    // Parse một appointment từ text
    private static Appointment parseAppointmentFromText(String text) {
        String[] lines = text.split("\n");
        String title = null, location = null, reminderNote = null;
        LocalDate date = null;
        LocalTime startTime = null, endTime = null;
        boolean reminder = false, groupMeeting = false;
        List<String> participants = new ArrayList<>();
        
        for (String line : lines) {
            if (line.startsWith("Title: ")) {
                title = line.substring(7);
            } else if (line.startsWith("Location: ")) {
                location = line.substring(10);
            } else if (line.startsWith("Date: ")) {
                date = LocalDate.parse(line.substring(6));
            } else if (line.startsWith("Start Time: ")) {
                startTime = LocalTime.parse(line.substring(12));
            } else if (line.startsWith("End Time: ")) {
                endTime = LocalTime.parse(line.substring(10));
            } else if (line.startsWith("Reminder: ")) {
                reminder = Boolean.parseBoolean(line.substring(10));
            } else if (line.startsWith("Reminder Note: ")) {
                reminderNote = line.substring(15);
            } else if (line.startsWith("Group Meeting: ")) {
                groupMeeting = Boolean.parseBoolean(line.substring(15));
            } else if (line.startsWith("Participants: ")) {
                String parts = line.substring(14);
                if (!parts.isEmpty()) {
                    String[] participantArray = parts.split(", ");
                    for (String p : participantArray) {
                        participants.add(p);
                    }
                }
            }
        }
        
        if (title != null && date != null && startTime != null && endTime != null) {
            Appointment appointment = new Appointment(title, location, date, startTime, endTime, reminder, reminderNote, groupMeeting);
            for (String p : participants) {
                appointment.addParticipant(p);
            }
            return appointment;
        }
        return null;
    }
    
    // Ghi một appointment ra text
    private static void writeAppointmentToText(BufferedWriter writer, Appointment appointment) throws IOException {
        writer.write("Title: " + appointment.getTitle() + "\n");
        writer.write("Location: " + (appointment.getLocation() != null ? appointment.getLocation() : "") + "\n");
        writer.write("Date: " + appointment.getDate() + "\n");
        writer.write("Start Time: " + appointment.getStartTime() + "\n");
        writer.write("End Time: " + appointment.getEndTime() + "\n");
        writer.write("Reminder: " + appointment.isReminder() + "\n");
        writer.write("Reminder Note: " + (appointment.getReminderNote() != null ? appointment.getReminderNote() : "") + "\n");
        writer.write("Group Meeting: " + appointment.isGroupMeeting() + "\n");
        writer.write("Participants: " + String.join(", ", appointment.getParticipants()) + "\n");
    }
}
