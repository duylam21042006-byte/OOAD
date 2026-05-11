import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.time.LocalDate;
import java.time.YearMonth;

public class CalendarApp extends JFrame {
    // Mô hình dữ liệu chứa danh sách cuộc hẹn
    private final CalendarModel model;
    // Bảng lịch hiển thị tháng và ngày
    private final CalendarPanel calendarPanel;
    // Model cho danh sách cuộc hẹn trong sidebar
    private final DefaultListModel<Appointment> appointmentListModel;
    private JLabel selectedDateLabel;
    private LocalDate currentSelectedDate;
    private JPanel emptyStatePanel;
    private JScrollPane listScrollPane;

    public CalendarApp() {
        // Thiết lập giao diện hệ thống để trông giống ứng dụng native hơn
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Áp dụng font mặc định cho toàn bộ UI
        setUIFont(new javax.swing.plaf.FontUIResource("Segoe UI", Font.PLAIN, 14));

        model = new CalendarModel();
        currentSelectedDate = LocalDate.now();
        
        // Khởi tạo cơ sở dữ liệu và tải danh sách cuộc hẹn từ file
        DatabaseUtil.initializeDatabase();
        DatabaseUtil.loadAllAppointments(model);
        
        setTitle("Calendar Appointment Manager");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(15, 15));
        getContentPane().setBackground(new Color(249, 250, 251)); // Tailwind gray-50
        
        appointmentListModel = new DefaultListModel<>();
        calendarPanel = new CalendarPanel(model, currentSelectedDate, this::onDateSelected);
        
