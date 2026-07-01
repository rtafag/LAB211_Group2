package repository;

import java.util.List;
import java.util.Locale;

import model.Medicine;

public class MedicineRepository extends CsvRepository<Medicine> {

    private final String fileName;

    public MedicineRepository() {
        this("data/medicines.csv");
    }

    public MedicineRepository(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public Medicine parseLine(String line) {
        return Medicine.fromCsvLine(line);
    }

    @Override
    public String toCsvLine(Medicine entity) {
        return entity.toCsvLine();
    }

    @Override
    protected String getHeader() {
        return "medicine_id,medicine_name,unit,units_per_box,description,manufacturer";
    }

    public List<Medicine> findAll() {
        return readAll(fileName);
    }

    public Medicine findFirstByName(String medicineName) {
        if (medicineName == null || medicineName.isBlank()) {
            return null;
        }
        String normalized = medicineName.trim().toLowerCase(Locale.ROOT);
        return findAll().stream()
                .filter(m -> m.getMedicineName() != null)
                .filter(m -> m.getMedicineName().trim().toLowerCase(Locale.ROOT).equals(normalized))
                .findFirst()
                .orElse(null);
    }

    public void save(Medicine medicine) {
        List<Medicine> medicines = findAll();
        medicines.add(medicine);
        writeAll(fileName, medicines);
    }

    public void updateMedicine(String medicineId, String newMedicineName, String newDescription, String newManufacturer) {
        if (medicineId == null || medicineId.isBlank()) {
            throw new IllegalArgumentException("Medicine ID is required");
        }

        List<Medicine> medicines = findAll();
        for (int i = 0; i < medicines.size(); i++) {
            Medicine current = medicines.get(i);
            if (!medicineId.trim().equals(current.getMedicineId())) {
                continue;
            }

            String updatedName = (newMedicineName == null || newMedicineName.isBlank())
                    ? current.getMedicineName()
                    : newMedicineName.trim();
            String updatedDescription = (newDescription == null || newDescription.isBlank())
                    ? current.getDescription()
                    : newDescription.trim();
            String updatedManufacturer = (newManufacturer == null || newManufacturer.isBlank())
                    ? current.getManufacturer()
                    : newManufacturer.trim();

            Medicine updated = new Medicine(
                    current.getMedicineId(),
                    updatedName,
                    current.getUnit(),
                    current.getUnitsPerBox(),
                    updatedDescription,
                    updatedManufacturer);
            medicines.set(i, updated);
            writeAll(fileName, medicines);
            return;
        }

        throw new IllegalArgumentException("Medicine not found: " + medicineId);
    }

    public String generateNextMedicineId() {
        int maxId = findAll().stream()
                .map(Medicine::getMedicineId)
                .filter(id -> id != null && id.startsWith("M"))
                .mapToInt(id -> {
                    try {
                        return Integer.parseInt(id.substring(1));
                    } catch (NumberFormatException ex) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
        return String.format("M%04d", maxId + 1);
    }
}
