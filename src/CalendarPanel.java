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
    
    // Dynamic sizes based on panel width/height
    private int cellWidth;
    private int cellHeight;
    private static final int GRID_COLS = 7;
    private static final int HEADER_HEIGHT = 60;
    
    private int hoveredRow = -1;
    private int hoveredCol = -1;

    public CalendarPanel(CalendarModel model, LocalDate selectedDate, Consumer<LocalDate> dateSelectionCallback) {
        this.model = model;
        this.selectedDate = selectedDate;
        this.currentMonth = YearMonth.from(selectedDate);
        this.dateSelectionCallback = dateSelectionCallback;
        
        setPreferredSize(new Dimension(500, 450));
        setBackground(Color.WHITE);
        
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                handleDateClick(e.getX(), e.getY());
            }
            @Override
            public void mouseExited(MouseEvent e) {
                hoveredRow = -1;
                hoveredCol = -1;
                repaint();
            }
        });
        addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                handleMouseMove(e.getX(), e.getY());
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
        if (y < HEADER_HEIGHT) return;
        
        int col = x / cellWidth;
        int row = (y - HEADER_HEIGHT) / cellHeight;
        
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

    private void handleMouseMove(int x, int y) {
        if (y < HEADER_HEIGHT) {
            if (hoveredRow != -1 || hoveredCol != -1) {
                hoveredRow = -1;
                hoveredCol = -1;
                repaint();
            }
            return;
        }
        
        int col = x / cellWidth;
        int row = (y - HEADER_HEIGHT) / cellHeight;
        
        if (row >= 0 && row < 6 && col >= 0 && col < GRID_COLS) {
            if (row != hoveredRow || col != hoveredCol) {
                hoveredRow = row;
                hoveredCol = col;
                repaint();
            }
        } else if (hoveredRow != -1 || hoveredCol != -1) {
            hoveredRow = -1;
            hoveredCol = -1;
            repaint();
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
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        cellWidth = getWidth() / GRID_COLS;
        cellHeight = (getHeight() - HEADER_HEIGHT) / 6;
        
        drawHeader(g2d);
        drawGridAndDates(g2d);
    }

    private void drawHeader(Graphics2D g) {
        // Month / Year Title
        g.setColor(new Color(17, 24, 39)); // gray-900
        g.setFont(new Font("Segoe UI", Font.BOLD, 22));
        String monthYear = currentMonth.format(java.time.format.DateTimeFormatter.ofPattern("MMMM yyyy"));
        FontMetrics fm = g.getFontMetrics();
        g.drawString(monthYear, 20, 35);
        
        // Days of week
        String[] dayNames = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        g.setColor(new Color(107, 114, 128)); // gray-500
        g.setFont(new Font("Segoe UI", Font.BOLD, 13));
        
        int yDayNames = HEADER_HEIGHT - 10;
        for (int i = 0; i < GRID_COLS; i++) {
            int x = i * cellWidth;
            FontMetrics dayFm = g.getFontMetrics();
            int textX = x + (cellWidth - dayFm.stringWidth(dayNames[i])) / 2;
            g.drawString(dayNames[i], textX, yDayNames);
        }
        
        // Separator Line
        g.setColor(new Color(243, 244, 246)); // gray-100
        g.drawLine(0, HEADER_HEIGHT, getWidth(), HEADER_HEIGHT);
    }

    private void drawGridAndDates(Graphics2D g) {
        LocalDate firstDay = currentMonth.atDay(1);
        int dayOfWeek = firstDay.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();
        
        int row = 0;
        int col = dayOfWeek;
        
        g.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        
        // Draw grid lines
        g.setColor(new Color(243, 244, 246)); // gray-100
        for (int i = 1; i <= 6; i++) {
            g.drawLine(0, HEADER_HEIGHT + i * cellHeight, getWidth(), HEADER_HEIGHT + i * cellHeight);
        }
        for (int i = 1; i < GRID_COLS; i++) {
            g.drawLine(i * cellWidth, HEADER_HEIGHT, i * cellWidth, getHeight());
        }

        for (int day = 1; day <= daysInMonth; day++) {
            int x = col * cellWidth;
            int y = HEADER_HEIGHT + row * cellHeight;
            
            LocalDate date = LocalDate.of(currentMonth.getYear(), currentMonth.getMonth(), day);
            boolean hasAppointments = !model.getAppointmentsForDate(date).isEmpty();
            boolean isToday = date.equals(LocalDate.now());
            
            // Draw Hover Effect
            if (row == hoveredRow && col == hoveredCol && !date.equals(selectedDate)) {
                g.setColor(new Color(243, 244, 246)); // gray-100
                g.fillRect(x + 1, y + 1, cellWidth - 1, cellHeight - 1);
            }
            
            // Draw Selection Circle
            if (date.equals(selectedDate)) {
                g.setColor(new Color(59, 130, 246)); // blue-500
                int circleSize = Math.min(cellWidth, cellHeight) - 20;
                int circleX = x + (cellWidth - circleSize) / 2;
                int circleY = y + (cellHeight - circleSize) / 2;
                g.fillOval(circleX, circleY, circleSize, circleSize);
                g.setColor(Color.WHITE);
                g.setFont(new Font("Segoe UI", Font.BOLD, 15));
            } else {
                if (isToday) {
                    g.setColor(new Color(59, 130, 246)); // blue text for today
                    g.setFont(new Font("Segoe UI", Font.BOLD, 15));
                } else {
                    g.setColor(new Color(55, 65, 81)); // gray-700
                    g.setFont(new Font("Segoe UI", Font.PLAIN, 15));
                }
            }
            
            // Draw Date Text
            FontMetrics fm = g.getFontMetrics();
            String dayStr = String.valueOf(day);
            int textX = x + (cellWidth - fm.stringWidth(dayStr)) / 2;
            int textY = y + (cellHeight - fm.getHeight()) / 2 + fm.getAscent();
            g.drawString(dayStr, textX, textY);
            
            // Draw Appointment Dot
            if (hasAppointments) {
                if (date.equals(selectedDate)) {
                    g.setColor(Color.WHITE); // White dot if selected
                } else {
                    g.setColor(new Color(59, 130, 246)); // blue dot
                }
                int dotSize = 6;
                int dotX = x + (cellWidth - dotSize) / 2;
                int dotY = textY + 5; 
                g.fillOval(dotX, dotY, dotSize, dotSize);
            }
            
            col++;
            if (col >= GRID_COLS) {
                col = 0;
                row++;
            }
        }
    }
}
