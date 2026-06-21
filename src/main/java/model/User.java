package model;

public class User {
    private String id;
    private String password;
    private String role; // PATIENT, PHARMACIST, MANAGER

    public User(String id, String password, String role) {
        this.id = id;
        this.password = password;
        this.role = normalizeRole(role);
    }

    public String getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public String getRole() {
        return role;
    }

    public static User fromCsvLine(String line) {
        String[] parts = line.split(",");
        if (parts.length >= 6) {
            String username = parts[3];
            String password = parts[4];
            String role = normalizeRole(parts[5]);
            return new User(username, password, role);
        }
        if (parts.length == 3) {
            return new User(parts[0], parts[1], normalizeRole(parts[2]));
        }
        throw new IllegalArgumentException("Invalid user CSV line: " + line);
    }

    private static String normalizeRole(String role) {
        if (role == null) {
            return "UNKNOWN";
        }
        String normalized = role.trim().toUpperCase();
        if ("STAFF".equals(normalized) || "PHARMACIST".equals(normalized)) {
            return "PHARMACIST";
        }
        if ("MANAGER".equals(normalized) || "ADMIN".equals(normalized)) {
            return "MANAGER";
        }
        if ("PATIENT".equals(normalized)) {
            return "PATIENT";
        }
        return normalized;
    }

    public String toCsvLine() {
        return id + "," + password + "," + role;
    }
}
