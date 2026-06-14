package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class BatchLot extends BaseEntity {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String batchLotId;
    private final String medicineId;
    private final String branchId;
    private int quantity;
    private final LocalDate manufactureDate;
    private final LocalDate expiryDate;
    private int version;

    public BatchLot(String batchLotId, String medicineId, String branchId, int quantity, LocalDate expiryDate,
            int version) {
        this(batchLotId, medicineId, branchId, quantity, LocalDate.now(), expiryDate, version);
    }

    public BatchLot(String batchLotId, String medicineId, String branchId, int quantity, LocalDate manufactureDate,
            LocalDate expiryDate, int version) {
        this.batchLotId = batchLotId;
        this.medicineId = medicineId;
        this.branchId = branchId;
        this.quantity = quantity;
        this.manufactureDate = manufactureDate;
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

    public LocalDate getManufactureDate() {
        return manufactureDate;
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

    /**
     * Deduct box quantity from this batch. Throws IllegalArgumentException for
     * non-positive amounts, IllegalStateException when insufficient quantity.
     */
    public void deduct(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (amount > quantity) {
            throw new IllegalStateException("insufficient batch quantity");
        }
        this.quantity -= amount;
        this.version++;
    }

    @Override
    public String toCsvLine() {
        return String.join(",", batchLotId, medicineId, branchId, String.valueOf(quantity), manufactureDate.format(F),
                expiryDate.format(F), String.valueOf(version));
    }

    public static BatchLot fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        if (p.length == 6) {
            return new BatchLot(p[0], p[1], p[2], Integer.parseInt(p[3]), LocalDate.parse(p[4], F),
                    Integer.parseInt(p[5]));
        }
        return new BatchLot(p[0], p[1], p[2], Integer.parseInt(p[3]), LocalDate.parse(p[4], F),
                LocalDate.parse(p[5], F), Integer.parseInt(p[6]));
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
                && manufactureDate.equals(b.manufactureDate)
                && expiryDate.equals(b.expiryDate)
                && version == b.version;
    }

    @Override
    public int hashCode() {
        int r = batchLotId.hashCode();
        r = 31 * r + medicineId.hashCode();
        r = 31 * r + branchId.hashCode();
        r = 31 * r + Integer.hashCode(quantity);
        r = 31 * r + manufactureDate.hashCode();
        r = 31 * r + expiryDate.hashCode();
        r = 31 * r + Integer.hashCode(version);
        return r;
    }
}
