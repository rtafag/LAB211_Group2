package model;

public class Medicine extends BaseEntity {

    private final String medicineId;
    private final String medicineName;
    private final String unit;
    private final int unitsPerBox;
    private final double price;

    public Medicine(String medicineId, String medicineName, String unit, int unitsPerBox, double price) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.unit = unit;
        this.unitsPerBox = unitsPerBox;
        this.price = price;
    }

    public String getMedicineId() {
        return medicineId;
    }

    public String getMedicineName() {
        return medicineName;
    }

    public String getUnit() {
        return unit;
    }

    public int getUnitsPerBox() {
        return unitsPerBox;
    }

    public double getPrice() {
        return price;
    }

    @Override
    public String toCsvLine() {
        return String.join(",", medicineId, medicineName, unit, String.valueOf(unitsPerBox), String.valueOf(price));
    }

    public static Medicine fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        return new Medicine(p[0], p[1], p[2], Integer.parseInt(p[3]), Double.parseDouble(p[4]));
    }
}
