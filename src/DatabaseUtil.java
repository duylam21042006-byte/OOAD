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
        appointments.removeIf(a -> a.title.equals(appointment.getTitle()) && 
                              a.date.equals(appointment.getDate().toString()) &&
                              a.startTime.equals(appointment.getStartTime().toString()));
        saveAppointmentsToFile(appointments);
    }
    
    public static void updateAppointment(Appointment appointment) {
        List<SerializableAppointment> appointments = loadAppointmentsFromFile();
        for (int i = 0; i < appointments.size(); i++) {
            SerializableAppointment sa = appointments.get(i);
            if (sa.title.equals(appointment.getTitle()) && 
                sa.date.equals(appointment.getDate().toString()) &&
                sa.startTime.equals(appointment.getStartTime().toString())) {
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
    
    String title;
    String location;
    String date;
    String startTime;
    String endTime;
    boolean reminder;
    String reminderNote;
    boolean groupMeeting;
    
    public SerializableAppointment(Appointment a) {
        this.title = a.getTitle();
        this.location = a.getLocation();
        this.date = a.getDate().toString();
        this.startTime = a.getStartTime().toString();
        this.endTime = a.getEndTime().toString();
        this.reminder = a.isReminder();
        this.reminderNote = a.getReminderNote();
        this.groupMeeting = a.isGroupMeeting();
    }
    
    public Appointment toAppointment() {
        return new Appointment(
                title,
                location,
                LocalDate.parse(date),
                LocalTime.parse(startTime),
                LocalTime.parse(endTime),
                reminder,
                reminderNote,
                groupMeeting
        );
    }
}
