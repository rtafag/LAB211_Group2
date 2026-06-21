package controller;

import java.time.LocalDate;

import model.Prescription;
import repository.PrescriptionRepository;

public class PrescriptionController {

    private final PrescriptionRepository presRepo;

    public PrescriptionController(PrescriptionRepository presRepo) {
        this.presRepo = presRepo;
    }

    public Prescription createPrescription(String patientName, String patientDob, String createdDateInput,
            String branchId) {
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
        return prescription;
    }
}
