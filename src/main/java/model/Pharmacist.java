package model;

public class Pharmacist extends BaseEntity {

    private final String pharmacistId;
    private final String name;
    private final String branchId;
    private final String username;
    private final String passwordHash;
    private final PharmacistRole role;

    public Pharmacist(String pharmacistId, String name, String branchId, String username, String passwordHash, PharmacistRole role) {
        this.pharmacistId = pharmacistId;
        this.name = name;
        this.branchId = branchId;
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public String getPharmacistId() {
        return pharmacistId;
    }

    public String getName() {
        return name;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public PharmacistRole getRole() {
        return role;
    }

    @Override
    public String toCsvLine() {
        return String.join(",", pharmacistId, name, branchId, username, passwordHash, role.name());
    }

    public static Pharmacist fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        PharmacistRole r = PharmacistRole.valueOf(p[5]);
        return new Pharmacist(p[0], p[1], p[2], p[3], p[4], r);
    }
}
