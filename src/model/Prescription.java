package model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Prescription extends BaseEntity {

    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private final String prescriptionId;
    private final String patientName;
    private final LocalDate patientDob;
    private final LocalDate createdDate;
    private final LocalDate expiredDate;
    private final PrescriptionStatus status;
    private final String branchId;
    private final int version;

    public Prescription(String prescriptionId, String patientName, LocalDate patientDob, LocalDate createdDate, LocalDate expiredDate, PrescriptionStatus status, String branchId, int version) {
        this.prescriptionId = prescriptionId;
        this.patientName = patientName;
        this.patientDob = patientDob;
        this.createdDate = createdDate;
        this.expiredDate = expiredDate;
        this.status = status;
        this.branchId = branchId;
        this.version = version;
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                prescriptionId,
                patientName,
                patientDob.format(F),
                createdDate.format(F),
                expiredDate.format(F),
                status.name(),
                branchId,
                String.valueOf(version)
        );
    }

    public static Prescription fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        return new Prescription(p[0], p[1], LocalDate.parse(p[2], F), LocalDate.parse(p[3], F), LocalDate.parse(p[4], F), PrescriptionStatus.valueOf(p[5]), p[6], Integer.parseInt(p[7]));
    }
}
