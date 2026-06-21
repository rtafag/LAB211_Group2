package repository;

import java.util.List;

import model.Prescription;

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

    public Prescription findById(String prescriptionId) {
        return readAll(fileName).stream()
                .filter(p -> prescriptionId.equals(p.getPrescriptionId()))
                .findFirst()
                .orElse(null);
    }

    public void save(Prescription prescription) {
        synchronized (PRESCRIPTION_LOCK) {
            List<Prescription> prescriptions = readAll(fileName);
            prescriptions.add(prescription);
            writeAll(fileName, prescriptions);
        }
    }

    public String generateNextPrescriptionId() {
        synchronized (PRESCRIPTION_LOCK) {
            int maxId = readAll(fileName).stream()
                    .map(Prescription::getPrescriptionId)
                    .filter(id -> id != null && id.startsWith("PR"))
                    .mapToInt(id -> {
                        try {
                            return Integer.parseInt(id.substring(2));
                        } catch (NumberFormatException ex) {
                            return 0;
                        }
                    })
                    .max()
                    .orElse(0);
            return String.format("PR%05d", maxId + 1);
        }
    }

    public void markDispensed(String prescriptionId) throws PrescriptionAlreadyDispensedException {
        synchronized (PRESCRIPTION_LOCK) {
            List<Prescription> prescriptions = readAll(fileName);
            for (int i = 0; i < prescriptions.size(); i++) {
                Prescription current = prescriptions.get(i);
                if (!prescriptionId.equals(current.getPrescriptionId())) {
                    continue;
                }
                if ("DISPENSED".equals(current.getStatus())) {
                    throw new PrescriptionAlreadyDispensedException(
                            "Prescription " + prescriptionId + " is already dispensed");
                }

                Prescription updated = new Prescription(
                        current.getPrescriptionId(),
                        current.getPatientName(),
                        current.getPatientDob(),
                        current.getCreatedDate(),
                        current.getExpiredDate(),
                        "DISPENSED",
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
