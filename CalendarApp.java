import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class CalendarApp extends JFrame {
    private final CalendarModel model;
    private final CalendarPanel calendarPanel;
    private final DefaultListModel<String> appointmentListModel;
    private JLabel selectedDateLabel;
    private LocalDate currentSelectedDate;

    public CalendarApp() {
        model = new CalendarModel();
        currentSelectedDate = LocalDate.now();
        
        setTitle("Calendar Appointment Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(10, 10));
        
        appointmentListModel = new DefaultListModel<>();
        calendarPanel = new CalendarPanel(model, currentSelectedDate, this::onDateSelected);
        
        buildUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        topPanel.setBackground(Color.WHITE);
        
        JPanel datePickerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        datePickerPanel.setBackground(Color.WHITE);
        
        JSpinner daySpinner = new JSpinner(new SpinnerNumberModel(currentSelectedDate.getDayOfMonth(), 1, 31, 1));
        JSpinner monthSpinner = new JSpinner(new SpinnerNumberModel(currentSelectedDate.getMonthValue(), 1, 12, 1));
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(currentSelectedDate.getYear(), 1900, 2100, 1));
        
        daySpinner.setPreferredSize(new Dimension(50, 25));
        monthSpinner.setPreferredSize(new Dimension(50, 25));
        yearSpinner.setPreferredSize(new Dimension(70, 25));
        
        JButton goButton = new JButton("Go");
        goButton.addActionListener(e -> {
            try {
                int day = (int) daySpinner.getValue();
                int month = (int) monthSpinner.getValue();
                int year = (int) yearSpinner.getValue();
                LocalDate selectedDate = LocalDate.of(year, month, day);
                currentSelectedDate = selectedDate;
                calendarPanel.setCurrentMonth(YearMonth.from(selectedDate));
                onDateSelected();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Invalid date", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        datePickerPanel.add(new JLabel("Day:"));
        datePickerPanel.add(daySpinner);
        datePickerPanel.add(new JLabel("Month:"));
        datePickerPanel.add(monthSpinner);
        datePickerPanel.add(new JLabel("Year:"));
        datePickerPanel.add(yearSpinner);
        datePickerPanel.add(goButton);
        
        topPanel.add(datePickerPanel, BorderLayout.WEST);
        
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        navigationPanel.setBackground(Color.WHITE);
        
        JButton prevButton = new JButton("<");
        prevButton.addActionListener(e -> {
            YearMonth prev = calendarPanel.getCurrentMonth().minusMonths(1);
            calendarPanel.setCurrentMonth(prev);
        });
        
        JButton nextButton = new JButton(">");
        nextButton.addActionListener(e -> {
            YearMonth next = calendarPanel.getCurrentMonth().plusMonths(1);
            calendarPanel.setCurrentMonth(next);
        });
        
        JButton todayButton = new JButton("Today");
        todayButton.addActionListener(e -> {
            LocalDate today = LocalDate.now();
            calendarPanel.setCurrentMonth(YearMonth.from(today));
            currentSelectedDate = today;
            calendarPanel.setSelectedDate(today);
            daySpinner.setValue(today.getDayOfMonth());
            monthSpinner.setValue(today.getMonthValue());
            yearSpinner.setValue(today.getYear());
            onDateSelected();
        });
        
        navigationPanel.add(prevButton);
        navigationPanel.add(nextButton);
        navigationPanel.add(new JSeparator(JSeparator.VERTICAL) {
            @Override
            public Dimension getPreferredSize() {
                return new Dimension(1, 25);
            }
        });
        navigationPanel.add(todayButton);
        
        topPanel.add(navigationPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.NORTH);
        add(calendarPanel, BorderLayout.CENTER);
        
        JPanel sidePanel = new JPanel(new BorderLayout(10, 10));
        sidePanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        sidePanel.setBackground(new Color(245, 245, 245));
        
        selectedDateLabel = new JLabel(formatDate(currentSelectedDate));
        selectedDateLabel.setFont(new Font("Arial", Font.BOLD, 16));
        sidePanel.add(selectedDateLabel, BorderLayout.NORTH);
        
        JList<String> appointmentList = new JList<>(appointmentListModel);
        appointmentList.setBackground(new Color(255, 255, 255));
        appointmentList.setFont(new Font("Arial", Font.PLAIN, 12));
        appointmentList.setFixedCellHeight(40);
        
        sidePanel.add(new JScrollPane(appointmentList), BorderLayout.CENTER);
        
        JButton addButton = new JButton("+ Add appointment");
        addButton.setFont(new Font("Arial", Font.PLAIN, 12));
        addButton.setBackground(new Color(63, 81, 181));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> openAddDialog());
        sidePanel.add(addButton, BorderLayout.SOUTH);
        
        add(sidePanel, BorderLayout.EAST);
    }

    private void onDateSelected() {
        calendarPanel.setSelectedDate(currentSelectedDate);
        selectedDateLabel.setText(formatDate(currentSelectedDate));
        refreshAppointments();
        calendarPanel.repaint();
    }

    private void onDateSelected(LocalDate selectedDate) {
        System.out.println("onDateSelected callback: " + selectedDate);
        currentSelectedDate = selectedDate;
        calendarPanel.setSelectedDate(selectedDate);
        selectedDateLabel.setText(formatDate(selectedDate));
        refreshAppointments();
    }

    private void openAddDialog() {
        AddAppointmentDialog dialog = new AddAppointmentDialog(this, model, currentSelectedDate);
        dialog.setVisible(true);
        if (dialog.isAppointmentAdded()) {
            refreshAppointments();
            calendarPanel.repaint();
        }
    }

    private void refreshAppointments() {
        appointmentListModel.clear();
        model.getAppointmentsForDate(currentSelectedDate).forEach(appointment -> {
            String displayText = String.format("%s %s %s - %s",
                    appointment.getStartTime(),
                    appointment.getTitle(),
                    appointment.getLocation(),
                    appointment.getEndTime());
            appointmentListModel.addElement(displayText);
        });
        if (appointmentListModel.isEmpty()) {
            appointmentListModel.addElement("No appointments");
        }
    }

    private String formatDate(LocalDate date) {
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] monthNames = {"January", "February", "March", "April", "May", "June", 
                             "July", "August", "September", "October", "November", "December"};
        return dayNames[date.getDayOfWeek().getValue() % 7] + ", " + 
               monthNames[date.getMonthValue() - 1] + " " + date.getDayOfMonth();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalendarApp());
    }
}
