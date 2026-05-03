import java.io.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

public class DatabaseUtil {
    private static final String DATA_FILE = "appointments.dat";
    
    public static void initializeDatabase() {
        // Create file if it doesn't exist
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
    
    public static void loadAllAppointments(CalendarModel model) {
        File file = new File(DATA_FILE);
        if (!file.exists() || file.length() == 0) {
            return;
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            List<SerializableAppointment> appointments = (List<SerializableAppointment>) ois.readObject();
            
            for (SerializableAppointment sa : appointments) {
                Appointment appointment = sa.toAppointment();
                model.addAppointmentFromDB(appointment);
            }
            
            System.out.println("Loaded " + appointments.size() + " appointments from database");
            
        } catch (EOFException e) {
            // Empty file, ignore
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error loading appointments: " + e.getMessage());
        }
    }
    
    public static void saveAppointment(Appointment appointment) {
        List<SerializableAppointment> appointments = loadAppointmentsFromFile();
        SerializableAppointment sa = new SerializableAppointment(appointment);
        appointments.add(sa);
        saveAppointmentsToFile(appointments);
    }
    
    public static void deleteAppointment(Appointment appointment) {
        List<SerializableAppointment> appointments = loadAppointmentsFromFile();
        appointments.removeIf(a -> {
            Appointment appt = a.toAppointment();
            return appt.getTitle().equals(appointment.getTitle()) && 
                   appt.getDate().equals(appointment.getDate()) &&
                   appt.getStartTime().equals(appointment.getStartTime());
        });
        saveAppointmentsToFile(appointments);
    }
    
    public static void updateAppointment(Appointment appointment) {
        List<SerializableAppointment> appointments = loadAppointmentsFromFile();
        for (int i = 0; i < appointments.size(); i++) {
            Appointment appt = appointments.get(i).toAppointment();
            if (appt.getTitle().equals(appointment.getTitle()) && 
                appt.getDate().equals(appointment.getDate()) &&
                appt.getStartTime().equals(appointment.getStartTime())) {
                appointments.set(i, new SerializableAppointment(appointment));
                break;
            }
        }
        saveAppointmentsToFile(appointments);
    }
    
    private static List<SerializableAppointment> loadAppointmentsFromFile() {
        File file = new File(DATA_FILE);
        if (!file.exists() || file.length() == 0) {
            return new ArrayList<>();
        }
        
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            List<SerializableAppointment> appointments = (List<SerializableAppointment>) ois.readObject();
            return appointments;
        } catch (EOFException e) {
            return new ArrayList<>();
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("Error reading file: " + e.getMessage());
            return new ArrayList<>();
        }
    }
    
    private static void saveAppointmentsToFile(List<SerializableAppointment> appointments) {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(appointments);
            oos.flush();
        } catch (IOException e) {
            System.err.println("Error saving appointments: " + e.getMessage());
        }
    }
}

// Serializable wrapper class
class SerializableAppointment implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String title;
    private String location;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean reminder;
    private String reminderNote;
    private boolean groupMeeting;
    private List<String> participants;
    
    public SerializableAppointment(Appointment a) {
        this.title = a.getTitle();
        this.location = a.getLocation();
        this.date = a.getDate();
        this.startTime = a.getStartTime();
        this.endTime = a.getEndTime();
        this.reminder = a.isReminder();
        this.reminderNote = a.getReminderNote();
        this.groupMeeting = a.isGroupMeeting();
        this.participants = new ArrayList<>(a.getParticipants());
    }
    
    public Appointment toAppointment() {
        Appointment a = new Appointment(
                title,
                location,
                date,
                startTime,
                endTime,
                reminder,
                reminderNote,
                groupMeeting
        );
        if (participants != null) {
            for(String p : participants) {
                a.addParticipant(p);
            }
        }
        return a;
    }
}
