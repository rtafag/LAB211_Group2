package controller;

import java.util.List;

import model.Medicine;
import repository.MedicineRepository;

public class MedicineController {

    private final MedicineRepository medicineRepo;

    public MedicineController() {
        this.medicineRepo = new MedicineRepository();
    }

    public Medicine findById(String medicineId) {
        if (medicineId == null || medicineId.isBlank()) {
            return null;
        }
        return medicineRepo.findAll().stream()
                .filter(m -> medicineId.trim().equals(m.getMedicineId()))
                .findFirst()
                .orElse(null);
    }

    public Medicine addMedicine(String medicineName, String unit, String unitsPerBoxInput, String priceInput) {
        if (medicineName == null || medicineName.isBlank()) {
            throw new IllegalArgumentException("Medicine name is required");
        }
        if (unit == null || unit.isBlank()) {
            throw new IllegalArgumentException("Unit is required");
        }

        int unitsPerBox = Integer.parseInt(unitsPerBoxInput);
        double price = Double.parseDouble(priceInput);
        String medicineId = medicineRepo.generateNextMedicineId();

        Medicine medicine = new Medicine(medicineId, medicineName.trim(), unit.trim(), unitsPerBox, price);
        medicineRepo.save(medicine);
        return medicine;
    }

    public Medicine updateMedicine(String medicineId, String newName, String newUnit,
            String newUnitsPerBoxInput, String newPriceInput) {
        if (medicineId == null || medicineId.isBlank()) {
            throw new IllegalArgumentException("Medicine ID is required");
        }

        List<Medicine> medicines = medicineRepo.findAll();
        Medicine current = medicines.stream()
                .filter(m -> medicineId.trim().equals(m.getMedicineId()))
                .findFirst()
                .orElse(null);

        if (current == null) {
            throw new IllegalArgumentException("Medicine not found: " + medicineId);
        }

        String updatedName = (newName == null || newName.isBlank()) ? current.getMedicineName() : newName.trim();
        String updatedUnit = (newUnit == null || newUnit.isBlank()) ? current.getUnit() : newUnit.trim();
        int updatedUnitsPerBox = (newUnitsPerBoxInput == null || newUnitsPerBoxInput.isBlank())
                ? current.getUnitsPerBox()
                : Integer.parseInt(newUnitsPerBoxInput);
        double updatedPrice = (newPriceInput == null || newPriceInput.isBlank())
                ? current.getPrice()
                : Double.parseDouble(newPriceInput);

        Medicine updated = new Medicine(current.getMedicineId(), updatedName, updatedUnit, updatedUnitsPerBox, updatedPrice);
        medicines.replaceAll(m -> m.getMedicineId().equals(current.getMedicineId()) ? updated : m);
        medicineRepo.writeAll("data/medicines.csv", medicines);
        return updated;
    }

    public Medicine deleteMedicine(String medicineId) {
        if (medicineId == null || medicineId.isBlank()) {
            throw new IllegalArgumentException("Medicine ID is required");
        }

        List<Medicine> medicines = medicineRepo.findAll();
        Medicine current = medicines.stream()
                .filter(m -> medicineId.trim().equals(m.getMedicineId()))
                .findFirst()
                .orElse(null);

        if (current == null) {
            throw new IllegalArgumentException("Medicine not found: " + medicineId);
        }

        medicines.removeIf(m -> m.getMedicineId().equals(current.getMedicineId()));
        medicineRepo.writeAll("data/medicines.csv", medicines);
        return current;
    }
}
