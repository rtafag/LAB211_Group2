package model;

public class PrescriptionItem extends BaseEntity {

    private final String prescriptionItemId;
    private final String prescriptionId;
    private final String medicineId;
    private final int quantity;

    public PrescriptionItem(String prescriptionItemId, String prescriptionId, String medicineId, int quantity) {
        this.prescriptionItemId = prescriptionItemId;
        this.prescriptionId = prescriptionId;
        this.medicineId = medicineId;
        this.quantity = quantity;
    }

    @Override
    public String toCsvLine() {
        return String.join(",", prescriptionItemId, prescriptionId, medicineId, String.valueOf(quantity));
    }

    public static PrescriptionItem fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        return new PrescriptionItem(p[0], p[1], p[2], Integer.parseInt(p[3]));
    }
}
