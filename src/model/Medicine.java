package model;

public class Medicine extends BaseEntity {

    private final String medicineId;
    private final String medicineName;
    private final String unit;
    private final String description;
    private final String manufacturer;

    public Medicine(String medicineId, String medicineName, String unit, String description, String manufacturer) {
        this.medicineId = medicineId;
        this.medicineName = medicineName;
        this.unit = unit;
        this.description = description;
        this.manufacturer = manufacturer;
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

    public String getDescription() {
        return description;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    @Override
    public String toCsvLine() {
        return String.join(",", medicineId, medicineName, unit, description, manufacturer);
    }

    public static Medicine fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        return new Medicine(p[0], p[1], p[2], p[3], p[4]);
    }
}
