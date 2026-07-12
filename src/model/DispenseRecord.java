package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DispenseRecord extends BaseEntity {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String dispenseRecordId;
    private final String prescriptionId;
    private final String pharmacistId;
    private final LocalDate dispensedTime;
    private final String branchId;

    public DispenseRecord(String dispenseRecordId, String prescriptionId, String pharmacistId, LocalDate dispensedTime, String branchId) {
        this.dispenseRecordId = dispenseRecordId;
        this.prescriptionId = prescriptionId;
        this.pharmacistId = pharmacistId;
        this.dispensedTime = dispensedTime;
        this.branchId = branchId;
    }

    public String getDispenseRecordId() {
        return dispenseRecordId;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public String getPharmacistId() {
        return pharmacistId;
    }

    public LocalDate getDispensedTime() {
        return dispensedTime;
    }

    public String getBranchId() {
        return branchId;
    }

    @Override
    public String toCsvLine() {
        return String.join(",", dispenseRecordId, prescriptionId, pharmacistId, dispensedTime.format(F), branchId);
    }

    public static DispenseRecord fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        return new DispenseRecord(p[0], p[1], p[2], LocalDate.parse(p[3], F), p[4]);
    }
}
