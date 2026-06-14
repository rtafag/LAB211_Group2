package model;

public class Stock extends BaseEntity {
    private final String stockId;
    private final String branchId;
    private final String medicineId;
    private int quantity;
    private int version;

    public Stock(String stockId, String branchId, String medicineId, int quantity, int version) {
        this.stockId = stockId;
        this.branchId = branchId;
        this.medicineId = medicineId;
        this.quantity = quantity;
        this.version = version;
    }

    public String getStockId() {
        return stockId;
    }

    public String getBranchId() {
        return branchId;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public int getQuantity() {
        return quantity;
    }

    public int getVersion() {
        return version;
    }

    public void deduct(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount must be positive");
        }
        if (amount > quantity) {
            throw new IllegalStateException("insufficient stock");
        }
        this.quantity -= amount;
        this.version++;
    }

    @Override
    public String toCsvLine() {
        return String.join(",", stockId, branchId, medicineId, String.valueOf(quantity), String.valueOf(version));
    }

    public static Stock fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        return new Stock(p[0], p[1], p[2], Integer.parseInt(p[3]), Integer.parseInt(p[4]));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Stock)) {
            return false;
        }
        Stock s = (Stock) o;
        return stockId.equals(s.stockId)
                && branchId.equals(s.branchId)
                && medicineId.equals(s.medicineId)
                && quantity == s.quantity
                && version == s.version;
    }

    @Override
    public int hashCode() {
        int r = stockId.hashCode();
        r = 31 * r + branchId.hashCode();
        r = 31 * r + medicineId.hashCode();
        r = 31 * r + Integer.hashCode(quantity);
        r = 31 * r + Integer.hashCode(version);
        return r;
    }
}
