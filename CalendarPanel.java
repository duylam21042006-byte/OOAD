import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.function.Consumer;

public class CalendarPanel extends JPanel {
    private final CalendarModel model;
    private YearMonth currentMonth;
    private LocalDate selectedDate;
    private final Consumer<LocalDate> onDateSelected;
    private static final String[] DAY_NAMES = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int CELL_HEIGHT = 100;
    private static final int HEADER_HEIGHT = 50;
    private static final int DAY_LABEL_HEIGHT = 30;

    public CalendarPanel(CalendarModel model, LocalDate initialDate, Consumer<LocalDate> onDateSelected) {
        this.model = model;
        this.currentMonth = YearMonth.from(initialDate);
        this.selectedDate = initialDate;
        this.onDateSelected = onDateSelected;
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 650));
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleMouseClick(e);
            }
        });
    }

    public YearMonth getCurrentMonth() {
        return currentMonth;
    }

    public void setCurrentMonth(YearMonth month) {
        this.currentMonth = month;
        repaint();
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        drawHeader(g2, getWidth(), HEADER_HEIGHT);
        drawDayLabels(g2, getWidth(), HEADER_HEIGHT, DAY_LABEL_HEIGHT);
        drawCalendar(g2, HEADER_HEIGHT + DAY_LABEL_HEIGHT, CELL_HEIGHT);
    }
    
    private void handleMouseClick(MouseEvent e) {
        int width = getWidth();
        int cellWidth = width / COLS;
        int startY = HEADER_HEIGHT + DAY_LABEL_HEIGHT;
        
        int cellX = e.getX() / cellWidth;
        int cellY = (e.getY() - startY) / CELL_HEIGHT;
        
        if (cellY >= 0 && cellY < ROWS && cellX >= 0 && cellX < COLS) {
            LocalDate firstDay = currentMonth.atDay(1);
            int firstDayOfWeek = (firstDay.getDayOfWeek().getValue() % 7);
            
            int positionInGrid = cellY * COLS + cellX;
            int dayOfMonth = positionInGrid - firstDayOfWeek + 1;
            
            System.out.println("Click at: x=" + e.getX() + " y=" + e.getY() + 
                             " cellX=" + cellX + " cellY=" + cellY +
                             " position=" + positionInGrid + " day=" + dayOfMonth);
            
            if (dayOfMonth > 0 && dayOfMonth <= currentMonth.lengthOfMonth()) {
                selectedDate = currentMonth.atDay(dayOfMonth);
                System.out.println("Selected date: " + selectedDate);
                onDateSelected.accept(selectedDate);
                repaint();
            }
        }
    }

    private void drawHeader(Graphics2D g, int width, int height) {
        g.setColor(new Color(63, 81, 181));
        g.fillRect(0, 0, width, height);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 20));
        String monthText = currentMonth.getMonth().toString().charAt(0) + 
                          currentMonth.getMonth().toString().substring(1).toLowerCase() + 
                          " " + currentMonth.getYear();
        FontMetrics fm = g.getFontMetrics();
        int textX = (width - fm.stringWidth(monthText)) / 2;
        g.drawString(monthText, textX, 35);
    }

    private void drawDayLabels(Graphics2D g, int width, int startY, int height) {
        g.setColor(new Color(230, 230, 230));
        g.fillRect(0, startY, width, height);
        g.setColor(Color.BLACK);
        g.setFont(new Font("Arial", Font.BOLD, 12));
        int cellWidth = width / COLS;
        for (int i = 0; i < DAY_NAMES.length; i++) {
            String dayName = DAY_NAMES[i];
            FontMetrics fm = g.getFontMetrics();
            int x = i * cellWidth + (cellWidth - fm.stringWidth(dayName)) / 2;
            g.drawString(dayName, x, startY + 20);
        }
    }

    private void drawCalendar(Graphics2D g, int startY, int cellHeight) {
        int width = getWidth();
        int cellWidth = width / COLS;
        LocalDate firstDay = currentMonth.atDay(1);
        int firstDayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();

        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = col * cellWidth;
                int y = startY + row * cellHeight;
                
                int positionInGrid = row * COLS + col;
                int dayOfMonth = positionInGrid - firstDayOfWeek + 1;
                
                LocalDate cellDate = null;
                boolean isCurrentMonth = true;
                
                if (dayOfMonth < 1) {
                    isCurrentMonth = false;
                    cellDate = currentMonth.minusMonths(1).atDay(
                        currentMonth.minusMonths(1).lengthOfMonth() + dayOfMonth);
                } else if (dayOfMonth <= daysInMonth) {
                    cellDate = currentMonth.atDay(dayOfMonth);
                } else {
                    isCurrentMonth = false;
                    cellDate = currentMonth.plusMonths(1).atDay(dayOfMonth - daysInMonth);
                }

                drawCell(g, x, y, cellWidth, cellHeight, cellDate, isCurrentMonth);
            }
        }
    }

    private void drawCell(Graphics2D g, int x, int y, int width, int height, LocalDate date, boolean isCurrentMonth) {
        g.setColor(isCurrentMonth ? Color.WHITE : new Color(240, 240, 240));
        g.fillRect(x, y, width, height);

        if (date.equals(selectedDate)) {
            g.setColor(new Color(63, 81, 181));
            g.fillRect(x, y, width, height);
            g.setColor(Color.WHITE);
        } else {
            g.setColor(new Color(200, 200, 200));
        }
        g.setStroke(new BasicStroke(1));
        g.drawRect(x, y, width, height);

        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(isCurrentMonth && !date.equals(selectedDate) ? Color.BLACK : 
                   (date.equals(selectedDate) ? Color.WHITE : Color.GRAY));
        String dayText = String.valueOf(date.getDayOfMonth());
        FontMetrics fm = g.getFontMetrics();
        g.drawString(dayText, x + 5, y + 20);

        List<Appointment> appointments = model.getAppointmentsForDate(date);
        g.setFont(new Font("Arial", Font.PLAIN, 9));
        int appointmentY = y + 35;
        for (int i = 0; i < Math.min(appointments.size(), 2); i++) {
            Appointment apt = appointments.get(i);
            String aptText = apt.getStartTime() + " " + apt.getTitle();
            if (aptText.length() > 15) {
                aptText = aptText.substring(0, 12) + "...";
            }
            g.setColor(new Color(100, 150, 255));
            g.drawString(aptText, x + 3, appointmentY);
            appointmentY += 12;
        }

        if (appointments.size() > 2) {
            g.setColor(new Color(150, 150, 150));
            g.drawString("+" + (appointments.size() - 2) + " more", x + 3, appointmentY);
        }
    }
}
