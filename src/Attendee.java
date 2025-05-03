import java.util.Objects;

public final class Attendee {
    private final String name;
    private final String email;

    public Attendee(String name, String email) {
        this.name = Objects.requireNonNullElse(name, "");
        this.email = Objects.requireNonNullElse(email, "");
    }

    public static Attendee fromJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return null;
        }

        String normalizedJson = normalizeJson(json);
        String name = extractJsonValue(normalizedJson, "name");
        String email = extractJsonValue(normalizedJson, "email");

        return new Attendee(name, email);
    }

    public String toAttendeeJson() {
        return String.format("{\"name\":\"%s\",\"email\":\"%s\"}",
                escapeJson(name), escapeJson(email));
    }

    public boolean isValid() {
        return !name.isBlank() &&
                email.contains("@") &&
                email.indexOf('@') > 0 &&
                email.indexOf('@') < email.length() - 1;
    }

    private static String normalizeJson(String json) {
        String trimmed = json.trim();
        if (!trimmed.startsWith("{")) trimmed = "{" + trimmed;
        if (!trimmed.endsWith("}")) trimmed = trimmed + "}";
        return trimmed;
    }

    private static String extractJsonValue(String json, String key) {
        String keyPattern = "\"" + key + "\":\"";
        int keyIndex = json.indexOf(keyPattern);
        if (keyIndex == -1) return "";

        int valueStart = keyIndex + keyPattern.length();
        int valueEnd = json.indexOf("\"", valueStart);
        return valueEnd > valueStart ?
                unescapeJson(json.substring(valueStart, valueEnd)) : "";
    }

    private static String escapeJson(String input) {
        return input.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static String unescapeJson(String input) {
        return input.replace("\\\"", "\"")
                .replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\t", "\t")
                .replace("\\\\", "\\");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attendee attendee = (Attendee) o;
        return name.equals(attendee.name) && email.equals(attendee.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, email);
    }

    @Override
    public String toString() {
        return String.format("%s <%s>", name, email);
    }

    // Getters
    public String getName() { return name; }
    public String getEmail() { return email; }
}