package model;

public class Prescription extends BaseEntity {

    private final String prescriptionId;
    private final String patientName;
    private final String patientDob;
    private final String createdDate;
    private final String expiredDate;
    private final String status;
    private final String branchId;
    private final int version;

    public Prescription(String prescriptionId, String patientName, String patientDob, String createdDate,
            String expiredDate, String status, String branchId, int version) {
        this.prescriptionId = prescriptionId;
        this.patientName = patientName;
        this.patientDob = patientDob;
        this.createdDate = createdDate;
        this.expiredDate = expiredDate;
        this.status = status;
        this.branchId = branchId;
        this.version = version;
    }

    public String getPrescriptionId() {
        return prescriptionId;
    }

    public String getPatientName() {
        return patientName;
    }

    public String getPatientDob() {
        return patientDob;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public String getExpiredDate() {
        return expiredDate;
    }

    public String getStatus() {
        return status;
    }

    public String getBranchId() {
        return branchId;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public String toCsvLine() {
        return String.join(",",
                prescriptionId,
                patientName,
                patientDob,
                createdDate,
                expiredDate,
                status,
                branchId,
                String.valueOf(version));
    }

    public static Prescription fromCsvLine(String line) {
        String[] p = line.split(",", -1);
        return new Prescription(p[0], p[1], p[2], p[3], p[4], p[5], p[6], Integer.parseInt(p[7]));
    }
}
