import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.function.Consumer;

public class CalendarPanel extends JPanel {
    private final CalendarModel model;
    private LocalDate selectedDate;
    private YearMonth currentMonth;
    private final Consumer<LocalDate> dateSelectionCallback;
    private static final int CELL_WIDTH = 60;
    private static final int CELL_HEIGHT = 60;
    private static final int GRID_COLS = 7;

    public CalendarPanel(CalendarModel model, LocalDate selectedDate, Consumer<LocalDate> dateSelectionCallback) {
        this.model = model;
        this.selectedDate = selectedDate;
        this.currentMonth = YearMonth.from(selectedDate);
        this.dateSelectionCallback = dateSelectionCallback;
        
        setPreferredSize(new Dimension(CELL_WIDTH * GRID_COLS, 400));
        setBackground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleDateClick(e.getX(), e.getY());
            }
        });
    }

    public void setCurrentMonth(YearMonth month) {
        this.currentMonth = month;
        repaint();
    }

    public YearMonth getCurrentMonth() {
        return currentMonth;
    }

    public void setSelectedDate(LocalDate date) {
        this.selectedDate = date;
        repaint();
    }

    private void handleDateClick(int x, int y) {
        int col = x / CELL_WIDTH;
        int row = (y - 40) / CELL_HEIGHT;
        
        if (row >= 0 && row < 6 && col >= 0 && col < GRID_COLS) {
            LocalDate clickedDate = getDateAtPosition(row, col);
            if (clickedDate != null && clickedDate.getMonth() == currentMonth.getMonth() && 
                clickedDate.getYear() == currentMonth.getYear()) {
                selectedDate = clickedDate;
                dateSelectionCallback.accept(clickedDate);
                repaint();
            }
        }
    }

    private LocalDate getDateAtPosition(int row, int col) {
        LocalDate firstDay = currentMonth.atDay(1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
        int dayOffset = row * GRID_COLS + col - dayOfWeek;
        
        if (dayOffset >= 0 && dayOffset < currentMonth.lengthOfMonth()) {
            return firstDay.plusDays(dayOffset);
        }
        return null;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        drawHeader(g2d);
        drawDays(g2d);
        drawDates(g2d);
    }

    private void drawHeader(Graphics2D g) {
        g.setColor(new Color(63, 81, 181));
        g.fillRect(0, 0, getWidth(), 40);
        
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        String monthYear = currentMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"));
        FontMetrics fm = g.getFontMetrics();
        int x = (getWidth() - fm.stringWidth(monthYear)) / 2;
        g.drawString(monthYear, x, 25);
    }

    private void drawDays(Graphics2D g) {
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        g.setColor(new Color(220, 220, 220));
        g.setFont(new Font("Arial", Font.BOLD, 12));
        
        for (int i = 0; i < GRID_COLS; i++) {
            int x = i * CELL_WIDTH;
            g.drawRect(x, 40, CELL_WIDTH, 20);
            FontMetrics fm = g.getFontMetrics();
            int textX = x + (CELL_WIDTH - fm.stringWidth(dayNames[i])) / 2;
            g.drawString(dayNames[i], textX, 55);
        }
    }

    private void drawDates(Graphics2D g) {
        LocalDate firstDay = currentMonth.atDay(1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();
        
        g.setFont(new Font("Arial", Font.PLAIN, 12));
        
        int row = 0;
        int col = dayOfWeek;
        
        for (int day = 1; day <= daysInMonth; day++) {
            int x = col * CELL_WIDTH;
            int y = 60 + row * CELL_HEIGHT;
            
            LocalDate date = LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), day);
            boolean hasAppointments = !model.getAppointmentsForDate(date).isEmpty();
            
            if (date.equals(selectedDate)) {
                g.setColor(new Color(63, 81, 181));
                g.fillRect(x, y, CELL_WIDTH, CELL_HEIGHT);
                g.setColor(Color.WHITE);
            } else if (hasAppointments) {
                g.setColor(new Color(255, 235, 205));
                g.fillRect(x, y, CELL_WIDTH, CELL_HEIGHT);
                g.setColor(Color.BLACK);
            } else {
                g.setColor(Color.WHITE);
                g.fillRect(x, y, CELL_WIDTH, CELL_HEIGHT);
                g.setColor(Color.BLACK);
            }
            
            g.drawRect(x, y, CELL_WIDTH, CELL_HEIGHT);
            FontMetrics fm = g.getFontMetrics();
            String dayStr = String.valueOf(day);
            int textX = x + (CELL_WIDTH - fm.stringWidth(dayStr)) / 2;
            int textY = y + CELL_HEIGHT / 2 + 5;
            g.drawString(dayStr, textX, textY);
            
            if (hasAppointments && !date.equals(selectedDate)) {
                g.setColor(new Color(63, 81, 181));
                g.fillOval(x + CELL_WIDTH / 2 - 3, y + CELL_HEIGHT - 10, 6, 6);
            }
            
            col++;
            if (col >= GRID_COLS) {
                col = 0;
                row++;
            }
        }
    }
}
