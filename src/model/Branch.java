package model;

public class Branch extends BaseEntity {

    private final String branchId;
    private final String branchName;
    private final String address;
    private final String phone;

    public Branch(String branchId, String branchName, String address, String phone) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.address = address;
        this.phone = phone;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getBranchName() {
        return branchName;
    }

    public String getAddress() {
        return address;
    }

    public String getPhone() {
        return phone;
    }

    @Override
    public String toCsvLine() {
        return String.join(",", branchId, branchName, address, phone);
    }

    public static Branch fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        return new Branch(parts[0], parts[1], parts[2], parts[3]);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Branch)) {
            return false;
        }
        Branch b = (Branch) o;
        return branchId.equals(b.branchId);
    }

    @Override
    public int hashCode() {
        return branchId.hashCode();
    }
}
