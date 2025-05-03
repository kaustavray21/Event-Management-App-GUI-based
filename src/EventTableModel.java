import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class EventTableModel extends AbstractTableModel {
    private List<Event> events;
    private final String[] columnNames = {"Title", "Date", "Location", "Type", "Attendees"};

    public EventTableModel(List<Event> events) {
        this.events = new ArrayList<>(events);
    }

    public void setEvents(List<Event> events) {
        this.events = new ArrayList<>(events);
        fireTableDataChanged();
    }

    @Override
    public int getRowCount() {
        return events.size();
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public String getColumnName(int column) {
        return columnNames[column];
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Event event = events.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> event.getTitle();
            case 1 -> event.getDate();
            case 2 -> event.getLocation();
            case 3 -> event.getType();
            case 4 -> event.getAttendees().size();
            default -> null;
        };
    }
}