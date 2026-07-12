package model;

public class User {

    private String phoneNumber;
    private String password;
    private String role;

    public User(String phoneNumber, String password, String role) {
        this.phoneNumber = phoneNumber;
        this.password = password;
        this.role = normalizeRole(role);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getId() {
        return phoneNumber;
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
            String phoneNumber = parts[3];
            String password = parts[4];
            String role = normalizeRole(parts[5]);
            return new User(phoneNumber, password, role);
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
            return "STAFF";
        }
        if ("MANAGER".equals(normalized) || "ADMIN".equals(normalized)) {
            return "ADMIN";
        }
        return normalized;
    }

    public String toCsvLine() {
        return phoneNumber + "," + password + "," + role;
    }
}
