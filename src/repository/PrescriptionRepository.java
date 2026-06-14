package repository;

import java.util.List;

import model.Prescription;
import model.PrescriptionStatus;

public class PrescriptionRepository extends CsvRepository<Prescription> {
    public static class PrescriptionAlreadyDispensedException extends Exception {
        public PrescriptionAlreadyDispensedException(String message) {
            super(message);
        }
    }

    private static final Object PRESCRIPTION_LOCK = new Object();

    private final String fileName;

    public PrescriptionRepository() {
        this("data/prescriptions.csv");
    }

    public PrescriptionRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Prescription parseLine(String line) {
        return Prescription.fromCsvLine(line);
    }

    @Override
    public String toCsvLine(Prescription entity) {
        return entity.toCsvLine();
    }

    @Override
    protected String getHeader() {
        return "prescription_id,patient_name,patient_dob,created_date,expired_date,status,branch_id,version";
    }

    public void markDispensed(String prescriptionId) throws PrescriptionAlreadyDispensedException {
        synchronized (PRESCRIPTION_LOCK) {
            List<Prescription> prescriptions = readAll(fileName);
            for (int i = 0; i < prescriptions.size(); i++) {
                Prescription current = prescriptions.get(i);
                if (!prescriptionId.equals(current.getPrescriptionId())) {
                    continue;
                }
                if (current.getStatus() == PrescriptionStatus.DISPENSED) {
                    throw new PrescriptionAlreadyDispensedException(
                            "Prescription " + prescriptionId + " is already dispensed");
                }

                Prescription updated = new Prescription(
                        current.getPrescriptionId(),
                        current.getPatientName(),
                        current.getPatientDob(),
                        current.getCreatedDate(),
                        current.getExpiredDate(),
                        PrescriptionStatus.DISPENSED,
                        current.getBranchId(),
                        current.getVersion() + 1);
                prescriptions.set(i, updated);
                writeAll(fileName, prescriptions);
                return;
            }
            throw new IllegalArgumentException("Prescription not found: " + prescriptionId);
        }
    }
}
