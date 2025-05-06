import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.stream.Collectors;

public class EventManagerApp {
    private final List<Event> events;
    private final EventFileHandler fileHandler;

    public EventManagerApp() {
        this.events = new ArrayList<>();
        this.fileHandler = new EventFileHandler("C:\\Users\\kaust\\IdeaProjects\\EventManagementApp2\\src\\events.json"); // Relative path
    }

    // File Operations
    public void loadEvents() {
        events.clear();
        events.addAll(fileHandler.loadEvents());
    }

    public void saveEvents() {
        fileHandler.saveEvents(events);
    }

    // Event CRUD Operations
    public void createEvent(String title, String date, String location, String type) {
        events.add(new Event(title, date, location, type));
    }

    public boolean deleteEvent(String title) {
        return events.removeIf(e -> e.getTitle().equals(title));
    }

    // Event Queries
    public Optional<Event> findEventByTitle(String title) {
        return events.stream()
                .filter(e -> e.getTitle().equals(title))
                .findFirst();
    }

    public List<Event> findEventsByType(String type) {
        return events.stream()
                .filter(e -> e.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }

    // Attendee Management
    public boolean registerAttendee(String eventTitle, String name, String email) {
        return findEventByTitle(eventTitle)
                .map(event -> event.registerAttendee(new Attendee(name, email)))
                .orElse(false);
    }

    public boolean removeAttendee(String eventTitle, int attendeeIndex) {
        return findEventByTitle(eventTitle)
                .map(event -> event.removeAttendee(attendeeIndex))
                .orElse(false);
    }

    // Data Access
    public List<Event> getAllEvents() {
        return Collections.unmodifiableList(events);
    }

    public List<Attendee> getEventAttendees(String eventTitle) {
        return findEventByTitle(eventTitle)
                .map(Event::getAttendees)
                .orElse(Collections.emptyList());
    }

    public String[] getEventTitles() {
        return events.stream()
                .map(Event::getTitle)
                .toArray(String[]::new);
    }

    // Display Methods
    public String getEventListDisplay() {
        return events.stream()
                .map(e -> String.format("%s (%s)", e.getTitle(), e.getDate()))
                .collect(Collectors.joining("\n"));
    }

    public String getEventDetailsDisplay() {
        if (events.isEmpty()) {
            return "No events found.";
        }
        return events.stream()
                .map(Event::getDetails)
                .collect(Collectors.joining("\n\n"));
    }

    // Inner class for file handling
    private static class EventFileHandler {
        private final String filePath;

        public EventFileHandler(String filePath) {
            this.filePath = filePath;
        }

        public List<Event> loadEvents() {
            try {
                return Event.EventJsonParser.loadEventsFromFile(filePath);
            } catch (IOException e) {
                System.err.println("Error loading events: " + e.getMessage());
                return Collections.emptyList();
            }
        }

        public void saveEvents(List<Event> events) {
            try (PrintWriter writer = new PrintWriter(filePath)) {
                writer.println("[");
                for (int i = 0; i < events.size(); i++) {
                    writer.print(events.get(i).convertToJson());
                    if (i < events.size() - 1) writer.println(",");
                }
                writer.println("\n]");
            } catch (IOException e) {
                System.err.println("Error saving events: " + e.getMessage());
            }
        }
    }

    // Additional utility methods
    public boolean isValidEvent(String title) {
        return title != null && !title.trim().isEmpty();
    }

    public boolean isValidDate(String date) {
        return date != null && date.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    public boolean isValidEmail(String email) {
        return email != null && email.matches("^[\\w-.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }
}