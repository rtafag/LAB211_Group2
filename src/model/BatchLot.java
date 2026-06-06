package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BatchLot extends BaseEntity {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String batchLotId;
    private final String medicineId;
    private final String branchId;
    private int quantity;
    private final LocalDate expiryDate;
    private int version;

    public BatchLot(String batchLotId, String medicineId, String branchId, int quantity, LocalDate expiryDate, int version) {
        this.batchLotId = batchLotId;
        this.medicineId = medicineId;
        this.branchId = branchId;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.version = version;
    }

    public String getBatchLotId() {
        return batchLotId;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getBranchId() {
        return branchId;
    }

    public int getQuantity() {
        return quantity;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public int getVersion() {
        return version;
    }

    public boolean isExpired() {
        return expiryDate.isBefore(LocalDate.now());
    }

    @Override
    public String toCsvLine() {
        return String.join(",", batchLotId, medicineId, branchId, String.valueOf(quantity), expiryDate.format(F), String.valueOf(version));
    }

    public static BatchLot fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        return new BatchLot(p[0], p[1], p[2], Integer.parseInt(p[3]), LocalDate.parse(p[4], F), Integer.parseInt(p[5]));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BatchLot)) {
            return false;
        }
        BatchLot b = (BatchLot) o;
        return batchLotId.equals(b.batchLotId)
                && medicineId.equals(b.medicineId)
                && branchId.equals(b.branchId)
                && quantity == b.quantity
                && expiryDate.equals(b.expiryDate)
                && version == b.version;
    }

    @Override
    public int hashCode() {
        int r = batchLotId.hashCode();
        r = 31 * r + medicineId.hashCode();
        r = 31 * r + branchId.hashCode();
        r = 31 * r + Integer.hashCode(quantity);
        r = 31 * r + expiryDate.hashCode();
        r = 31 * r + Integer.hashCode(version);
        return r;
    }
}