        buildUI();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof javax.swing.plaf.FontUIResource)
                UIManager.put(key, f);
        }
    }

    private void buildUI() {
        // --- Top Navigation Panel ---
        JPanel topPanel = new JPanel(new BorderLayout(10, 0));
        topPanel.setBorder(new EmptyBorder(15, 20, 15, 20));
        topPanel.setBackground(Color.WHITE);
        topPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235))); // Bottom border
        
        JPanel datePickerPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        datePickerPanel.setOpaque(false);
        
        // Bộ chọn ngày tháng năm ở đầu ứng dụng
        JSpinner daySpinner = new JSpinner(new SpinnerNumberModel(currentSelectedDate.getDayOfMonth(), 1, 31, 1));
        JSpinner monthSpinner = new JSpinner(new SpinnerNumberModel(currentSelectedDate.getMonthValue(), 1, 12, 1));
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(currentSelectedDate.getYear(), 1900, 2100, 1));
        
        daySpinner.setPreferredSize(new Dimension(60, 30));
        monthSpinner.setPreferredSize(new Dimension(60, 30));
        yearSpinner.setPreferredSize(new Dimension(80, 30));
        
        JButton goButton = new JButton("Go");
        styleButton(goButton, new Color(229, 231, 235), new Color(209, 213, 219), new Color(17, 24, 39));
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
        
        JPanel navigationPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        navigationPanel.setOpaque(false);
        
        JButton prevButton = new JButton("◄ Prev");
        styleButton(prevButton, new Color(229, 231, 235), new Color(209, 213, 219), new Color(17, 24, 39));
        prevButton.addActionListener(e -> {
            YearMonth prev = calendarPanel.getCurrentMonth().minusMonths(1);
            calendarPanel.setCurrentMonth(prev);
        });
        
        JButton todayButton = new JButton("Today");
        todayButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        styleButton(todayButton, new Color(219, 234, 254), new Color(191, 219, 254), new Color(30, 64, 175));
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

        JButton nextButton = new JButton("Next ►");
        styleButton(nextButton, new Color(229, 231, 235), new Color(209, 213, 219), new Color(17, 24, 39));
        nextButton.addActionListener(e -> {
            YearMonth next = calendarPanel.getCurrentMonth().plusMonths(1);
            calendarPanel.setCurrentMonth(next);
        });
        
        navigationPanel.add(prevButton);
        navigationPanel.add(todayButton);
        navigationPanel.add(nextButton);
        
        topPanel.add(navigationPanel, BorderLayout.EAST);
        add(topPanel, BorderLayout.NORTH);
        
        // --- Center Content: Calendar + Sidebar ---
        JPanel centerContent = new JPanel(new BorderLayout(20, 0));
        centerContent.setOpaque(false);
        centerContent.setBorder(new EmptyBorder(10, 20, 20, 20));
        
        // Wrap Calendar Panel in a styled container
        JPanel calendarContainer = new JPanel(new BorderLayout());
        calendarContainer.setBackground(Color.WHITE);
        calendarContainer.setBorder(BorderFactory.createLineBorder(new Color(229, 231, 235), 1));
        calendarContainer.add(calendarPanel, BorderLayout.CENTER);
        centerContent.add(calendarContainer, BorderLayout.CENTER);
        
        // --- Side Panel ---
        JPanel sidePanel = new JPanel(new BorderLayout(15, 15));
        sidePanel.setPreferredSize(new Dimension(350, 0));
        sidePanel.setOpaque(false);
        
        // Sidebar Header
        JPanel sideHeader = new JPanel(new BorderLayout());
        sideHeader.setOpaque(false);
        selectedDateLabel = new JLabel(formatDate(currentSelectedDate));
        selectedDateLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        selectedDateLabel.setForeground(new Color(17, 24, 39));
        sideHeader.add(selectedDateLabel, BorderLayout.CENTER);
        
        sidePanel.add(sideHeader, BorderLayout.NORTH);
        
        // Appointments List
        JList<Appointment> appointmentList = new JList<>(appointmentListModel);
        appointmentList.setCellRenderer(new AppointmentCellRenderer());
        appointmentList.setBackground(new Color(249, 250, 251));
        appointmentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        listScrollPane = new JScrollPane(appointmentList);
        listScrollPane.setBorder(BorderFactory.createEmptyBorder());
        listScrollPane.getViewport().setBackground(new Color(249, 250, 251));
        
        // Empty state label
        emptyStatePanel = new JPanel(new GridBagLayout());
        emptyStatePanel.setBackground(new Color(249, 250, 251));
        JLabel emptyLabel = new JLabel("No appointments for this day");
        emptyLabel.setForeground(new Color(156, 163, 175)); // gray-400
        emptyLabel.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        emptyStatePanel.add(emptyLabel);

        sidePanel.add(listScrollPane, BorderLayout.CENTER);
        
        // Add Button
        JButton addButton = new JButton("+ Add Appointment");
        addButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addButton.setPreferredSize(new Dimension(0, 45));
        styleButton(addButton, new Color(59, 130, 246), new Color(37, 99, 235), Color.WHITE);
        addButton.addActionListener(e -> openAddDialog());
        
        sidePanel.add(addButton, BorderLayout.SOUTH);
        
        centerContent.add(sidePanel, BorderLayout.EAST);
        add(centerContent, BorderLayout.CENTER);

        // Lần đầu tiên hiển thị danh sách cuộc hẹn cho ngày hiện tại
        refreshAppointments();
    }

    // Cập nhật lại giao diện khi người dùng chọn ngày mới
    private void onDateSelected() {
        calendarPanel.setSelectedDate(currentSelectedDate);
        selectedDateLabel.setText(formatDate(currentSelectedDate));
        refreshAppointments();
        calendarPanel.repaint();
    }

    // Phiên bản callback khi CalendarPanel trả về ngày đã chọn
    private void onDateSelected(LocalDate selectedDate) {
        currentSelectedDate = selectedDate;
        calendarPanel.setSelectedDate(selectedDate);
        selectedDateLabel.setText(formatDate(selectedDate));
        refreshAppointments();
    }

    // Mở hộp thoại tạo cuộc hẹn mới
    private void openAddDialog() {
        AddAppointmentDialog dialog = new AddAppointmentDialog(this, model, currentSelectedDate);
        dialog.setVisible(true);
        if (dialog.isAppointmentAdded()) {
            refreshAppointments();
            calendarPanel.repaint();
        }
    }

    // Tải lại danh sách cuộc hẹn cho ngày đang chọn
    private void refreshAppointments() {
        appointmentListModel.clear();
        model.getAppointmentsForDate(currentSelectedDate).forEach(appointmentListModel::addElement);
        
        if (appointmentListModel.isEmpty()) {
            listScrollPane.setViewportView(emptyStatePanel);
        } else {
            JList<Appointment> list = new JList<>(appointmentListModel);
            list.setCellRenderer(new AppointmentCellRenderer());
            list.setBackground(new Color(249, 250, 251));
            listScrollPane.setViewportView(list);
        }
    }

    // Định dạng ngày để hiển thị ở sidebar
    private String formatDate(LocalDate date) {
        String[] dayNames = {"Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"};
        String[] monthNames = {"January", "February", "March", "April", "May", "June", 
                             "July", "August", "September", "October", "November", "December"};
        return dayNames[date.getDayOfWeek().getValue() % 7] + ", " + 
               monthNames[date.getMonthValue() - 1] + " " + date.getDayOfMonth();
    }

    // Tùy biến giao diện cho các nút
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CalendarApp());
    }

    // Custom Renderer for the Appointment List
    static class AppointmentCellRenderer extends JPanel implements ListCellRenderer<Appointment> {
        private JLabel timeLabel;
        private JLabel titleLabel;
        private JLabel locationLabel;

        public AppointmentCellRenderer() {
            setLayout(new BorderLayout(15, 5));
            setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                    BorderFactory.createEmptyBorder(15, 10, 15, 10)
            ));
            setBackground(Color.WHITE);

            timeLabel = new JLabel();
            timeLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
            timeLabel.setForeground(new Color(75, 85, 99)); // gray-600
            timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
            timeLabel.setPreferredSize(new Dimension(80, 0));

            titleLabel = new JLabel();
            titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
            titleLabel.setForeground(new Color(31, 41, 55)); // gray-800

            locationLabel = new JLabel();
            locationLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            locationLabel.setForeground(new Color(107, 114, 128)); // gray-500

            JPanel centerPanel = new JPanel(new GridLayout(2, 1, 0, 4));
            centerPanel.setOpaque(false);
            centerPanel.add(titleLabel);
            centerPanel.add(locationLabel);

            add(timeLabel, BorderLayout.WEST);
            add(centerPanel, BorderLayout.CENTER);
        }

        @Override
        public Component getListCellRendererComponent(JList<? extends Appointment> list, Appointment value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value != null) {
                timeLabel.setText(String.format("<html><center>%s<br/>|<br/>%s</center></html>", value.getStartTime(), value.getEndTime()));
                
                String groupTag = value.isGroupMeeting() ? "  [👥 Group]" : "";
                String reminderTag = value.isReminder() ? "  [⏰ Reminder]" : "";
                titleLabel.setText(value.getTitle() + groupTag + reminderTag);
                
                locationLabel.setText("📍 " + value.getLocation());
                
                if (isSelected) {
                    setBackground(new Color(239, 246, 255)); // blue-50
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 4, 1, 0, new Color(59, 130, 246)), // Left blue border
                            BorderFactory.createEmptyBorder(15, 6, 15, 10)
                    ));
                } else {
                    setBackground(Color.WHITE);
                    setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(229, 231, 235)),
                            BorderFactory.createEmptyBorder(15, 10, 15, 10)
                    ));
                }
            }
            return this;
        }
    }
}
