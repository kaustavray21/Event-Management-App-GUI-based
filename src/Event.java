import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class Event {
    private final String title;
    private final String date;
    private final String location;
    private final String type;
    private final List<Attendee> attendees;

    public Event(String title, String date, String location, String type) {
        this.title = title;
        this.date = date;
        this.location = location;
        this.type = type;
        this.attendees = new ArrayList<>();
    }

    // JSON Utility Methods
    private static String[] splitJsonObjects(String jsonArray) {
        List<String> objects = new ArrayList<>();
        int depth = 0;
        int start = 0;

        for (int i = 0; i < jsonArray.length(); i++) {
            char c = jsonArray.charAt(i);
            if (c == '{' || c == '[') depth++;
            if (c == '}' || c == ']') depth--;

            if (c == ',' && depth == 0) {
                objects.add(jsonArray.substring(start, i).trim());
                start = i + 1;
            }
        }

        if (start < jsonArray.length()) {
            objects.add(jsonArray.substring(start).trim());
        }

        return objects.toArray(new String[0]);
    }

    private static String extractJsonValue(String json, String key) {
        String keyPattern = "\"" + key + "\":";
        int keyIndex = json.indexOf(keyPattern);
        if (keyIndex == -1) return "";

        int valueStart = json.indexOf(':', keyIndex) + 1;
        valueStart = skipWhitespace(json, valueStart);
        if (valueStart >= json.length()) return "";

        if (json.charAt(valueStart) == '"') {
            return extractQuotedValue(json, valueStart);
        }
        return "";
    }

    private static int skipWhitespace(String str, int start) {
        while (start < str.length() && Character.isWhitespace(str.charAt(start))) {
            start++;
        }
        return start;
    }

    private static String extractQuotedValue(String json, int start) {
        int end = findUnescapedQuote(json, start + 1);
        return end > start ? unescapeJson(json.substring(start + 1, end)) : "";
    }

    private static int findUnescapedQuote(String json, int start) {
        for (int i = start; i < json.length(); i++) {
            if (json.charAt(i) == '"' && json.charAt(i - 1) != '\\') {
                return i;
            }
        }
        return -1;
    }

    private static String extractJsonArray(String json, String key) {
        String keyPattern = "\"" + key + "\":";
        int keyIndex = json.indexOf(keyPattern);
        if (keyIndex == -1) return "";

        int arrayStart = json.indexOf('[', keyIndex) + 1;
        if (arrayStart == 0) return "";

        int arrayEnd = findMatchingBracket(json, arrayStart - 1);
        return arrayEnd > arrayStart ? json.substring(arrayStart, arrayEnd) : "";
    }

    private static int findMatchingBracket(String json, int start) {
        int depth = 1;
        for (int i = start + 1; i < json.length(); i++) {
            char c = json.charAt(i);
            if (c == '[') depth++;
            if (c == ']') depth--;
            if (depth == 0) return i;
        }
        return -1;
    }

    private static String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private static String unescapeJson(String input) {
        return input.replace("\\\"", "\"").replace("\\n", "\n").replace("\\r", "\r").replace("\\t", "\t").replace("\\\\", "\\");
    }

    // Core Methods
    public String convertToJson() {
        return String.format("{\"title\":\"%s\",\"date\":\"%s\",\"location\":\"%s\",\"type\":\"%s\",\"attendees\":[%s]}", escapeJson(title), escapeJson(date), escapeJson(location), escapeJson(type), attendees.stream().map(Attendee::toAttendeeJson).collect(Collectors.joining(",")));
    }

    public String getDetails() {
        return String.format("Title: %s\nDate: %s\nLocation: %s\nType: %s\nAttendees: %d", title, date, location, type, attendees.size());
    }

    public boolean registerAttendee(Attendee attendee) {
        if (attendee == null || !attendee.isValid()) return false;
        return attendees.add(attendee);
    }

    public boolean removeAttendee(int index) {
        if (index < 0 || index >= attendees.size()) return false;
        attendees.remove(index);
        return true;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public String getType() {
        return type;
    }

    public List<Attendee> getAttendees() {
        return Collections.unmodifiableList(attendees);
    }

    // JSON Serialization/Deserialization
    public static class EventJsonParser {
        private EventJsonParser() {
        } // Prevent instantiation

        public static List<Event> loadEventsFromFile(String filePath) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String jsonContent = reader.lines().collect(Collectors.joining()).trim();
                if (jsonContent.startsWith("[") && jsonContent.endsWith("]")) {
                    jsonContent = jsonContent.substring(1, jsonContent.length() - 1).trim();
                }
                return parseEventArray(jsonContent);
            }
        }

        private static List<Event> parseEventArray(String jsonArray) {
            return Arrays.stream(splitJsonObjects(jsonArray)).filter(json -> !json.trim().isEmpty()).map(EventJsonParser::parseEvent).filter(Objects::nonNull).collect(Collectors.toList());
        }

        private static Event parseEvent(String json) {
            try {
                String title = extractJsonValue(json, "title");
                String date = extractJsonValue(json, "date");
                String location = extractJsonValue(json, "location");
                String type = extractJsonValue(json, "type");

                Event event = new Event(title, date, location, type);
                parseAttendees(json, event);
                return event;
            } catch (Exception e) {
                System.err.println("Error parsing event: " + e.getMessage());
                return null;
            }
        }

        private static void parseAttendees(String json, Event event) {
            if (json.contains("\"attendees\":")) {
                String attendeesJson = extractJsonArray(json, "attendees");
                Arrays.stream(splitJsonObjects(attendeesJson)).map(EventJsonParser::parseAttendee).filter(Objects::nonNull).forEach(event::registerAttendee);
            }
        }

        private static Attendee parseAttendee(String json) {
            String fixedJson = json.trim();
            if (fixedJson.isEmpty()) return null;

            if (!fixedJson.startsWith("{")) fixedJson = "{" + fixedJson;
            if (!fixedJson.endsWith("}")) fixedJson = fixedJson + "}";

            String name = extractJsonValue(fixedJson, "name");
            String email = extractJsonValue(fixedJson, "email");
            return (name.isEmpty() && email.isEmpty()) ? null : new Attendee(name, email);
        }
    }
}