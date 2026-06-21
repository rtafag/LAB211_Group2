package controller;

import java.time.LocalDate;

import model.Prescription;
import model.PrescriptionItem;
import repository.PrescriptionItemRepository;
import repository.PrescriptionRepository;

public class PrescriptionController {

    private final PrescriptionRepository presRepo;
    private final PrescriptionItemRepository itemRepo;

    public PrescriptionController(PrescriptionRepository presRepo, PrescriptionItemRepository itemRepo) {
        this.presRepo = presRepo;
        this.itemRepo = itemRepo;
    }

    public Prescription createPrescription(String patientName, String patientDob, String createdDateInput,
            String branchId, String medicineId) {
        String createdDate = (createdDateInput == null || createdDateInput.isBlank())
                ? LocalDate.now().toString()
                : LocalDate.parse(createdDateInput).toString();
        String expiredDate = LocalDate.parse(createdDate).plusDays(5).toString();

        Prescription prescription = new Prescription(
                presRepo.generateNextPrescriptionId(),
                patientName,
                patientDob,
                createdDate,
                expiredDate,
                "PENDING",
                branchId,
                1);
        presRepo.save(prescription);

        PrescriptionItem item = new PrescriptionItem(
                itemRepo.generateNextPrescriptionItemId(),
                prescription.getPrescriptionId(),
                medicineId,
                1);
        itemRepo.save(item);

        return prescription;
    }

    public Prescription findById(String prescriptionId) {
        return presRepo.findById(prescriptionId);
    }

    public Prescription updatePrescriptionInfo(String prescriptionId, String patientNameInput, String patientDobInput,
            String createdDateInput, String branchIdInput, String medicineIdInput) {
        Prescription current = presRepo.findById(prescriptionId);
        if (current == null) {
            throw new IllegalArgumentException("Prescription not found: " + prescriptionId);
        }
        if ("DISPENSED".equals(current.getStatus())) {
            throw new IllegalStateException("Cannot edit a dispensed prescription");
        }

        String patientName = (patientNameInput == null || patientNameInput.isBlank())
                ? current.getPatientName()
                : patientNameInput;
        String patientDob = (patientDobInput == null || patientDobInput.isBlank())
                ? current.getPatientDob()
                : LocalDate.parse(patientDobInput).toString();
        String createdDate = (createdDateInput == null || createdDateInput.isBlank())
                ? current.getCreatedDate()
                : LocalDate.parse(createdDateInput).toString();
        String branchId = (branchIdInput == null || branchIdInput.isBlank())
                ? current.getBranchId()
                : branchIdInput;
        String expiredDate = LocalDate.parse(createdDate).plusDays(5).toString();

        Prescription updated = new Prescription(
                current.getPrescriptionId(),
                patientName,
                patientDob,
                createdDate,
                expiredDate,
                current.getStatus(),
                branchId,
                current.getVersion() + 1);

        presRepo.update(updated);

        if (medicineIdInput != null && !medicineIdInput.isBlank()) {
            itemRepo.updateFirstMedicineIdByPrescriptionId(prescriptionId, medicineIdInput);
        }

        return updated;
    }
}
