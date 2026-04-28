import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

public class AddAppointmentDialog extends JDialog {
    private final CalendarModel model;
    private final LocalDate selectedDate;
    private boolean appointmentAdded;

    private JTextField titleField;
    private JTextField locationField;
    private JSpinner dateSpinner;
    private JSpinner startTimeSpinner;
    private JSpinner endTimeSpinner;
    private JCheckBox reminderCheckBox;
    private JCheckBox groupMeetingCheckBox;

    public AddAppointmentDialog(Frame owner, CalendarModel model, LocalDate selectedDate) {
        super(owner, "Add Calendar Appointment", true);
        this.model = model;
        this.selectedDate = selectedDate;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        buildUI();
        pack();
        setLocationRelativeTo(owner);
    }

    public boolean isAppointmentAdded() {
        return appointmentAdded;
    }

    private void buildUI() {
        setLayout(new BorderLayout(10, 10));
        JPanel inputPanel = new JPanel(new GridLayout(0, 2, 6, 6));

        titleField = new JTextField();
        locationField = new JTextField();
        dateSpinner = new JSpinner(new SpinnerDateModel(dateToDate(selectedDate), null, null, Calendar.DAY_OF_MONTH));
        startTimeSpinner = new JSpinner(new SpinnerDateModel(dateTimeToDate(selectedDate, LocalTime.of(9, 0)), null, null, Calendar.MINUTE));
        endTimeSpinner = new JSpinner(new SpinnerDateModel(dateTimeToDate(selectedDate, LocalTime.of(10, 0)), null, null, Calendar.MINUTE));
        reminderCheckBox = new JCheckBox("Enable reminder");
        groupMeetingCheckBox = new JCheckBox("Group meeting");

        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        JSpinner.DateEditor startEditor = new JSpinner.DateEditor(startTimeSpinner, "HH:mm");
        startTimeSpinner.setEditor(startEditor);
        JSpinner.DateEditor endEditor = new JSpinner.DateEditor(endTimeSpinner, "HH:mm");
        endTimeSpinner.setEditor(endEditor);

        inputPanel.add(new JLabel("Appointment name:"));
        inputPanel.add(titleField);
        inputPanel.add(new JLabel("Location:"));
        inputPanel.add(locationField);
        inputPanel.add(new JLabel("Date:"));
        inputPanel.add(dateSpinner);
        inputPanel.add(new JLabel("Start time:"));
        inputPanel.add(startTimeSpinner);
        inputPanel.add(new JLabel("End time:"));
        inputPanel.add(endTimeSpinner);
        inputPanel.add(reminderCheckBox);
        inputPanel.add(groupMeetingCheckBox);

        add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton addButton = new JButton("Add");
        JButton cancelButton = new JButton("Cancel");

        addButton.addActionListener(this::onAddClicked);
        cancelButton.addActionListener(e -> dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void onAddClicked(ActionEvent event) {
        String title = titleField.getText().trim();
        String location = locationField.getText().trim();
        LocalDate appointmentDate = dateFromSpinner(dateSpinner);
        LocalTime startTime = timeFromSpinner(startTimeSpinner);
        LocalTime endTime = timeFromSpinner(endTimeSpinner);
        boolean reminder = reminderCheckBox.isSelected();
        boolean groupMeeting = groupMeetingCheckBox.isSelected();

        if (title.isEmpty()) {
            showError("Appointment name cannot be empty.");
            return;
        }

        if (!endTime.isAfter(startTime)) {
            showError("End time must be after start time.");
            return;
        }

        Appointment appointment = new Appointment(title, location.isEmpty() ? "No location" : location,
                appointmentDate, startTime, endTime, reminder,
                reminder ? "Reminder set for " + startTime.minusMinutes(15) : "", groupMeeting);

        Optional<Appointment> groupMatch = model.findGroupMeetingMatch(appointment);
        if (groupMatch.isPresent() && !appointment.isGroupMeeting()) {
            int joinResult = JOptionPane.showConfirmDialog(this,
                    "A group meeting already exists with the same name and duration. Do you want to join it instead?",
                    "Join Group Meeting",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (joinResult == JOptionPane.YES_OPTION) {
                groupMatch.get().addParticipant("Current user");
                appointmentAdded = true;
                dispose();
                return;
            }
        }

        Optional<Appointment> conflict = model.findConflict(appointment);
        if (conflict.isPresent()) {
            int replaceResult = JOptionPane.showConfirmDialog(this,
                    "This time slot conflicts with an existing appointment:\n" + conflict.get() + "\nDo you want to replace it?",
                    "Time Conflict",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (replaceResult != JOptionPane.YES_OPTION) {
                return;
            }
            model.replaceAppointment(conflict.get(), appointment);
            appointmentAdded = true;
            dispose();
            return;
        }

        model.addAppointment(appointment);
        appointmentAdded = true;
        dispose();
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid appointment", JOptionPane.ERROR_MESSAGE);
    }

    private static Date dateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Date dateTimeToDate(LocalDate date, LocalTime time) {
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    private static LocalDate dateFromSpinner(JSpinner spinner) {
        return Instant.ofEpochMilli(((Date) spinner.getValue()).getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private static LocalTime timeFromSpinner(JSpinner spinner) {
        return Instant.ofEpochMilli(((Date) spinner.getValue()).getTime()).atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }
}
