import javax.swing.*;
import javax.swing.border.EmptyBorder;
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
    private CalendarApp parent;
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

    public AddAppointmentDialog(CalendarApp parent, CalendarModel model, LocalDate selectedDate) {
        super(parent, "Add Calendar Appointment", true);
        this.parent = parent;
        this.model = model;
        this.selectedDate = selectedDate;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        getContentPane().setBackground(Color.WHITE);
        
        buildUI();
        pack();
        setLocationRelativeTo(parent);
    }

    // Kiểm tra xem cuộc hẹn đã được thêm thành công chưa
    public boolean isAppointmentAdded() {
        return appointmentAdded;
    }

    // Xây dựng giao diện nhập liệu cho dialog
    private void buildUI() {
        setLayout(new BorderLayout());
        
        // Header trên cùng của dialog
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(59, 130, 246)); // blue-500
        headerPanel.setBorder(new EmptyBorder(20, 25, 20, 25));
        
        JLabel titleLabel = new JLabel("New Appointment");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        add(headerPanel, BorderLayout.NORTH);

        // Form nhập liệu bên trong dialog
        JPanel inputPanel = new JPanel(new GridBagLayout());
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(new EmptyBorder(25, 30, 25, 30));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(8, 10, 8, 10);
        
        Font labelFont = new Font("Segoe UI", Font.BOLD, 13);
        Font inputFont = new Font("Segoe UI", Font.PLAIN, 14);

        titleField = new JTextField(20);
        titleField.setFont(inputFont);
        titleField.setPreferredSize(new Dimension(0, 32));
        
        locationField = new JTextField(20);
        locationField.setFont(inputFont);
        locationField.setPreferredSize(new Dimension(0, 32));
        
        LocalTime defaultStart = LocalTime.now().plusHours(1).withMinute(0);
        LocalTime defaultEnd = defaultStart.plusHours(1);

        dateSpinner = new JSpinner(new SpinnerDateModel(dateToDate(selectedDate), null, null, Calendar.DAY_OF_MONTH));
        startTimeSpinner = new JSpinner(new SpinnerDateModel(dateTimeToDate(selectedDate, defaultStart), null, null, Calendar.MINUTE));
        endTimeSpinner = new JSpinner(new SpinnerDateModel(dateTimeToDate(selectedDate, defaultEnd), null, null, Calendar.MINUTE));
        
        setupSpinner(dateSpinner, "yyyy-MM-dd", inputFont);
        setupSpinner(startTimeSpinner, "HH:mm", inputFont);
        setupSpinner(endTimeSpinner, "HH:mm", inputFont);

        reminderCheckBox = new JCheckBox("Enable Reminder");
        reminderCheckBox.setFont(inputFont);
        reminderCheckBox.setBackground(Color.WHITE);
        
        groupMeetingCheckBox = new JCheckBox("Group Meeting");
        groupMeetingCheckBox.setFont(inputFont);
        groupMeetingCheckBox.setBackground(Color.WHITE);

        int row = 0;
        
        // Tiêu đề
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        inputPanel.add(createStyledLabel("Title:", labelFont), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        inputPanel.add(titleField, gbc);
        
        row++;
        // Địa điểm
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        inputPanel.add(createStyledLabel("Location:", labelFont), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        inputPanel.add(locationField, gbc);
        
        row++;
        // Ngày hẹn
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        inputPanel.add(createStyledLabel("Date:", labelFont), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        inputPanel.add(dateSpinner, gbc);
        
        row++;
        // Giờ bắt đầu
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        inputPanel.add(createStyledLabel("Start Time:", labelFont), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        inputPanel.add(startTimeSpinner, gbc);
        
        row++;
        // Giờ kết thúc
        gbc.gridx = 0; gbc.gridy = row; gbc.weightx = 0.0;
        inputPanel.add(createStyledLabel("End Time:", labelFont), gbc);
        gbc.gridx = 1; gbc.gridy = row; gbc.weightx = 1.0;
        inputPanel.add(endTimeSpinner, gbc);
        
        row++;
        // Checkboxes bổ sung
        JPanel checkPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        checkPanel.setBackground(Color.WHITE);
        checkPanel.add(reminderCheckBox);
        checkPanel.add(Box.createRigidArea(new Dimension(20, 0)));
        checkPanel.add(groupMeetingCheckBox);
        
        gbc.gridx = 1; gbc.gridy = row;
        inputPanel.add(checkPanel, gbc);

        add(inputPanel, BorderLayout.CENTER);

        // Nút lưu / hủy
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(new Color(249, 250, 251)); // gray-50
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(229, 231, 235)));
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        cancelButton.setPreferredSize(new Dimension(90, 35));
        styleButton(cancelButton, new Color(229, 231, 235), new Color(209, 213, 219), new Color(17, 24, 39));
        cancelButton.addActionListener(e -> dispose());
        
        JButton addButton = new JButton("Save");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        addButton.setPreferredSize(new Dimension(90, 35));
        styleButton(addButton, new Color(59, 130, 246), new Color(37, 99, 235), Color.WHITE);
        addButton.addActionListener(this::onSave);

        buttonPanel.add(cancelButton);
        buttonPanel.add(addButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    // Tạo label với font và màu sắc chuẩn
    private JLabel createStyledLabel(String text, Font font) {
        JLabel label = new JLabel(text);
        label.setFont(font);
        label.setForeground(new Color(75, 85, 99)); // gray-600
        return label;
    }

    // Tùy biến giao diện cho nút bấm
    private void styleButton(JButton button, Color bg, Color hoverBg, Color fg) {
        button.setBackground(bg);
        button.setForeground(fg);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setOpaque(true);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(hoverBg);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(bg);
            }
        });
    }
    
    // Cấu hình JSpinner với định dạng ngày/giờ
    private void setupSpinner(JSpinner spinner, String pattern, Font font) {
        JSpinner.DateEditor editor = new JSpinner.DateEditor(spinner, pattern);
        editor.getTextField().setFont(font);
        spinner.setEditor(editor);
        spinner.setPreferredSize(new Dimension(0, 32));
    }

    // Kiểm tra dữ liệu nhập liệu trước khi lưu
    public boolean validateInput() {
        String title = titleField.getText().trim();
        LocalTime startTime = timeFromSpinner(startTimeSpinner);
        LocalTime endTime = timeFromSpinner(endTimeSpinner);

        if (title.isEmpty()) {
            showError("Appointment title cannot be empty.");
            return false;
        }

        if (!endTime.isAfter(startTime)) {
            showError("End time must be after start time.");
            return false;
        }
        return true;
    }

    // Xử lý sự kiện lưu cuộc hẹn
    private void onSave(ActionEvent event) {
        if (!validateInput()) {
            return;
        }

        String title = titleField.getText().trim();
        String location = locationField.getText().trim();
        LocalDate appointmentDate = dateFromSpinner(dateSpinner);
        LocalTime startTime = timeFromSpinner(startTimeSpinner);
        LocalTime endTime = timeFromSpinner(endTimeSpinner);
        boolean reminder = reminderCheckBox.isSelected();
        boolean groupMeeting = groupMeetingCheckBox.isSelected();

        Appointment appointment = new Appointment(title, location.isEmpty() ? "No location" : location,
                appointmentDate, startTime, endTime, reminder,
                reminder ? "Reminder set for " + startTime.minusMinutes(15) : "", groupMeeting);

        Optional<Appointment> groupMatch = model.findGroupMeetingMatch(appointment);
        if (groupMatch.isPresent()) {
            int joinResult = JOptionPane.showConfirmDialog(this,
                    "A group meeting already exists with the same name and duration. Do you want to join it instead?",
                    "Join Group Meeting",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);
            if (joinResult == JOptionPane.YES_OPTION) {
                Appointment match = groupMatch.get();
                match.addParticipant("Current user");
                DatabaseUtil.updateAppointment(match);
                appointmentAdded = true;
                dispose();
                return;
            }
        }

        Optional<Appointment> conflict = model.findConflict(appointment);
        if (conflict.isPresent()) {
            Object[] options = {"Replace", "Available Time", "Cancel"};
            int result = JOptionPane.showOptionDialog(this,
                    "This time slot conflicts with an existing appointment:\n" + conflict.get().getTitle() + "\nWhat do you want to do?",
                    "Time Conflict",
                    JOptionPane.YES_NO_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE,
                    null,
                    options,
                    options[0]);

            if (result == 0) { // Replace
                model.replaceAppointment(conflict.get(), appointment);
                appointmentAdded = true;
                dispose();
            } else if (result == 1) { // Available Time
                LocalTime newStartTime = model.findNextAvailableTime(appointmentDate, startTime, java.time.Duration.between(startTime, endTime));
                if (newStartTime != null) {
                    startTimeSpinner.setValue(dateTimeToDate(appointmentDate, newStartTime));
                    endTimeSpinner.setValue(dateTimeToDate(appointmentDate, newStartTime.plusMinutes(java.time.Duration.between(startTime, endTime).toMinutes())));
                    JOptionPane.showMessageDialog(this, "Suggested available time: " + newStartTime, "Available Time Found", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "No available time found on this day.", "No Time", JOptionPane.ERROR_MESSAGE);
                }
            }
            return;
        }

        model.addAppointment(appointment);
        appointmentAdded = true;
        dispose();
    }

    // Hiển thị thông báo lỗi nhập liệu
    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Invalid input", JOptionPane.ERROR_MESSAGE);
    }

    // Chuyển LocalDate sang java.util.Date để dùng với SpinnerDateModel
    private static Date dateToDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    // Chuyển LocalDate và LocalTime sang java.util.Date để dùng với spinner
    private static Date dateTimeToDate(LocalDate date, LocalTime time) {
        LocalDateTime dateTime = LocalDateTime.of(date, time);
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    // Đọc ngày từ spinner
    private static LocalDate dateFromSpinner(JSpinner spinner) {
        return Instant.ofEpochMilli(((Date) spinner.getValue()).getTime()).atZone(ZoneId.systemDefault()).toLocalDate();
    }

    // Đọc giờ từ spinner
    private static LocalTime timeFromSpinner(JSpinner spinner) {
        return Instant.ofEpochMilli(((Date) spinner.getValue()).getTime()).atZone(ZoneId.systemDefault()).toLocalTime().withSecond(0).withNano(0);
    }
}
